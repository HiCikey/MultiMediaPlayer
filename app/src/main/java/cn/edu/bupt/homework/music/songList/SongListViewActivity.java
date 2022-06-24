package cn.edu.bupt.homework.music.songList;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.edu.bupt.homework.R;
import cn.edu.bupt.homework.music.MusicActivity;

/**
 * Choose a music, and then play the music on MusicActivity
 */
public class SongListViewActivity extends AppCompatActivity {
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list_view);
        setTitle("Choose a music");

        listView = findViewById(R.id.song_list_view);
        listView.setAdapter(new MyAdapter());
        setItemClickEvent();
    }

    /**
     * 设置ListView项点击事件
     */
    private void setItemClickEvent() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 点击后，打开音乐播放界面，并将歌手照片和点击位置传过去
            Intent intent = new Intent(this, MusicActivity.class);
            intent.putExtra("picture", SongInformation.getSingerPic(position));
            intent.putExtra("position", position);
            startActivity(intent);
        });
    }
}
