package cn.edu.bupt.homework.music;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import cn.edu.bupt.homework.R;
import cn.edu.bupt.homework.music.songList.SongInformation;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener {

    @SuppressLint("StaticFieldLeak")
    private static SeekBar seekBar;     // 进度条
    @SuppressLint("StaticFieldLeak")
    private static TextView curTime;    // 当前播放时间
    @SuppressLint("StaticFieldLeak")
    private static TextView totalTime;  // 音乐总时间

    private ObjectAnimator rotate;      // 旋转动画
    private ImageView round;            // 歌手照片
    private TextView content;           // 歌曲信息
    private Button btnPlay;             // 播放按钮
    private Button btnPause;            // 暂停按钮
    private int curPosition;            // 当前播放歌曲在ListView中的位置

    private MyServiceCon con;
    private MusicService.MusicBinder musicBinder;
    public static Handler handler = new MyHandler();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        setTitle("Enjoy the music");

        // 获取ListView传来的歌曲参数
        Intent intent = getIntent();
        int picture = intent.getIntExtra("picture", R.drawable.xs);
        curPosition = intent.getIntExtra("position", 0);

        round = findViewById(R.id.round);
        content = findViewById(R.id.music_info);
        round.setImageResource(picture);
        content.setText(SongInformation.songInfo[curPosition]);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onDestroy() {
        rotate.pause();
        rotate.cancel();
        musicBinder.stop();
        unbindService(con);
        super.onDestroy();
    }

    private void init() {
        curTime = findViewById(R.id.progress);
        totalTime = findViewById(R.id.total);
        seekBar = findViewById(R.id.seekbar);
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        findViewById(R.id.last).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);

        // 绑定音乐播放服务
        Intent intent = new Intent(this, MusicService.class);
        con = new MyServiceCon();
        bindService(intent, con, BIND_AUTO_CREATE);

        setSeekBar();
        setAnimation();
    }

    /**
     * 设置进度条
     */
    private void setSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 放完后自动播放下一首
                if (progress != 0 && progress == seekBar.getMax()) {
                    curPosition = (curPosition + 1) % SongInformation.total;
                    if (rotate.isPaused())
                        rotate.resume();
                    changeMusic();
                    btnPlay.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 开始播放后拖动进度条
                if (musicBinder.getPlayer() != null) {
                    int pro = seekBar.getProgress();
                    musicBinder.seekTo(pro);
                    // 暂停时拖动进度条
                    if (musicBinder.isPlayerPaused()) {
                        musicBinder.continuePlay();
                        if (rotate.isPaused())
                            rotate.resume();
                        btnPlay.setVisibility(View.INVISIBLE);
                        btnPause.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * 设置旋转动画
     */
    private void setAnimation() {
        rotate = ObjectAnimator.ofFloat(findViewById(R.id.round), "rotation", 0, 360);
        rotate.setRepeatCount(ValueAnimator.INFINITE);
        rotate.setRepeatMode(ValueAnimator.RESTART);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setDuration(8000);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                // 首次点击
                if (musicBinder.getPlayer() == null) {
                    int song = SongInformation.songs[curPosition];
                    musicBinder.play(song);
                    rotate.start();
                }
                // 暂停后点击
                else {
                    musicBinder.continuePlay();
                    rotate.resume();
                }
                btnPlay.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_pause:
                musicBinder.pause();
                rotate.pause();
                btnPause.setVisibility(View.INVISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
                break;
            case R.id.last:
                int count = SongInformation.total;
                curPosition = (curPosition - 1 + count) % count;
                changeMusic();
                break;
            case R.id.next:
                curPosition = (curPosition + 1) % SongInformation.total;
                changeMusic();
                break;
        }
    }

    /**
     * 打印进度条显示的时间
     *
     * @param min  分钟数
     * @param sec  秒数
     * @param type 打印的时间类型
     *             "total"-->歌曲总时间
     *             "current"-->当前播放时间
     */
    @SuppressLint("SetTextI18n")
    private static void printTime(int min, int sec, String type) {
        String strMin = Integer.toString(min);
        String strSec = Integer.toString(sec);
        if (min < 10)
            strMin = "0" + strMin;
        if (sec < 10)
            strSec = "0" + strSec;
        if (type.equals("total"))
            totalTime.setText(strMin + ":" + strSec);
        else if (type.equals("current"))
            curTime.setText(strMin + ":" + strSec);
        else
            Log.e("ParameterError", "A wrong parameter " + type + " to printTime()");
    }

    /**
     * 换歌，下一首歌的位置是curPosition
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void changeMusic() {
        round.setImageResource(SongInformation.getSingerPic(curPosition));
        if (rotate.isPaused())
            rotate.resume();
        content.setText(SongInformation.songInfo[curPosition]);
        musicBinder.stop();
        musicBinder.play(SongInformation.songs[curPosition]);
        btnPlay.setVisibility(View.INVISIBLE);
        btnPause.setVisibility(View.VISIBLE);
    }

    /**
     * 处理服务传来的进度消息
     */
    static class MyHandler extends Handler {
        MyHandler() {
            super();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            // 根据获取的信息更新进度条
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
            printTime(minTotal, secTotal, "total");
            printTime(minCur, secCur, "current");
        }
    }

    /**
     * 服务连接类，用于Activity连接Service
     */
    class MyServiceCon implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicBinder = (MusicService.MusicBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
}
