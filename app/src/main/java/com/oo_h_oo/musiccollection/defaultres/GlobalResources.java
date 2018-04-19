package com.oo_h_oo.musiccollection.defaultres;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;

import com.oo_h_oo.musiccollection.MainActivity;
import com.oo_h_oo.musiccollection.PlayActivity;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;

import java.util.ArrayList;
import java.util.List;

public class GlobalResources {
    public List<Music> getCurrentMusicList() {
        return currentMusicList;
    }

    public List<PlayListCollection> getPlayListCollection() {
        return playListCollection;
    }

    public void setPlayActivity(PlayActivity playActivity) {
        this.playActivity = playActivity;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Intent getPlayIntent() {
        return playIntent;
    }

    public void setPlayIntent(Intent playIntent) {
        this.playIntent = playIntent;
    }

    public int getCurrentMusicIndex() {
        return currentMusicIndex;
    }

    public void setCurrentMusicIndex(int currentMusicIndex) {
        if(currentMusicIndex >= 0 && currentMusicIndex < this.currentMusicList.size())
            this.currentMusicIndex = currentMusicIndex;
        else
            this.currentMusicIndex =0;
    }

    public void addMusic(List<Music> musicList, boolean needClear){
        if (needClear) currentMusicList.clear();
        for (Music music:musicList) {
            addMusic(music, true);
        }
    }

    public void addMusic(Music item, boolean needPlay) {
        currentMusicList.add(item);
        new GetMusicDetailTask(currentMusicList.size()-1).execute();
    }

    private static class GlobalResourcesHolder{
        private static GlobalResources instance=new GlobalResources();
    }
    public static GlobalResources getInstance(){
        return GlobalResourcesHolder.instance;
    }
    private GlobalResources(){
        currentMusicList = new ArrayList<>();
//        historyMusicList = new ArrayList<>();

        playListCollection = new ArrayList<>();
//        for (int i=0; i < 15;i++){
//            playListCollection.add(new PlayListCollection("收藏歌单" + (i+1), "https://p1.music.126.net/ImfNhEZ47Wfx825XLTf-vw==/109951163214338579.jpg?param=150y150", new ArrayList<Music>() ));
//        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetMusicDetailTask extends AsyncTask<Void, Void, Music>
    {
        private int index;
        GetMusicDetailTask(int index){
            this.index = index;
        }

        @Override
        protected Music doInBackground(Void... p)
        {
            if (currentMusicList.get(index).getPath().length() < 5){
                NetMusicHelper.getMusicDetail(currentMusicList.get(index));
            }
            return currentMusicList.get(index);
        }

        @Override
        protected void onPostExecute(Music music)
        {
            currentMusicList.set(index,music);
            if(playActivity != null){
                playActivity.updateMusicData();
//                if (needPlay) playActivity.playToAndView(currentMusicList.size()-1);
            }
        }
    }

    private int currentMusicIndex = 0;
    private List<Music> currentMusicList;
    private List<PlayListCollection> playListCollection;
    private PlayActivity playActivity;
    private MainActivity mainActivity;
    private Intent playIntent;
}
