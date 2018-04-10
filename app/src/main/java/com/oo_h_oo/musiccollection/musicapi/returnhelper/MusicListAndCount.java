package com.oo_h_oo.musiccollection.musicapi.returnhelper;

import com.oo_h_oo.musiccollection.musicmanage.Music;

import java.util.List;

public class MusicListAndCount {
    public MusicListAndCount(List<Music> list, int count){
        this.list = list;
        this.count = count;
    }
    private List<Music> list;
    private int count;

    public List<Music> getList() {
        return list;
    }

    public int getCount() {
        return count;
    }
}
