package com.oo_h_oo.musiccollection.musicmanage;


public class Playlist {

    public Playlist(String name, String imgUrl, String url)
    {
        this.name = name;
        this.imgUrl = imgUrl;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    private String name ;
    private String url ;
    private String imgUrl ;
}
