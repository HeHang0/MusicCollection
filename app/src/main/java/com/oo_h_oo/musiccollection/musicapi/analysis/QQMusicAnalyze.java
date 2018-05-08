package com.oo_h_oo.musiccollection.musicapi.analysis;

import android.annotation.SuppressLint;

import com.oo_h_oo.musiccollection.musicapi.Tools;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.*;
import com.oo_h_oo.musiccollection.musicmanage.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QQMusicAnalyze implements IBaseAnalyze {
    private static class QQMusicAnalyzeHolder{
        private static QQMusicAnalyze instance=new QQMusicAnalyze();
    }
    public static QQMusicAnalyze getInstance(){
        return QQMusicAnalyzeHolder.instance;
    }

    private static String searchAPI = "http://i.y.qq.com/s.music/fcgi-bin/search_for_qq_cp?g_tk=938407465&uin=0&format=jsonp&inCharset=utf-8&outCharset=utf-8&notice=0&platform=h5&needNewCode=1&w=%s&zhidaqu=1&catZhida=1&t=0&flag=1&ie=utf-8&sem=1&aggr=0&perpage=30&n=30&p=%s&remoteplace=txt.mqq.all&_=1459991037831";
    private static String playListHotAPI = "https://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg?rnd=0.4781484879517406&g_tk=732560869&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&categoryId=10000000&sortId=5&sin=%s&ein=%s";
    private static String playListDetailAPI = "https://i.y.qq.com/qzone-music/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg?type=1&json=1&utf8=1&onlysong=0&nosign=1&disstid=%s&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
    private static String lyricAPI = "https://i.y.qq.com/lyric/fcgi-bin/fcg_query_lyric.fcg?songmid=%s&loginUin=0&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
    @SuppressLint("DefaultLocale,SimpleDateFormat")
    private Map<RankingListType,String> rankinListAPI = new HashMap<>();

    private QQMusicAnalyze(){
        rankinListAPI.put(RankingListType.HotList,"https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?tpl=3&page=detail&date=" + Calendar.getInstance().get(Calendar.YEAR) + "_" +  String.format("%2d",(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)-2)) + "&topid=26&type=top&song_begin=0&song_num=100&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0");
        rankinListAPI.put(RankingListType.NewSongList,"https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?tpl=3&page=detail&date=" + new SimpleDateFormat("yyyy-MM-dd").format(Tools.addDate(new Date(), Calendar.DAY_OF_MONTH,(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10 ? -2 : -1))) + "&topid=27&type=top&song_begin=0&song_num=100&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0");
        rankinListAPI.put(RankingListType.SoarList,"https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?tpl=3&page=detail&date=" + new SimpleDateFormat("yyyy-MM-dd").format(Tools.addDate(new Date(), Calendar.DAY_OF_MONTH,(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 10 ? -2 : -1))) + "&topid=4&type=top&song_begin=0&song_num=100&g_tk=5381&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0");
    }

    @Override
    public PlayListAndCount getPlayList(int offset) {
        List<Playlist> list = new ArrayList<>();
        int count = 0;
        try {
            String retStr = Tools.sendDataByGet(String.format(playListHotAPI, offset*30, offset * 30+29));
            retStr = retStr.substring(0,retStr.length() - 1).replace("MusicJsonCallback(", "");
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
        String retStr = Tools.sendDataByGet(rankinListAPI.get(listType));
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
                    music.setMusicID(stream.getString("songmid"));

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

    @Override
    public MusicListAndPlayListDetail getPlayListItems(String ssid) {
        if (Pattern.matches("[a-zA-z]+://[^\\s]*",ssid))
            ssid = Tools.getStrWithRegular("/([\\d]+)",ssid);
        String retStr = Tools.sendDataByGet(String.format(playListDetailAPI, ssid));
        retStr = retStr.substring(0,retStr.length() - 1).replace("jsonCallback(", "");
        List<Music> list = new ArrayList<>();
        String name = ""; String imgUrl = "";
        try {
            JSONObject ja = new JSONObject(retStr).getJSONArray("cdlist").getJSONObject(0);
            name = ja.getString("dissname");
            imgUrl = ja.getString("logo").replace("300?n=1", "150?n=1");
            JSONArray streams = ja.getJSONArray("songlist");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setAlbum(stream.getString("albumname"));
                    String albumID = stream.getString("albummid");
                    if(albumID.length() > 2){
                        String albumSS = String.valueOf(albumID.charAt(albumID.length()-2)) + "/" + String.valueOf(albumID.charAt(albumID.length()-1)) + "/" + albumID;
                        music.setAlbumImageUrl("http://imgcache.qq.com/music/photo/mid_album_300/" + albumSS + ".jpg");
                    }
                    music.setDuration(Long.valueOf(stream.getString("size128"))/16);
                    music.setOrigin(NetMusicType.QQMusic);
                    music.setSize(Long.valueOf(stream.getString("size128"))*1.0/(1024*1024) + "Mb");
                    music.setSinger(stream.getJSONArray("singer").getJSONObject(0).getString("name"));
                    music.setTitle(stream.getString("songname"));
                    music.setMusicID(stream.getString("songmid"));

                    list.add(music);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new MusicListAndPlayListDetail(list, name, imgUrl);
    }

    @Override
    public void getMusicDetail(Music music) {
        String retStr = Tools.sendDataByGet("http://base.music.qq.com/fcgi-bin/fcg_musicexpress.fcg?json=3&guid=780782017&g_tk=938407465&loginUin=0&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=GB2312&notice=0&platform=yqq&needNewCode=0");
        retStr = retStr.replace("jsonCallback(", "").replace(");", "");
        try {
            String token = new JSONObject(retStr).getString("key");
            music.setPath("http://dl.stream.qqmusic.qq.com/C200" + music.getMusicID() + ".m4a?vkey=" + token + "&fromtag=0&guid=780782017");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MusicListAndCount getSearchMusicList(String searchStr, int offset) {
        String retStr = Tools.sendDataByGet(String.format(searchAPI, searchStr, offset+1));
        retStr = retStr.replaceFirst("^[a-zA-Z]{0,10}[C|c]allback\\(", "").replaceFirst(";$","").replaceFirst("\\)$","");
        List<Music> list = new ArrayList<>();
        int count = 0;
        try {
            JSONObject ja = new JSONObject(retStr).getJSONObject("data").getJSONObject("song");
            count = ja.getInt("totalnum");
            JSONArray streams = ja.getJSONArray("list");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setAlbum(stream.getString("albumname"));
                    String albumID = stream.getString("albummid");
                    if(albumID.length() > 2){
                        String albumSS = String.valueOf(albumID.charAt(albumID.length()-2)) + "/" + String.valueOf(albumID.charAt(albumID.length()-1)) + "/" + albumID;
                        music.setAlbumImageUrl("http://imgcache.qq.com/music/photo/mid_album_300/" + albumSS + ".jpg");
                    }
                    music.setDuration(Long.valueOf(stream.getString("size128"))/16);
                    music.setOrigin(NetMusicType.QQMusic);
                    music.setSize(Long.valueOf(stream.getString("size128"))*1.0/(1024*1024) + "Mb");
                    music.setSinger(stream.getJSONArray("singer").getJSONObject(0).getString("name"));
                    music.setTitle(stream.getString("songname"));
                    music.setMusicID(stream.getString("songmid"));

                    list.add(music);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new MusicListAndCount(list,count);
    }
}
