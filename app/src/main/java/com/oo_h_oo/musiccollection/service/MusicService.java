package com.oo_h_oo.musiccollection.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.oo_h_oo.musiccollection.MainActivity;
import com.oo_h_oo.musiccollection.defaultres.GlobalResources;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicmanage.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    /*操作指令*/
    public static final String ACTION_OPT_MUSIC_PLAY = "ACTION_OPT_MUSIC_PLAY";
    public static final String ACTION_OPT_MUSIC_PAUSE = "ACTION_OPT_MUSIC_PAUSE";
    public static final String ACTION_OPT_MUSIC_NEXT = "ACTION_OPT_MUSIC_NEXT";
    public static final String ACTION_OPT_MUSIC_LAST = "ACTION_OPT_MUSIC_LAST";
    public static final String ACTION_OPT_MUSIC_SEEK_TO = "ACTION_OPT_MUSIC_SEEK_TO";
    public static final String ACTION_OPT_MUSIC_PLAY_TO = "ACTION_OPT_MUSIC_PLAY_TO";

    /*状态指令*/
    public static final String ACTION_STATUS_MUSIC_PLAY = "ACTION_STATUS_MUSIC_PLAY";
    public static final String ACTION_STATUS_MUSIC_PAUSE = "ACTION_STATUS_MUSIC_PAUSE";
    public static final String ACTION_STATUS_MUSIC_COMPLETE = "ACTION_STATUS_MUSIC_COMPLETE";
    public static final String ACTION_STATUS_MUSIC_DURATION = "ACTION_STATUS_MUSIC_DURATION";

    public static final String PARAM_MUSIC_DURATION = "PARAM_MUSIC_DURATION";
    public static final String PARAM_MUSIC_SEEK_TO = "PARAM_MUSIC_SEEK_TO";
    public static final String PARAM_MUSIC_CURRENT_POSITION = "PARAM_MUSIC_CURRENT_POSITION";
    public static final String PARAM_MUSIC_IS_OVER = "PARAM_MUSIC_IS_OVER";

    public static final String PARAM_MUSIC_PLAY_TO = "PARAM_MUSIC_PLAY_TO";

//    private int mCurrentMusicIndex = 0;
    private boolean mIsMusicPause = false;
//    private List<Music> mMusicDatas = new ArrayList<>();

    private MusicReceiver mMusicReceiver = new MusicReceiver();
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        initMusicDatas(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBoardCastReceiver();
    }

    private void initMusicDatas(Intent intent) {
        if (intent == null) return;
        List<Music> musicDatas = GlobalResources.getInstance().getCurrentMusicList();//MainActivity.PARAM_MUSIC_LIST
//        mMusicDatas.addAll(musicDatas);
    }

    private void initBoardCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(ACTION_OPT_MUSIC_PLAY);
        intentFilter.addAction(ACTION_OPT_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_OPT_MUSIC_NEXT);
        intentFilter.addAction(ACTION_OPT_MUSIC_LAST);
        intentFilter.addAction(ACTION_OPT_MUSIC_SEEK_TO);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver,intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
    }

    private void play(final int index) {
        if (index >= GlobalResources.getInstance().getCurrentMusicList().size()) return;
        new PlayMusicTask(index,this).execute();
//        if (GlobalResources.getInstance().getCurrentMusicList().get(index).getPath().length() < 5){
////            NetMusicHelper.getMusicDetail(GlobalResources.getInstance().getCurrentMusicList().get(index));
////        }
////        if (GlobalResources.getInstance().getCurrentMusicList().get(index).getPath().length() < 5) return;
////        if (mCurrentMusicIndex == index && mIsMusicPause) {
////            mMediaPlayer.start();
////        } else {
////            mMediaPlayer.stop();
////            mMediaPlayer = null;
////
////            mMediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(GlobalResources.getInstance().getCurrentMusicList().get(index).getPath()));
////            mMediaPlayer.start();
////            mMediaPlayer.setOnCompletionListener(this);
////            mCurrentMusicIndex = index;
////            mIsMusicPause = false;
////
////            int duration = mMediaPlayer.getDuration();
////            sendMusicDurationBroadCast(duration);
////        }
////        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PLAY);
    }
    private class PlayMusicTask extends AsyncTask<Void, Void, Music>
    {
        private int index;
        private MusicService musicService;
        PlayMusicTask(int index, MusicService musicService){
            this.index = index;
            this.musicService = musicService;
        }

        @Override
        protected Music doInBackground(Void... p)
        {
            if (GlobalResources.getInstance().getCurrentMusicList().get(index).getPath().length() < 5){
                NetMusicHelper.getMusicDetail(GlobalResources.getInstance().getCurrentMusicList().get(index));
            }
            return GlobalResources.getInstance().getCurrentMusicList().get(index);
        }

        @Override
        protected void onPostExecute(Music music)
        {
            GlobalResources.getInstance().getCurrentMusicList().set(index,music);
            if (GlobalResources.getInstance().getCurrentMusicList().get(index).getPath().length() < 5) return;
            if (GlobalResources.getInstance().getCurrentMusicIndex() == index && mIsMusicPause) {
                mMediaPlayer.start();
            } else {
                mMediaPlayer.stop();
                mMediaPlayer = null;

                mMediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(GlobalResources.getInstance().getCurrentMusicList().get(index).getPath()));
                mMediaPlayer.start();
                mMediaPlayer.setOnCompletionListener(musicService);
                GlobalResources.getInstance().setCurrentMusicIndex(index);
                mIsMusicPause = false;

                int duration = mMediaPlayer.getDuration();
                sendMusicDurationBroadCast(duration);
            }
            sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PLAY);
        }
    }

    private void pause() {
        mMediaPlayer.pause();
        mIsMusicPause = true;
        sendMusicStatusBroadCast(ACTION_STATUS_MUSIC_PAUSE);
    }

    private void stop() {
        mMediaPlayer.stop();
    }

    private void next() {
        if (GlobalResources.getInstance().getCurrentMusicIndex() + 1 < GlobalResources.getInstance().getCurrentMusicList().size()) {
            play(GlobalResources.getInstance().getCurrentMusicIndex() + 1);
        } else {
            stop();
        }
    }

    private void last() {
        if (GlobalResources.getInstance().getCurrentMusicIndex() != 0) {
            play(GlobalResources.getInstance().getCurrentMusicIndex() - 1);
        }
    }

    private void seekTo(Intent intent) {
        if (mMediaPlayer.isPlaying()) {
            int position = intent.getIntExtra(PARAM_MUSIC_SEEK_TO, 0);
            mMediaPlayer.seekTo(position);
        }
    }

    private void playTo(Intent intent){
        int index = intent.getIntExtra(PARAM_MUSIC_PLAY_TO, 0);
        if(index < GlobalResources.getInstance().getCurrentMusicList().size() && GlobalResources.getInstance().getCurrentMusicList().size() > 0 && index >= 0){
            GlobalResources.getInstance().setCurrentMusicIndex(index);
            play(GlobalResources.getInstance().getCurrentMusicIndex());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendMusicCompleteBroadCast();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_OPT_MUSIC_PLAY)) {
                play(GlobalResources.getInstance().getCurrentMusicIndex());
            } else if (action.equals(ACTION_OPT_MUSIC_PAUSE)) {
                pause();
            } else if (action.equals(ACTION_OPT_MUSIC_LAST)) {
                last();
            } else if (action.equals(ACTION_OPT_MUSIC_NEXT)) {
                next();
            } else if (action.equals(ACTION_OPT_MUSIC_SEEK_TO)) {
                seekTo(intent);
            }else if (action.equals(ACTION_OPT_MUSIC_PLAY_TO)){
                playTo(intent);
            }
        }
    }

    private void sendMusicCompleteBroadCast() {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_COMPLETE);
        intent.putExtra(PARAM_MUSIC_IS_OVER, (GlobalResources.getInstance().getCurrentMusicIndex() == GlobalResources.getInstance().getCurrentMusicList().size() - 1));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMusicDurationBroadCast(int duration) {
        Intent intent = new Intent(ACTION_STATUS_MUSIC_DURATION);
        intent.putExtra(PARAM_MUSIC_DURATION, duration);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMusicStatusBroadCast(String action) {
        Intent intent = new Intent(action);
        if (action.equals(ACTION_STATUS_MUSIC_PLAY)) {
            intent.putExtra(PARAM_MUSIC_CURRENT_POSITION,mMediaPlayer.getCurrentPosition());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
