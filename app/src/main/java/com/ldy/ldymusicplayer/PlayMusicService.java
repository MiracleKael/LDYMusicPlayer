package com.ldy.ldymusicplayer;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ldy.ldymusicplayer.utils.MusicLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by ${jiaojing} on 2017/10/7.
 * Desc：
 */

public class PlayMusicService extends Service{
    private String TAG = getClass().getSimpleName();
    /**
     * 三种播放模式
     */
    private static final int PLAYMODE_ORDER = 0;
    private static final int PLAYMODE_RANDOM = 1;
    private static final int PLAYMODE_SINGLE = 2;

    public static final String PLAY_MUSIC = "com.ldy.ldymusicplayer.action.PLAYMUSIC";

    private LocalBroadcastManager localBroadcastManager;
    private List<LocalMusicBean> musicList = new ArrayList<>();

    private IPlayMusicAidlInterface.Stub stub = new IPlayMusicAidlInterface.Stub() {
        PlayMusicService service = PlayMusicService.this;//得到外部服务的实例

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public void changeSpeed(float speed) throws RemoteException {
            service.changeSpeed(speed);
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public int getProgress() throws RemoteException {
            return service.getProgress();
        }

        @Override
        public void setPlayMode() throws RemoteException {
            service.setPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }
    };
    private IjkMediaPlayer ijkMediaPlayer;
    private int currentPositionInList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        return null;
        //这里一定要注意修改过来！！！
        //这里一定要注意修改过来！！！
        //这里一定要注意修改过来！！！
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        ijkMediaPlayer = new IjkMediaPlayer();
        //得到本地音乐集合，为了写Demo方面，我就放这了，它就是播放列表了。
        ContentResolver contentResolver = getContentResolver();
        musicList= MusicLoader.getInstance(contentResolver).getMusicList();
    }
    /**
     * 在此方法中，可以执行相关逻辑，如耗时操作
     * @param intent :由Activity传递给service的信息，存在intent中
     * @param flags ：规定的额外信息
     * @param startId ：开启服务时，如果有规定id，则传入startid
     * @return 返回值规定此startservice是哪种类型，粘性的还是非粘性的
     *          START_STICKY:粘性的，遇到异常停止后重新启动，并且intent=null
     *          START_NOT_STICKY:非粘性，遇到异常停止不会重启
     *          START_REDELIVER_INTENT:粘性的，重新启动，并且将Context传递的信息intent传递
     * 此方法是唯一的可以执行很多次的方法
     */
    @Override
//    public int onStartCommand(Intent intent, @IntDef(value = {Service.START_FLAG_REDELIVERY, Service.START_FLAG_RETRY}, flag = true) int flags, int startId) {
    public int onStartCommand(Intent intent,int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    class MyOnPreparedListener implements IMediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            start();
        }
    }

    class MyOnCompletionListener implements IMediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            //播放完播放下一首
            next();
        }
    }

    class MyOnErrorListener implements IMediaPlayer.OnErrorListener {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            //如果出错，尝试播放下一首
            next();
            return true;
        }
    }

    /**
     * 根据播放列表位置打开音频文件
     * @param position
     */
    public void openAudio(int position){
        //记录当前播放位置
        currentPositionInList = position;
        //得到播放对象
        LocalMusicBean localMusicBean = musicList.get(position);
        String localMusicBeanPath = localMusicBean.getPath();
        Log.e(TAG,"localMusicBeanPath==="+ localMusicBeanPath);
        if(ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
            ijkMediaPlayer.release();
        }
        //创建一个新的ijkMediaPlayer
        ijkMediaPlayer = new IjkMediaPlayer();
        //设置监听
        ijkMediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
        ijkMediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
        ijkMediaPlayer.setOnErrorListener(new MyOnErrorListener());

        try {
            ijkMediaPlayer.setDataSource(localMusicBeanPath);
            ijkMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    public void start(){
        //发送广播修改界面
        notifyChange(PLAY_MUSIC);
        //开始播放
        ijkMediaPlayer.start();
    }

    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * 暂停
     */
    public void pause(){
        ijkMediaPlayer.pause();
    }
    /**
     * 下一首
     */
    public void next(){
        if(currentPositionInList == musicList.size()-1) {
            currentPositionInList =0;
        }else {
            currentPositionInList ++;
        }
        openAudio(currentPositionInList);
    }
    /**
     * 上一首
     */
    public void pre(){
        if(currentPositionInList == 0) {
            currentPositionInList = musicList.size()-1;
        }else {
            currentPositionInList --;
        }
        openAudio(currentPositionInList);
    }
    /**
     * 停止
     */
    public void stop(){

    }
    /**
     * 改变播放速率
     */
    public void changeSpeed(float speed){
        ijkMediaPlayer.setSpeed(speed);
    }
    /**
     * 得到乐曲时长
     */
    public int getDuration(){
        int duration = (int)ijkMediaPlayer.getDuration();
        return duration;
    }
    /**
     * 得到当前播放进度，用于更新进度条
     */
    public int getProgress(){
        int currentPosition =(int) ijkMediaPlayer.getCurrentPosition();
        return currentPosition;
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(){

    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying(){
        return  ijkMediaPlayer.isPlaying();
    }

    public void seekTo(int position){
        ijkMediaPlayer.seekTo(position);
    }

//    /**
//     * 得到当前列表中的播放位置
//     */
//    public int getCurrentPositionInList(){
//        return 0;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
