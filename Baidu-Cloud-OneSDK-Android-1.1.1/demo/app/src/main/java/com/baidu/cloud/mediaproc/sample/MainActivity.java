package com.baidu.cloud.mediaproc.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.cloud.mediaproc.sample.databinding.ActivityMainBinding;
import com.baidu.cloud.mediaproc.sample.ui.contest.ContestEntranceActivity;
import com.baidu.cloud.mediaproc.sample.ui.lss.LiveRoomActivity;
import com.baidu.cloud.mediaproc.sample.ui.lss.ScreenStreamingService;
import com.baidu.cloud.mediaproc.sample.ui.shortvideo.ShortVideoActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private ActivityMainBinding binding;

    private int mResultCode;
    private Intent mResultData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }

        new RxPermissions(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (!aBoolean) {
                            finish();
                        }
                    }
                });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "权限授予失败，无法开启悬浮窗", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "悬浮窗权限授予成功", Toast.LENGTH_SHORT).show();
                    startMediaProjection();
                }
            }

        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Toast.makeText(this, "User cancelled", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.i(TAG, "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            startMediaProjection();
        }
    }

    public void onClickLSS(View view) {
        startActivity(new Intent(this, LiveRoomActivity.class));
    }

    public void onClickShortVideo(View view) {
        startActivity(new Intent(this, ShortVideoActivity.class));
    }

    public void onClickScreenCapture(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "需要开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            startMediaProjection();
        }
    }

    public void onClickKnowledgeContest(View view) {
        startActivity(new Intent(this, ContestEntranceActivity.class));
    }

    private void startMediaProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mResultCode == 0 || mResultData == null) {
                MediaProjectionManager mMediaProjectionManager =
                        (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if (mMediaProjectionManager != null) {
                    startActivityForResult(
                            mMediaProjectionManager.createScreenCaptureIntent(),
                            REQUEST_MEDIA_PROJECTION);
                }
            } else {
                new AlertDialog.Builder(this)
                        .setItems(new String[]{"开始录屏", "结束录屏"},
                                new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            Intent i = new Intent();
                                            i.putExtra("role", 1);
                                            i.putExtra("room", "s");
                                            i.putExtra("url_play", "rtmp://auto-play1.rocyblog.com:8935/test/");
                                            i.putExtra("url_push", "rtmp://auto-push1.rocyblog.com:8935/test/");
                                            i.putExtra("result_code", mResultCode);
                                            i.putExtra("result_data", mResultData);
                                            ScreenStreamingService.start(MainActivity.this, i);
                                            finish();
                                        } else {
                                            stopService(new Intent(MainActivity.this,
                                                    ScreenStreamingService.class));
                                        }
                                    }
                                })
                        .create()
                        .show();
            }
        }
    }
}
