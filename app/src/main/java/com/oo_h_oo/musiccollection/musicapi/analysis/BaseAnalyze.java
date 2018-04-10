package com.oo_h_oo.musiccollection.musicapi.analysis;

import com.oo_h_oo.musiccollection.musicapi.returnhelper.PlayListAndCount;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.musicmanage.RankingListType;

import java.util.List;

public interface BaseAnalyze {
    PlayListAndCount GetPlayList(int offset);
    List<Music> getRankingMusicList(RankingListType listType);
}
