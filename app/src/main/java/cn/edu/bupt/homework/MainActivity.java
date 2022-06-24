package cn.edu.bupt.homework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import cn.edu.bupt.homework.music.songList.SongListViewActivity;
import cn.edu.bupt.homework.video.VideoActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.view_music_list).setOnClickListener(v ->
                startActivity(new Intent(this, SongListViewActivity.class)));
        findViewById(R.id.view_video).setOnClickListener(v ->
                startActivity(new Intent(this, VideoActivity.class)));
    }
}
