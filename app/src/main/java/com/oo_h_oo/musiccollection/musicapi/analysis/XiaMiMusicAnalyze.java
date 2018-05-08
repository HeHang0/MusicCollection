package com.oo_h_oo.musiccollection.musicapi.analysis;

import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class XiaMiMusicAnalyze implements IBaseAnalyze {
    private static class XiaMiMusicAnalyzeHolder{
        private static XiaMiMusicAnalyze instance=new XiaMiMusicAnalyze();
    }
    public static XiaMiMusicAnalyze getInstance(){
        return XiaMiMusicAnalyzeHolder.instance;
    }

    private static String playListHotAPI = "http://www.xiami.com/collect/recommend/page/%s";
    private static String playListDetailAPI = "http://api.xiami.com/web?v=2.0&app_key=1&id=%s&r=collect/detail";
    private static String musicDetailAPI = "http://api.xiami.com/web?v=2.0&app_key=1&id=%s&r=song/detail";
    private static String searchAPI = "http://api.xiami.com/web?v=2.0&app_key=1&key=%s&page=%s&limit=30&r=search/songs";

    private Map<RankingListType,String> rankinListAPI = new HashMap<>();
    private XiaMiMusicAnalyze(){
        rankinListAPI.put(RankingListType.HotList,"http://api.xiami.com/web?v=2.0&app_key=1&id=101&page=1&limit=100&r=rank/song-list");
        rankinListAPI.put(RankingListType.NewSongList,"http://api.xiami.com/web?v=2.0&app_key=1&id=102&page=1&limit=100&r=rank/song-list");
        rankinListAPI.put(RankingListType.SoarList,"http://api.xiami.com/web?v=2.0&app_key=1&id=103&page=1&limit=100&r=rank/song-list");
    }

    @Override
    public PlayListAndCount getPlayList(int offset) {
        List<Playlist> list = new ArrayList<>();
        int count = 0;
//        String retStr = Tools.sendDataByGet(String.format(playListHotAPI, offset+1));
        try {
            Document doc = Jsoup.connect(String.format(playListHotAPI, offset+1)).get();
            Elements links = doc.select("div.block_list.clearfix ul li");
            String sumStr = doc.selectFirst("div.all_page span").html();
            sumStr = Tools.getStrWithRegular("共([\\d]+)条",sumStr);
            count = Integer.valueOf(sumStr) / 30 + (Integer.valueOf(sumStr) % 30 == 0 ? 0 : 1);
            for ( Element link: links) {
                Element el = link.selectFirst("div.block_cover a");
                String name = el.attr("title");
                String imgUrl = el.selectFirst("img").attr("src");
                String url = Tools.getStrWithRegular("/collect/([\\d]+)",el.attr("href"));
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
        int count = 0;
        String retStr = Tools.sendDataByGet(rankinListAPI.get(listType));
        try {
            JSONArray streams = new JSONObject(retStr).getJSONArray("data");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setSinger(stream.getString("singers"));
                    music.setTitle(stream.getString("song_name"));
                    music.setMusicID(stream.getString("song_id"));
                    music.setOrigin(NetMusicType.XiaMiMusic);
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
        List<Music> list = new ArrayList<>();
        String name = ""; String imgUrl = "";
        try {
            JSONObject ja = new JSONObject(retStr).getJSONObject("data");
            name = ja.getString("collect_name");
            imgUrl = ja.getString("logo");
            JSONArray streams = ja.getJSONArray("songs");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setTitle(stream.getString("song_name"));
                    music.setSinger(stream.getString("singers"));
                    music.setMusicID(stream.getString("song_id"));
                    music.setAlbum(stream.getString("album_name"));
                    music.setAlbumImageUrl(stream.getString("album_logo"));
                    music.setPath(stream.getString("listen_file"));
                    music.setDuration(Long.valueOf(stream.getString("length")));
                    music.setOrigin(NetMusicType.XiaMiMusic);
                    music.setSize(Long.valueOf(stream.getString("length"))*16/(1024*1024) + "Mb");
                    music.setLyricPath(stream.getString("lyric"));
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
        String retStr = Tools.sendDataByGet(String.format(musicDetailAPI, music.getMusicID()));
        try {
            JSONObject ja = new JSONObject(retStr).getJSONObject("data").getJSONObject("song");
            music.setAlbum(ja.getString("album_name"));
            music.setAlbumImageUrl(ja.getString("logo"));
            music.setPath(ja.getString("listen_file"));
            music.setLyricPath(ja.getString("lyric"));
            String secondstr = Tools.getStrWithRegular("/([\\d]{2,4})/", music.getLyricPath());
            if (secondstr.length() < 1) secondstr = "0";
            music.setDuration(Long.valueOf(secondstr));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MusicListAndCount getSearchMusicList(String searchStr, int offset) {
        String retStr = Tools.sendDataByGet(String.format(searchAPI, searchStr, offset+1));
        List<Music> list = new ArrayList<>();
        int count = 0;
        try {
            JSONObject ja = new JSONObject(retStr).getJSONObject("data");
            count = ja.getInt("total");
            JSONArray streams = ja.getJSONArray("songs");
            for (int i = 0; i < streams.length(); i++){
                try {
                    JSONObject stream = streams.getJSONObject(i);
                    Music music = new Music();
                    music.setTitle(stream.getString("song_name"));
                    music.setSinger(stream.getString("artist_name"));
                    music.setMusicID(stream.getString("song_id"));
                    music.setAlbum(stream.getString("album_name"));
                    music.setAlbumImageUrl(stream.getString("album_logo"));
                    music.setPath(stream.getString("listen_file"));
//                    music.setDuration(Long.valueOf(stream.getString("length")));
                    music.setOrigin(NetMusicType.XiaMiMusic);
//                    music.setSize(Long.valueOf(stream.getString("length"))*16/(1024*1024) + "Mb");
                    music.setLyricPath(stream.getString("lyric"));
                    list.add(music);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new MusicListAndCount(list, count);
    }
}
