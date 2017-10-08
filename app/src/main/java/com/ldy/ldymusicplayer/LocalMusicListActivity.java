package com.ldy.ldymusicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

public class LocalMusicListActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private static final int REQUEST_CODE_PERMISSION_MULTI = 101;
    private static final int REQUEST_CODE_SETTING = 300;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music_list);
        mContext = this;
        initPermission();

    }

    private void initPermission() {
        AndPermission.with(this)
                .requestCode(REQUEST_CODE_PERMISSION_MULTI)
                .permission(
                        //申请多个权限组
                        Permission.STORAGE,
                        Permission.LOCATION
                ).rationale(new RationaleListener() {
            @Override
            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                // 此对话框可以自定义，调用rationale.resume()就可以继续申请。
                AndPermission.rationaleDialog(mContext, rationale).show();
            }
        })
                .callback(listener)
                .start();
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。
            // 这里的requestCode就是申请时设置的requestCode。
            // 和onActivityResult()的requestCode一样，用来区分多个不同的请求。
            if (requestCode == REQUEST_CODE_PERMISSION_MULTI) {
//                Toast.makeText(MainActivity.this, "权限申请成功", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            if (requestCode == REQUEST_CODE_PERMISSION_MULTI) {
                Toast.makeText(mContext, "权限申请失败", Toast.LENGTH_SHORT).show();
            }

            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(mContext, deniedPermissions)) {
                // 第一种：用默认的提示语。
                AndPermission.defaultSettingDialog(LocalMusicListActivity.this,REQUEST_CODE_SETTING).show();
            }

        }


    };

    public void comeIn(View view){
        startActivity(new Intent(LocalMusicListActivity.this, PlayMusicActivity.class));
    }
}
