package com.oo_h_oo.musiccollection.musicapi;

import com.oo_h_oo.musiccollection.musicapi.analysis.CloudMusicAnalyze;
import com.oo_h_oo.musiccollection.musicapi.analysis.QQMusicAnalyze;
import com.oo_h_oo.musiccollection.musicapi.analysis.XiaMiMusicAnalyze;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.MusicListAndCount;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.MusicListAndPlayListDetail;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.PlayListAndCount;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.musicmanage.RankingListType;

import java.util.ArrayList;
import java.util.List;


public class NetMusicHelper {
    public static PlayListAndCount getPlayList(int offset, NetMusicType type) {
        PlayListAndCount playListAndCount;
        switch (type){
            case QQMusic:
                playListAndCount = QQMusicAnalyze.getInstance().getPlayList(offset);
                break;
            case CloudMusix:
                playListAndCount = CloudMusicAnalyze.getInstance().getPlayList(offset);;
                break;
            case XiaMiMusic:
                playListAndCount = XiaMiMusicAnalyze.getInstance().getPlayList(offset);
                break;
            default:
                playListAndCount = new PlayListAndCount(new ArrayList<Playlist>(),1);
        }
        return playListAndCount;
    }

    public static List<Music> getRankingMusicList(RankingListType listType, NetMusicType type){
        List<Music> list;
        switch (type){
            case QQMusic:
                list = QQMusicAnalyze.getInstance().getRankingMusicList(listType);
                break;
            case CloudMusix:
                list = CloudMusicAnalyze.getInstance().getRankingMusicList(listType);
                if (list.size() == 0) list = CloudMusicAnalyze.getInstance().getRankingMusicList(listType);
                break;
            case XiaMiMusic:
                list = XiaMiMusicAnalyze.getInstance().getRankingMusicList(listType);
                break;
            default:
                list = new ArrayList<>();
        }
        return list;
    }

    public static MusicListAndPlayListDetail getPlayListItems(String url, NetMusicType type){

        MusicListAndPlayListDetail musicListAndPlayListDetail;
        switch (type){
            case QQMusic:
                musicListAndPlayListDetail = QQMusicAnalyze.getInstance().getPlayListItems(url);
                break;
            case CloudMusix:
                musicListAndPlayListDetail = CloudMusicAnalyze.getInstance().getPlayListItems(url);
                if (musicListAndPlayListDetail.getList().size() == 0)
                    musicListAndPlayListDetail = CloudMusicAnalyze.getInstance().getPlayListItems(url);
                break;
            case XiaMiMusic:
                musicListAndPlayListDetail = XiaMiMusicAnalyze.getInstance().getPlayListItems(url);
                break;
            default:
                musicListAndPlayListDetail = new MusicListAndPlayListDetail(new ArrayList<Music>(),"","");
        }
        return musicListAndPlayListDetail;
    }

    public static void getMusicDetail(Music music){

        switch (music.getOrigin()){
            case QQMusic:
                QQMusicAnalyze.getInstance().getMusicDetail(music);
                break;
            case CloudMusix:
                CloudMusicAnalyze.getInstance().getMusicDetail(music);
                break;
            case XiaMiMusic:
                XiaMiMusicAnalyze.getInstance().getMusicDetail(music);
                break;
        }
    }

    public static MusicListAndCount getSearchMusicList(String searchStr, int offset, NetMusicType type){
        MusicListAndCount musicListAndCount;
        switch (type){
            case XiaMiMusic:
                musicListAndCount = XiaMiMusicAnalyze.getInstance().getSearchMusicList(searchStr,offset);
                break;
            case CloudMusix:
                musicListAndCount = CloudMusicAnalyze.getInstance().getSearchMusicList(searchStr,offset);
                break;
            case QQMusic:
                musicListAndCount = QQMusicAnalyze.getInstance().getSearchMusicList(searchStr,offset);
                break;
                default:
                    musicListAndCount = new MusicListAndCount(new ArrayList<Music>(), 0);
        }
        return musicListAndCount;
    }
}
