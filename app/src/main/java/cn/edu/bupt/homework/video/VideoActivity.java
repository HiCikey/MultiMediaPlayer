package cn.edu.bupt.homework.video;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

import cn.edu.bupt.homework.R;

public class VideoActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    private static SeekBar seekBar;
    @SuppressLint("StaticFieldLeak")
    private static TextView textTime;

    private static final MyHandler handler = new MyHandler();
    private VideoView videoView;
    private RelativeLayout control_up;      // 上层控制面板
    private RelativeLayout control_below;   // 下层控制面板
    private CountDownTimer countDownTimer;  // 控制面板和状态栏的隐藏定时器
    private Button btnPlay;
    private Button btnPause;
    private Timer timer;

    private Uri curUri;                         // 当前播放视频uri
    private int previousProgress = 0;           // 保存点击文件管理器前视频的播放位置
    private boolean isVideoCompleted = false;   // 视频是否完成播放
    private boolean isChooserClicked = false;   // 是否是由点击选文件按钮触发的onPaused()
    private boolean isGetPermission = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        // 隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        // 动态获取权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            // 播放默认视频
            findAndSetView();
            curUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
            beginPlay(0);
            isGetPermission = false;
        }
    }

    @Override
    protected void onPause() {
        Log.d("CheckVideo", "onPause() isChooser: " + isChooserClicked + "  isPermission:" + isGetPermission);
        super.onPause();
        if (!isChooserClicked && !isGetPermission)
            saveTheSite();
    }

    @Override
    protected void onRestart() {
        Log.d("CheckVideo", "onRestart() isChooser:" + isChooserClicked);
        super.onRestart();
        if (!isChooserClicked)
            recoverTheSite();
        else
            isChooserClicked = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        releaseCountDownTimer();
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("CheckVideo", "permission");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length != 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 播放默认视频
                findAndSetView();
                curUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
                beginPlay(0);
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                isGetPermission = false;
            } else {
                Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void findAndSetView() {
        videoView = findViewById(R.id.video_view);
        control_below = findViewById(R.id.control_below);
        control_up = findViewById(R.id.control_up);
        seekBar = findViewById(R.id.seekbar_video);
        btnPlay = findViewById(R.id.btn_play_video);
        btnPause = findViewById(R.id.btn_pause_video);
        textTime = findViewById(R.id.time_video);

        btnPlay.setOnClickListener(v -> pressButtonPlay());
        btnPause.setOnClickListener(v -> pressButtonPause());
        findViewById(R.id.choose_file).setOnClickListener(v -> pressButtonChooser());
        findViewById(R.id.screen_change).setOnClickListener(v -> changeScreenDirection());
        findViewById(R.id.back).setOnClickListener(v -> finish());

        setSeekBar();
        setVideoView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 用户选择视频，获取视频信息并播放
        if (data != null && resultCode == Activity.RESULT_OK) {
            curUri = data.getData();
            videoView.setVideoURI(curUri);
            addTimer();
            addCountDownTimer();
            videoView.start();
            btnPlay.setVisibility(View.INVISIBLE);
            btnPause.setVisibility(View.VISIBLE);
        } else
            recoverTheSite();
    }

    /**
     * 进度条配置
     */
    private void setSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                releaseCountDownTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 开始播放后拖动进度条
                int pro = seekBar.getProgress();
                videoView.seekTo(pro);
                // 暂停时拖动进度条
                pressButtonPlay();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setVideoView() {
        videoView.setOnClickListener(v -> {
        });
        // 视频播放结束时的动作
        videoView.setOnCompletionListener(mp -> {
            isVideoCompleted = true;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            btnPause.setVisibility(View.INVISIBLE);
            btnPlay.setVisibility(View.VISIBLE);
        });
        // 双击
        videoView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector mGesture;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGesture == null) {
                    mGesture = new GestureDetector(VideoActivity.this, new GestureDetector.SimpleOnGestureListener());
                    mGesture.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            clickScreen();
                            return true;
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            if (videoView.isPlaying())
                                pressButtonPause();
                            else
                                pressButtonPlay();
                            // 控制面板的隐藏重新计时
                            releaseCountDownTimer();
                            addCountDownTimer();
                            return true;
                        }

                        @Override
                        public boolean onDoubleTapEvent(MotionEvent e) {
                            return false;
                        }
                    });
                }
                return mGesture.onTouchEvent(event);
            }
        });
    }

    /**
     * 设置布局后开始播放
     *
     * @param progress 视频开始播放的进度，横竖屏切换时有效
     */
    private void beginPlay(int progress) {
        videoView.setVideoURI(curUri);
        if (progress != 0)
            videoView.seekTo(progress);
        btnPlay.setVisibility(View.INVISIBLE);
        if (timer == null)
            addTimer();
        addCountDownTimer();
        videoView.start();
    }

    /**
     * 点击播放按钮
     */
    private void pressButtonPlay() {
        if (timer == null)
            addTimer();
        if (!videoView.isPlaying())
            videoView.start();
        btnPlay.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        releaseCountDownTimer();
        addCountDownTimer();
    }

    /**
     * 点击暂停按钮
     */
    private void pressButtonPause() {
        if (videoView.isPlaying())
            videoView.pause();
        btnPause.setVisibility(View.INVISIBLE);
        btnPlay.setVisibility(View.VISIBLE);
        releaseCountDownTimer();
        addCountDownTimer();
    }

    /**
     * 点击屏幕，隐藏/显示控制面板
     */
    private void clickScreen() {
        if (control_below.getVisibility() == View.VISIBLE) {
            control_below.setVisibility(View.INVISIBLE);
            control_up.setVisibility(View.INVISIBLE);
            releaseCountDownTimer();
        } else {
            control_below.setVisibility(View.VISIBLE);
            control_up.setVisibility(View.VISIBLE);
            addCountDownTimer();
        }
    }

    /**
     * 横竖屏切换
     */
    private void changeScreenDirection() {
        // 保存现场
        saveTheSite();

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.activity_video_landscape);  // 切换为横屏布局
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);   // 隐藏状态栏
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            setContentView(R.layout.activity_video);            // 切换为竖屏布局
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 显示状态栏
        }
        // 恢复现场：重新获取并设置布局控件，从原来位置继续播放
        findAndSetView();
        beginPlay(previousProgress);
    }

    /**
     * 用户点击选择视频按钮，暂停当前播放视频，并打开手机默认文件管理器
     */
    private void pressButtonChooser() {
        isChooserClicked = true;
        saveTheSite();
        // 打开系统文件管理器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    /**
     * 保存现场
     * 暂停原视频，释放定时器，保存原视频播放位置
     */
    private void saveTheSite() {
        previousProgress = videoView.getCurrentPosition();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        releaseCountDownTimer();
        if (videoView.isPlaying())
            videoView.pause();
    }

    /**
     * 恢复现场
     * 若保存现场时视频已播放完毕，则从头开始播放
     */
    private void recoverTheSite() {
        addTimer();
        addCountDownTimer();
        btnPlay.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        if (!isVideoCompleted)
            videoView.seekTo(previousProgress);
        else
            isVideoCompleted = false;
        videoView.start();
    }

    /**
     * 控制面板出现4秒后消失
     */
    private void addCountDownTimer() {
        countDownTimer = new CountDownTimer(4000, 4000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (control_below.getVisibility() == View.VISIBLE) {
                    control_below.setVisibility(View.INVISIBLE);
                    control_up.setVisibility(View.INVISIBLE);
                }
            }
        };
        countDownTimer.start();
    }

    private void releaseCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * 添加一个计时器，定时获取视频播放进度，为进度条服务
     */
    private void addTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 传递视频总时长 当前播放时间
                int duration = videoView.getDuration();
                int curPosition = videoView.getCurrentPosition();
                Message msg = VideoActivity.handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("duration", duration);
                bundle.putInt("curPosition", curPosition);
                msg.setData(bundle);

                // 交给VideoActivity的handler处理
                VideoActivity.handler.sendMessage(msg);
            }
        };
        timer.schedule(task, 5, 50);
    }

    static class MyHandler extends Handler {
        MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int curPosition = bundle.getInt("curPosition");
            seekBar.setMax(duration);
            seekBar.setProgress(curPosition);

            // 打印歌曲总时间与当前播放时间
            int minTotal = duration / 1000 / 60;
            int secTotal = duration / 1000 % 60;
            int minCur = curPosition / 1000 / 60;
            int secCur = curPosition / 1000 % 60;
            String text = toStr(minCur) + ":" + toStr(secCur) + "/" + toStr(minTotal) + ":" + toStr(secTotal);
            textTime.setText(text);
        }
    }

    /**
     * 时间转换为文本，若小于10需要在前面加0
     *
     * @param time 要被转换的时间数
     */
    private static String toStr(int time) {
        String str = String.valueOf(time);
        if (time < 10)
            str = "0" + str;
        return str;
    }
}
