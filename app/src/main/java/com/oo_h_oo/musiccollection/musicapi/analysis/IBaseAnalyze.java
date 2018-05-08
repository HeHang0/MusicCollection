package com.oo_h_oo.musiccollection.musicapi.analysis;

import com.oo_h_oo.musiccollection.musicapi.returnhelper.*;
import com.oo_h_oo.musiccollection.musicmanage.*;

import java.util.List;

public interface IBaseAnalyze {
    PlayListAndCount getPlayList(int offset);
    List<Music> getRankingMusicList(RankingListType listType);
    MusicListAndPlayListDetail getPlayListItems(String url);
    MusicListAndCount getSearchMusicList(String searchStr, int offset);
    void getMusicDetail(Music music);
}
