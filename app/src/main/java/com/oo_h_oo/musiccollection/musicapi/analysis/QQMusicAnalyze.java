package com.oo_h_oo.musiccollection.musicapi.analysis;

import android.annotation.SuppressLint;

import com.oo_h_oo.musiccollection.musicapi.Tools;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.PlayListAndCount;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.musicmanage.RankingListType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QQMusicAnalyze implements BaseAnalyze {
    public static QQMusicAnalyze analyze = new QQMusicAnalyze();

    private static String playListHotAPI = "https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg?rnd=0.4781484879517406&g_tk=732560869&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&categoryId=10000000&sortId=5&sin=%s&ein=%s";
    @SuppressLint("DefaultLocale")
    private static String hotListAPI = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?tpl=3&page=detail&date=" + Calendar.getInstance().get(Calendar.YEAR) + "_" +  String.format("%2d",(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)-2)) + "&topid=26&type=top&song_begin=0&song_num=100&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
    @SuppressLint("SimpleDateFormat")
    private static String newSongListAPI = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?tpl=3&page=detail&date=" + new SimpleDateFormat("yyyy-MM-dd").format(Tools.addDate(new Date(), Calendar.DAY_OF_MONTH,(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10 ? -2 : -1))) + "&topid=27&type=top&song_begin=0&song_num=100&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
    @SuppressLint("SimpleDateFormat")
    private static String soarListAPI = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?tpl=3&page=detail&date=" + new SimpleDateFormat("yyyy-MM-dd").format(Tools.addDate(new Date(), Calendar.DAY_OF_MONTH,(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10 ? -2 : -1))) + "&topid=4&type=top&song_begin=0&song_num=100&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";

    @Override
    public PlayListAndCount GetPlayList(int offset) {
        List<Playlist> list = new ArrayList<>();
        int count = 0;
        String retStr = Tools.sendDataByGet(String.format(playListHotAPI, offset*30, offset * 30+29));
        retStr = retStr.substring(0,retStr.length() - 1).replace("MusicJsonCallback(", "");
        try {
            JSONObject jsonObject = new JSONObject(retStr).getJSONObject("data");
            JSONArray streams = (JSONArray) jsonObject.get("list");
            int sum = jsonObject.getInt("sum");
            count = sum / 30 + (sum % 30 == 0 ? 0 : 1);
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    String title = stream.getString("dissname");
                    String imgUrl = stream.getString("imgurl").replace("600?n=1", "150?n=1").replace("http://","https://");
                    String url = stream.getString("dissid");
                    Playlist pl = new Playlist(title,imgUrl,url);
                    list.add(pl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new PlayListAndCount(list,count);
    }

    @Override
    public List<Music> getRankingMusicList(RankingListType listType) {
        List<Music> list = new ArrayList<>();
        int count = 0;
        String path = "";
        switch (listType){
            case HotList:
                path = hotListAPI; break;
            case SoarList:
                path = soarListAPI; break;
            case NewSongList:
                path = newSongListAPI; break;
        }
        String retStr = Tools.sendDataByGet(path);
        try {
            JSONArray streams = new JSONObject(retStr).getJSONArray("songlist");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i).getJSONObject("data");
                    Music music = new Music();
                    music.setAlbum(stream.getString("albumname"));
                    String albumID = stream.getString("albummid");
                    String albumSS = String.valueOf(albumID.charAt(albumID.length()-2)) + "/" + String.valueOf(albumID.charAt(albumID.length()-1)) + "/" + albumID;
                    music.setAlbumImageUrl("http://imgcache.qq.com/music/photo/mid_album_300/" + albumSS + ".jpg");
                    music.setDuration(Long.valueOf(stream.getString("size128"))/16);
                    music.setOrigin(NetMusicType.QQMusic);
                    music.setSize(Long.valueOf(stream.getString("size128"))/(1024*1024) + "Mb");
                    music.setSinger(stream.getJSONArray("singer").getJSONObject(0).getString("name"));
                    music.setTitle(stream.getString("songname"));

                    list.add(music);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
