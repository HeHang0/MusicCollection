package com.oo_h_oo.musiccollection.musicmanage;

import java.util.List;

public class PlayListCollection {

    public PlayListCollection(String name, String imgUrl, List<Music> playList){
        this.name = name;
        this.imgUrl = imgUrl;
        this.playList = playList;
        if (imgUrl.length() < 5){
            for (Music music: playList) {
                if (music.getAlbumImageUrl().length() > 5){
                    this.imgUrl = music.getAlbumImageUrl();
                    break;
                }
            }
        }
    }

    private String name;
    private String imgUrl;
    private List<Music> playList;

    public void addMusicToList(Music music){
        if (playList != null){
            playList.add(music);
        }
    }

    public int getCount(){
        return playList.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<Music> getPlayList() {
        return playList;
    }

    public void setPlayList(List<Music> playList) {
        this.playList = playList;
    }
}
