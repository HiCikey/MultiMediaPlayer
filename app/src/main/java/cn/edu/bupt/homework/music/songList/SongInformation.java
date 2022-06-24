package cn.edu.bupt.homework.music.songList;

import cn.edu.bupt.homework.R;

/**
 * The class which saves the information of all songs
 */
public class SongInformation {
    public static int total = 19;

    // 歌手照片
    public static int[] singerPic = {R.drawable.dzq, R.drawable.xs, R.drawable.xzq};

    // 歌曲信息 包括歌手和歌名
    public static String[] songInfo = {
            "邓紫棋 - 喜欢你", "邓紫棋 - 多远都要在一起", "邓紫棋 - 再见",
            "许嵩 - 山水之间", "许嵩 - 千百度", "许嵩 - 天龙八部之宿敌", "许嵩 - 断桥残雪", "许嵩 - 玫瑰花的葬礼", "许嵩 - 灰色头像", "许嵩 - 燕归巢", "许嵩 - 有何不可", "许嵩 - 素颜", "许嵩 - 惊鸿一面",
            "薛之谦 - 动物世界", "薛之谦 - 暧昧", "薛之谦 - 怪咖", "薛之谦 - 认真的雪", "薛之谦 - 演员", "薛之谦 - 天外来物"};

    // mp3文件
    public static int[] songs = {R.raw.xhn, R.raw.dydyzyq, R.raw.zj,
            R.raw.sszj, R.raw.qbd, R.raw.tlbbzsd, R.raw.dqcx, R.raw.mghdzl, R.raw.hstx, R.raw.ygc, R.raw.yhbk, R.raw.sy, R.raw.jhym,
            R.raw.dwsj, R.raw.am, R.raw.gk, R.raw.rzdx, R.raw.yy, R.raw.twlw};

    /**
     * 通过position获取歌手照片
     *
     * @param position 点击的歌曲位置
     * @return 歌曲对应歌手照片资源
     */
    public static int getSingerPic(int position) {
        if (position < 3)
            return singerPic[0];
        else if (position < 3 + 10)
            return singerPic[1];
        else
            return singerPic[2];
    }
}
