package com.oo_h_oo.musiccollection.defaultres;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oo_h_oo.musiccollection.MainActivity;
import com.oo_h_oo.musiccollection.PlayActivity;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GlobalResources {
    private static class GlobalResourcesHolder{
        private static GlobalResources instance=new GlobalResources();
    }
    public static GlobalResources getInstance(){
        return GlobalResourcesHolder.instance;
    }

    GlobalResources(){
        checkFile();
        initValues();
    }

    private static final String DIR_NAME = "MusicCollection/Data";
    private File dataDir;
    private void initValues(){

        try{
            currentMusicList = new Gson().fromJson(ReadAllText("CurrentMusicList.json"), new TypeToken<List<Music>>(){}.getType());
            playListCollection = new Gson().fromJson(ReadAllText("PlayListCollection.json"), new TypeToken<List<PlayListCollection>>(){}.getType());
            currentMusicIndex = new Gson().fromJson(ReadAllText("CurrentMusicIndex.json"), Integer.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (currentMusicList == null) currentMusicList = new ArrayList<>();
        if (playListCollection == null) playListCollection = new ArrayList<>();

    }

    private void checkFile(){
        dataDir = new File(
                android.os.Environment.getExternalStorageDirectory(),
                DIR_NAME);
        if (!dataDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dataDir.mkdirs();
        }
        File currentMusicListFile = new File(dataDir, "CurrentMusicList.json");
        if (!currentMusicListFile.exists()) writeAllText("CurrentMusicList.json", "");
        File playListCollectionFile = new File(dataDir, "PlayListCollection.json");
        if (!playListCollectionFile.exists()) writeAllText("PlayListCollection.json", "");
        File currentMusicIndexFile = new File(dataDir, "CurrentMusicIndex.json");
        if (!currentMusicIndexFile.exists()) writeAllText("CurrentMusicIndex.json", "0");
    }

    public void saveValuesToFile(){
        String currentMusicListString=new Gson().toJson(currentMusicList);
        writeAllText("CurrentMusicList.json", currentMusicListString);

        String playListCollectionString=new Gson().toJson(playListCollection);
        writeAllText("PlayListCollection.json", playListCollectionString);

        writeAllText("CurrentMusicIndex.json", "" + currentMusicIndex);
    }

    private String ReadAllText(String path){
        StringBuilder result = new StringBuilder();
        try {
            File filename = new File(dataDir, path);
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line;
            do {
                line = br.readLine();
                if (line != null) result.append(line);
            }
            while (line != null) ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeAllText(String path, String res){
        try {
            File writename = new File(dataDir, path);
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            out.write(res);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


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
        if (needClear) {
            currentMusicList.clear();
            currentMusicIndex = 0;
        }
        for (Music music:musicList) {
            addMusic(music, false);
        }
    }

    public void addMusic(Music item, boolean needPlay) {
        currentMusicList.add(item);
        new GetMusicDetailTask(currentMusicList.size()-1, needPlay).execute();
        String currentMusicListString=new Gson().toJson(currentMusicList);
        writeAllText("CurrentMusicList.json", currentMusicListString);

        writeAllText("CurrentMusicIndex.json", "" + currentMusicIndex);
    }

    public void addPlayListCollection(PlayListCollection playListCollection){
        this.playListCollection.add(playListCollection);
        String playListCollectionString=new Gson().toJson(this.playListCollection);
        writeAllText("PlayListCollection.json", playListCollectionString);
    }

    @SuppressLint("StaticFieldLeak")
    private class GetMusicDetailTask extends AsyncTask<Void, Void, Music>
    {
        private int index;
        private boolean needPlay;
        GetMusicDetailTask(int index, boolean needPlay){
            this.index = index;
            this.needPlay = needPlay;
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
                if (needPlay && mainActivity != null)  mainActivity.startActivity(GlobalResources.getInstance().getPlayIntent());
                playActivity.updateMusicData(needPlay);
//                if (needPlay) playActivity.playToAndView(currentMusicList.size()-1);
            }
        }
    }

    private int currentMusicIndex;
    private List<Music> currentMusicList;
    private List<PlayListCollection> playListCollection;
    private PlayActivity playActivity;
    private MainActivity mainActivity;
    private Intent playIntent;
}
