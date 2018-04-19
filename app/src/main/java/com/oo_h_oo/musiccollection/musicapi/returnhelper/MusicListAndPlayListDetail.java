package com.oo_h_oo.musiccollection.musicapi.returnhelper;

import com.oo_h_oo.musiccollection.musicmanage.Music;

import java.util.List;

public class MusicListAndPlayListDetail {
    public MusicListAndPlayListDetail(List<Music> list, String name, String imgUrl){
        this.list = list;
        this.name = name;
        this.imgUrl = imgUrl;
    }
    private List<Music> list;
    private String name;
    private String imgUrl;

    public List<Music> getList() {
        return list;
    }

    public String getName() {
        return name;
    }

    public String getImgUrl() {
        return imgUrl;
    }
}
