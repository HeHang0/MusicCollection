package com.oo_h_oo.musiccollection.musicapi.returnhelper;

import com.oo_h_oo.musiccollection.musicmanage.Playlist;

import java.util.List;

public class PlayListAndCount{
    public PlayListAndCount(List<Playlist> list, int count){
        this.list = list;
        this.count = count;
    }
    private List<Playlist> list;
    private int count;

    public List<Playlist> getList() {
        return list;
    }

    public int getCount() {
        return count;
    }
}
