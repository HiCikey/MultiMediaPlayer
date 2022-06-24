package cn.edu.bupt.homework.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 音乐服务 运行在后台
 * 完成绑定媒体播放器，定时传送播放进度等任务
 */
public class MusicService extends Service {
    private MediaPlayer player;
    private Timer timer;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player == null)
            return;
        if (player.isPlaying())
            player.stop();
        player.release();
        player = null;
    }

    /**
     * 增设一个定时器，定时更新播放进度
     */
    private void addTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (player == null) return;
                // 使用消息传递音乐总时长、当前播放时间
                int duration = player.getDuration();
                int curPosition = player.getCurrentPosition();
                Message msg = MusicActivity.handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("duration", duration);
                bundle.putInt("curPosition", curPosition);
                msg.setData(bundle);

                // 交给MusicActivity的handler处理
                MusicActivity.handler.sendMessage(msg);
            }
        };
        timer.schedule(task, 5, 50);
    }

    class MusicBinder extends Binder {
        /**
         * 开始播放音乐
         */
        public void play(int song) {
            // 首次点击 开始 ，新建媒体播放器
            if (player == null)
                player = new MediaPlayer();
            player = MediaPlayer.create(getApplicationContext(), song);
            player.start();
            // 开始播放后新增定时器，传递播放进度
            if (timer == null)
                addTimer();
        }

        /**
         * 暂停播放音乐
         */
        public void pause() {
            if (player != null && player.isPlaying())
                player.pause();
        }

        /**
         * 继续播放音乐
         */
        public void continuePlay() {
            if (player != null && !player.isPlaying())
                player.start();
            if (timer == null)
                addTimer();
        }

        /**
         * 停止播放音乐
         */
        public void stop() {
            if (player != null) {
                timer.cancel();
                timer = null;
                player.stop();
                player.release();
                player = null;
            }
        }

        /**
         * 用户拖动进度条，修改音乐播放进度
         *
         * @param progress 主线程传来的当前进度
         */
        public void seekTo(int progress) {
            player.seekTo(progress);
        }

        public MediaPlayer getPlayer() {
            return player;
        }

        public void stopTimer() {
            timer.cancel();
            timer = null;
        }

        public boolean isPlayerPaused() {
            return !player.isPlaying();
        }
    }
}
