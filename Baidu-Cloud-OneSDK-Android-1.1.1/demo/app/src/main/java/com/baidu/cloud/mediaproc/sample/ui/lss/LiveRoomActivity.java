package com.baidu.cloud.mediaproc.sample.ui.lss;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.baidu.cloud.mediaproc.sample.R;
import com.baidu.cloud.mediaproc.sample.databinding.ActivityLiveRoomBinding;
import com.jakewharton.rxbinding2.widget.RxTextView;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;

public class LiveRoomActivity extends AppCompatActivity {
    private static final String TAG = "LiveRoomActivity";

    private ActivityLiveRoomBinding binding;

    private static final String PUSH_URL = "rtmp://push1.rocyblog.com:8935/test/";
    private static final String PLAY_URL = "rtmp://play.rocyblog.com:8935/test/";
    private static final String PLAY_URL_MERGE = "rtmp://play1.rocyblog.com:8935/test/";

    private String pushUrl;
    private String playUrl;
    private String playUrlMerge;
    private String mRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_room);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeButtonEnabled(true);
        }
        initUIandEvent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUIandEvent() {
        Observable.combineLatest(RxTextView.textChanges(binding.etUrlPush),
                RxTextView.textChanges(binding.etRoom),
                new BiFunction<CharSequence, CharSequence, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull CharSequence push, @NonNull CharSequence room) throws Exception {
                        if (TextUtils.isEmpty(push) || TextUtils.isEmpty(room)) {
                            return false;
                        }
                        pushUrl = push.toString();
                        mRoom = room.toString();
                        return true;
                    }
                }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                binding.btnPush.setEnabled(aBoolean);
            }
        });
        Observable.combineLatest(RxTextView.textChanges(binding.etUrlPlay),
                RxTextView.textChanges(binding.etUrlPlayMerge),
                RxTextView.textChanges(binding.etRoom),
                new Function3<CharSequence, CharSequence, CharSequence, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull CharSequence play, @NonNull CharSequence merge,
                                         @NonNull CharSequence room) throws Exception {
                        if (TextUtils.isEmpty(play) || TextUtils.isEmpty(room)
                                || TextUtils.isEmpty(merge)) {
                            return false;
                        }
                        playUrl = play.toString();
                        playUrlMerge = merge.toString();
                        mRoom = room.toString();
                        return true;
                    }
                }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean aBoolean) throws Exception {
                binding.btnPlay.setEnabled(aBoolean);
            }
        });

        binding.etUrlPush.setText(PUSH_URL);
        binding.etUrlPlay.setText(PLAY_URL);
        binding.etUrlPlayMerge.setText(PLAY_URL_MERGE);

        binding.etRoom.requestFocus();
    }

    public void onClickPush(View view) {
        Intent i = new Intent(LiveRoomActivity.this, StreamingActivity.class);
        i.putExtra("role", 1);
        i.putExtra("room", mRoom);
        // 大主播连麦时看的一直是小主播的低延时画面
        i.putExtra("url_play", playUrl);
        i.putExtra("url_push", pushUrl);
        startActivity(i);
    }

    public void onClickPlay(View view) {
        Intent i = new Intent(LiveRoomActivity.this, GuestStreamingActivity.class);
        i.putExtra("role", 2);
        i.putExtra("room", mRoom);
        i.putExtra("url_play", playUrl);
        i.putExtra("url_push", pushUrl);
        i.putExtra("url_play_merge", playUrlMerge);
        startActivity(i);
    }

    public void onClickClose(View view) {
        finish();
    }
}
