package com.oo_h_oo.musiccollection.musicapi;

import com.oo_h_oo.musiccollection.musicapi.analysis.BaseAnalyze;
import com.oo_h_oo.musiccollection.musicapi.analysis.QQMusicAnalyze;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.PlayListAndCount;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.musicmanage.RankingListType;

import java.util.ArrayList;
import java.util.List;


public class NetMusicHelper {
    public static PlayListAndCount GetPlayList(int offset, NetMusicType type) {
        PlayListAndCount playListAndCount;
        switch (type){
            case QQMusic:
                playListAndCount = QQMusicAnalyze.analyze.GetPlayList(offset);
                break;
            case CloudMusix:
                playListAndCount = new PlayListAndCount(new ArrayList<Playlist>(),0);
                break;
            case XiaMiMusic:
                playListAndCount = new PlayListAndCount(new ArrayList<Playlist>(),0);
                break;
            default:
                    playListAndCount = new PlayListAndCount(new ArrayList<Playlist>(),0);
        }
        return playListAndCount;
    }

    public static List<Music> getRankingMusicList(RankingListType listType, NetMusicType type){
        List<Music> list;
        switch (type){
            case QQMusic:
                list = QQMusicAnalyze.analyze.getRankingMusicList(listType);
                break;
            case CloudMusix:
                list = new ArrayList<>();
                break;
            case XiaMiMusic:
                list = new ArrayList<>();
                break;
            default:
                list = new ArrayList<>();
        }
        return list;
    }
}
