package com.oo_h_oo.musiccollection.musicapi.analysis;

import com.oo_h_oo.musiccollection.musicapi.Tools;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.*;
import com.oo_h_oo.musiccollection.musicmanage.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CloudMusicAnalyze implements IBaseAnalyze {
    private static class CloudMusicAnalyzeHolder{
        private static CloudMusicAnalyze instance=new CloudMusicAnalyze();
    }
    public static CloudMusicAnalyze getInstance(){
        return CloudMusicAnalyzeHolder.instance;
    }

    private static String encSecKey = "&encSecKey=2d48fd9fb8e58bc9c1f14a7bda1b8e49a3520a67a2300a1f73766caee29f2411c5350bceb15ed196ca963d6a6d0b61f3734f0a0f4a172ad853f16dd06018bc5ca8fb640eaa8decd1cd41f66e166cea7a3023bd63960e656ec97751cfc7ce08d943928e9db9b35400ff3d138bda1ab511a06fbee75585191cabe0e6e63f7350d6";

    private static String playListDetailAPI = "http://music.163.com/weapi/v3/playlist/detail?csrf_token=";
    private static String lyricAPI = "http://music.163.com/api/song/lyric?os=pc&id=%s&lv=-1&kv=-1&tv=-1";

    private static String playListHotAPI = "http://music.163.com/discover/playlist/?order=hot&limit=30&offset=%s";
    private Map<RankingListType,String> rankinListAPI = new HashMap<>();

    private CloudMusicAnalyze(){
        rankinListAPI.put(RankingListType.HotList,"3778678");
        rankinListAPI.put(RankingListType.NewSongList,"3779629");
        rankinListAPI.put(RankingListType.SoarList,"19723756");
    }

    @Override
    public PlayListAndCount getPlayList(int offset) {
        List<Playlist> list = new ArrayList<>();
        int count = 0;
//        String retStr = Tools.sendDataByGet(String.format(playListHotAPI, offset*30));
        try {
            Document doc = Jsoup.connect(String.format(playListHotAPI, offset*30)).get();
            Elements links = doc.select("ul#m-pl-container li");
            String sumStr = doc.select("a.zpgi").last().html();
            count = Integer.valueOf(sumStr);
            for ( Element link: links) {
                Element el = link.selectFirst("a.msk");
                String name = el.attr("title");
                String imgUrl = link.selectFirst("img.j-flag").attr("src");
                String url = "https://music.163.com" + el.attr("href");
                list.add(new Playlist(name, imgUrl, url));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PlayListAndCount(list,count);
    }

    @Override
    public List<Music> getRankingMusicList(RankingListType listType) {
        List<Music> list = new ArrayList<>();
        String id =  Pattern.matches("^[\\d]+$", rankinListAPI.get(listType)) ? rankinListAPI.get(listType) : Tools.getStrWithRegular("playlist\\?id=([\\d]+)",rankinListAPI.get(listType));
        String param = Tools.aesEncrypt("{\"id\":\"" + id + "\",\"offset\":0,\"total\":true,\"limit\":1000,\"n\":1000,\"csrf_token\":\"\"}", "0CoJUm6Qyw8W8jud");
        param = Tools.aesEncrypt(param, "a8LWv2uAtXjzSfkQ");
        try {
            param = java.net.URLEncoder.encode(param,   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String paramData = "params=" + param + encSecKey;
        String url = playListDetailAPI + "&" + paramData;

        String retStr = Tools.sendDataByPost(url);
        try {
            JSONArray streams = new JSONObject(retStr).getJSONObject("playlist").getJSONArray("tracks");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setAlbum(stream.getJSONObject("al").getString("name"));
                    music.setAlbumImageUrl(stream.getJSONObject("al").getString("picUrl"));
                    music.setDuration(Long.valueOf(stream.getString("dt")));
                    music.setOrigin(NetMusicType.CloudMusix);
                    music.setSize(Long.valueOf(stream.getString("dt"))*16/(1024*1024) + "Mb");
                    music.setSinger(stream.getJSONArray("ar").getJSONObject(0).getString("name"));
                    music.setTitle(stream.getString("name"));
                    music.setMusicID(stream.getString("id"));

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
    public MusicListAndPlayListDetail getPlayListItems(String id) {
        if (!Pattern.matches("^[\\d]+$",id))
            id = Tools.getStrWithRegular("playlist\\?id=([\\d]+)",id);
        String param = Tools.aesEncrypt("{\"id\":\"" + id + "\",\"offset\":0,\"total\":true,\"limit\":1000,\"n\":1000,\"csrf_token\":\"\"}", "0CoJUm6Qyw8W8jud");
        param = Tools.aesEncrypt(param, "a8LWv2uAtXjzSfkQ");
        try {
            param = java.net.URLEncoder.encode(param,   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String paramData = "params=" + param + encSecKey;
        String url = playListDetailAPI + "&" + paramData;

        String retStr = Tools.sendDataByPost(url);
        List<Music> list = new ArrayList<>();
        String name = ""; String imgUrl = "";
        try {
            JSONObject ja = new JSONObject(retStr).getJSONObject("playlist");
            name = ja.getString("name");
            imgUrl = ja.getString("coverImgUrl");
            JSONArray streams = ja.getJSONArray("tracks");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setAlbum(stream.getJSONObject("al").getString("name"));
                    music.setAlbumImageUrl(stream.getJSONObject("al").getString("picUrl"));
                    music.setDuration(Long.valueOf(stream.getString("dt")));
                    music.setOrigin(NetMusicType.CloudMusix);
                    music.setSize(Long.valueOf(stream.getString("dt"))*16/(1024*1024) + "Mb");
                    music.setSinger(stream.getJSONArray("ar").getJSONObject(0).getString("name"));
                    music.setTitle(stream.getString("name"));
                    music.setMusicID(stream.getString("id"));
                    list.add(music);
                } catch (JSONException e) {
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
        String param = Tools.aesEncrypt("{\"ids\":[" + music.getMusicID() + "],\"br\":320000,\"csrf_token\":\"\"}", "0CoJUm6Qyw8W8jud");
        param = Tools.aesEncrypt(param, "a8LWv2uAtXjzSfkQ");
        try {
            param = java.net.URLEncoder.encode(param,   "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String paramData = "params=" + param + encSecKey;
        String url = "http://music.163.com/weapi/song/enhance/player/url?csrf_token=";
        url = url + "&" + paramData;

        String retStr = Tools.sendDataByPost(url);
        try {
            JSONObject ja = new JSONObject(retStr).getJSONArray("data").getJSONObject(0);
            music.setPath(ja.getString("url"));
            music.setSize(ja.getLong("size")*1.0/(1024*1024) + "Mb");
            music.setBitRate(ja.getInt("br")/1000 + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        retStr = Tools.sendDataByGet(String.format(lyricAPI, music.getMusicID()));

    }
}
