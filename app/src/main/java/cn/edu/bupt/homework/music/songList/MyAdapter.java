package cn.edu.bupt.homework.music.songList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.edu.bupt.homework.R;

/**
 * The adapter of the ListView of songs
 */
public class MyAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return SongInformation.songInfo.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        myViewHolder holder;
        // 获取position位置的歌曲信息
        if (convertView == null) {
            holder = new myViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.song_item_list, null);
            holder.songName = convertView.findViewById(R.id.song_info_text);
            holder.singerPicture = convertView.findViewById(R.id.singer_image_view);
            convertView.setTag(holder);
        } else
            holder = (myViewHolder) convertView.getTag();

        // ListView中展示歌手照片和歌曲名，根据position从SongInformation获取
        holder.singerPicture.setImageResource(SongInformation.getSingerPic(position));
        holder.songName.setText(SongInformation.songInfo[position]);
        return convertView;
    }

    private static class myViewHolder {
        private TextView songName;
        private ImageView singerPicture;
    }
}
