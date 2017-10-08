package com.ldy.ldymusicplayer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ldy.ldymusicplayer.utils.CircleImageView;
import com.ldy.ldymusicplayer.utils.DensityUtil;
import com.ldy.ldymusicplayer.utils.MusicLoader;
import com.ldy.ldymusicplayer.utils.TimeToString;

import java.math.BigDecimal;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ldy.ldymusicplayer.R.id.iv_play_or_pause;
import static com.ldy.ldymusicplayer.R.id.progress_seekbar;

public class PlayMusicActivity extends AppCompatActivity  {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cover)
    CircleImageView cover;
    @BindView(R.id.tv_speed)
    TextView tvSpeed;
    @BindView(R.id.speed_seekbar)
    SeekBar speedSeekbar;
    @BindView(R.id.tv_currentposition)
    TextView tvCurrentposition;
    @BindView(progress_seekbar)
    SeekBar progressSeekbar;
    @BindView(R.id.all_time)
    TextView allTime;
    @BindView(iv_play_or_pause)
    ImageView ivPlayOrPause;
    @BindView(R.id.iv_play_next)
    ImageView ivPlayNext;
    @BindView(R.id.iv_play_pre)
    ImageView ivPlayPre;
    @BindView(R.id.iv_play_list)
    ImageView ivPlayList;
    @BindView(R.id.iv_play_mode)
    ImageView ivPlayMode;
    private List<LocalMusicBean> musicList;
    private Dialog bottomDialog;
    private PlayListAdpter playListAdpter;
    private MyBroadcastReceiver myBroadcastReceiver;
    private IPlayMusicAidlInterface service;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = IPlayMusicAidlInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private TimeToString timeToString;

    private static final int UPDATE_VIEW = 1;//进度更新
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_VIEW:
                    try {
                        int currentPosition = service.getProgress();
                        tvCurrentposition.setText(timeToString.translate(currentPosition));
                        progressSeekbar.setProgress(currentPosition);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    removeMessages(UPDATE_VIEW);
                    sendEmptyMessageDelayed(UPDATE_VIEW, 1000);

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        ButterKnife.bind(this);
        timeToString = new TimeToString();
        initToolbar();//初始化toolbar
        startAndBindService();
        initBraoadcast();//初始化广播接收器
        initSeekBarListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPlayList();//初始化播放列表数据
    }

    private void initSeekBarListener() {
        speedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = ProgressToSpeed(progress);
                    tvSpeed.setText(speed + "X");
                    try {
                        service.changeSpeed(speed);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        progressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    try {
                        service.seekTo(progress);
                        progressSeekbar.setProgress(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 将进度转换为倍数
     */
    private float ProgressToSpeed(int progress) {
        float speed = 1.0f;
        if (progress <= 50) {
            float f = (float) (progress + 50) / 100;
            BigDecimal b = new BigDecimal(f);
            speed = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            float f = (float) (progress - 50) / 50;
            BigDecimal b = new BigDecimal(f);
            speed = 1.0f + b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        }

        return speed;
    }


    private void startAndBindService() {
        Intent intent = new Intent(this, PlayMusicService.class);
        intent.setAction("com.ldy.ldymusicplayer.OPENAUDIO");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);//防止service多次实例化
    }

    private void initBraoadcast() {
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayMusicService.PLAY_MUSIC);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                myBroadcastReceiver, intentFilter);
    }

    private String TAG = getClass().getSimpleName();
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"onReceive======");
            String action = intent.getAction();
            if(PlayMusicService.PLAY_MUSIC.equals(action)) {
                updateView();
            }
        }
    }

    private void updateView() {
        Log.e(TAG,"updateView======");
        try {
            int duration = service.getDuration();
            Log.e(TAG,"duration======"+ duration);
            progressSeekbar.setMax(duration);
            String translate = timeToString.translate(duration);
            Log.e(TAG,"translate======"+ translate);
            allTime.setText(translate);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mHandler.sendEmptyMessage(UPDATE_VIEW);
    }

    private void initPlayList() {
        ContentResolver contentResolver = getContentResolver();
        MusicLoader instance = MusicLoader.getInstance(contentResolver);
        musicList = instance.getMusicList();
    }

    private void initToolbar() {
        toolbar.setTitle("LDY_Music");
        setSupportActionBar(toolbar);
    }


    @OnClick(R.id.iv_play_list)
    public void showPlaylist(){
        show();
    }
    @OnClick(R.id.iv_play_or_pause)
    public void playOrPause(){
        try {
            boolean playing = service.isPlaying();
            if (playing) {
                service.pause();
            } else {
                service.start();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @OnClick(R.id.iv_play_pre)
    public void pre(){
        try {
            service.pre();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @OnClick(R.id.iv_play_next)
    public void next(){
        try {
            service.next();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void show() {
        bottomDialog = new Dialog(this, R.style.BottomDialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_music_list, null);
        RecyclerView musicRecyclerview = (RecyclerView) contentView.findViewById(R.id.musiclist);
        musicRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        playListAdpter = new PlayListAdpter(this, musicList);
        musicRecyclerview.setAdapter(playListAdpter);

        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = this.getResources().getDisplayMetrics().widthPixels;
        layoutParams.height = DensityUtil.dip2px(this, 400);
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    class PlayListAdpter extends RecyclerView.Adapter<PlayListAdpter.MyViewHolder> {
        private Context mContext;
        List<LocalMusicBean> musicPlaylist;
        private final LayoutInflater inflater;

        public PlayListAdpter(Context context, List<LocalMusicBean> list) {
            mContext = context;
            musicPlaylist = list;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_playlist, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }


        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.musicName.setText(musicPlaylist.get(position).getTitle());

            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            holder.musicName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        service.openAudio(position);
                        bottomDialog.dismiss();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return musicPlaylist.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView musicName;
            ImageView imgDelete;
            ImageView imgTrumpet;

            public MyViewHolder(View itemView) {
                super(itemView);
                musicName = (TextView) itemView.findViewById(R.id.music_name);
                imgDelete = (ImageView) itemView.findViewById(R.id.img_delete);
                imgTrumpet = (ImageView) itemView.findViewById(R.id.img_trumpet);
            }
        }
    }
}
