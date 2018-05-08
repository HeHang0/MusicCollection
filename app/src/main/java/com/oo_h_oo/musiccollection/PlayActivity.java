package com.oo_h_oo.musiccollection;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.adapter.MusicListAdapter;
import com.oo_h_oo.musiccollection.defaultres.GlobalResources;
import com.oo_h_oo.musiccollection.image.FileCache;
import com.oo_h_oo.musiccollection.image.ImageLoader;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.service.MusicService;
import com.oo_h_oo.musiccollection.shareapi.qq.BaseUIListener;
import com.oo_h_oo.musiccollection.utils.DisplayUtil;
import com.oo_h_oo.musiccollection.utils.FastBlurUtil;
import com.oo_h_oo.musiccollection.widget.BackgourndAnimationRelativeLayout;
import com.oo_h_oo.musiccollection.widget.DiscView;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.Tencent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.net.sip.SipErrorCode.TIME_OUT;
import static android.provider.UserDictionary.Words.APP_ID;
import static com.oo_h_oo.musiccollection.widget.DiscView.DURATION_NEEDLE_ANIAMTOR;


public class PlayActivity extends AppCompatActivity implements DiscView.IPlayInfo, View
        .OnClickListener {

    private DiscView mDisc;
    private Toolbar mToolbar;
    private SeekBar mSeekBar;
    private ImageView mIvPlayOrPause, mIvNext, mIvLast, mIvMusicList;
    private TextView mTvMusicDuration,mTvTotalMusicDuration;
    private BackgourndAnimationRelativeLayout mRootLayout;
    private AlertView mAlertViewMusicList;
    private PullToRefreshListView musicListView;
    private MusicListAdapter adapter;
    private Tencent mTencent;
    private static final String APP_ID = "1106864706";
    public static final int MUSIC_MESSAGE = 0;

    public static final String PARAM_MUSIC_LIST = "PARAM_MUSIC_LIST";

    @SuppressLint("HandlerLeak")
    private Handler mMusicHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSeekBar.setProgress(mSeekBar.getProgress() + 1000);
            mTvMusicDuration.setText(duration2Time(mSeekBar.getProgress()));
            startUpdateSeekBarProgress();
        }
    };

    private MusicReceiver mMusicReceiver = new MusicReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTencent = Tencent.createInstance(APP_ID, this.getApplicationContext());
        setContentView(R.layout.layout_playpage);
        initMusicDatas();
        initMusicReceiver();
        initView();
        makeStatusBarTransparent();
        GlobalResources.getInstance().setPlayActivity(this);
        //moveTaskToBack(true);
    }

    private void initMusicReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PLAY);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_PAUSE);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_DURATION);
        intentFilter.addAction(MusicService.ACTION_STATUS_MUSIC_COMPLETE);
        /*注册本地广播*/
        LocalBroadcastManager.getInstance(this).registerReceiver(mMusicReceiver,intentFilter);
    }

    private void initView() {
        mDisc = findViewById(R.id.discview);
        mIvNext = findViewById(R.id.ivNext);
        mIvLast = findViewById(R.id.ivLast);
        mIvMusicList = findViewById(R.id.currentList);
        mIvPlayOrPause = findViewById(R.id.ivPlayOrPause);
        mTvMusicDuration = findViewById(R.id.tvCurrentTime);
        mTvTotalMusicDuration = findViewById(R.id.tvTotalTime);
        mSeekBar = findViewById(R.id.musicSeekBar);
        mRootLayout = findViewById(R.id.rootLayout);

        mToolbar = findViewById(R.id.toolBar);
//        setSupportActionBar(mToolbar);
//        mToolbar.setNavigationOnClickListener(this);
        mToolbar.findViewById(R.id.back_to_main).setOnClickListener(this);
        mToolbar.findViewById(R.id.sharemusic).setOnClickListener(this);
        mDisc.setPlayInfoListener(this);
        mIvLast.setOnClickListener(this);
        mIvNext.setOnClickListener(this);
        mIvMusicList.setOnClickListener(this);
        mIvPlayOrPause.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvMusicDuration.setText(duration2Time(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateSeekBarProgree();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
                startUpdateSeekBarProgress();
            }
        });
        mDisc.setFirstMusicInfo();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));

        mAlertViewMusicList = new AlertView(null, null, "关闭", null, null, this, AlertView.Style.ActionSheet, null);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.alert_musiclist_layout, null);
        musicListView = extView.findViewById(R.id.listview_musiclist);
        adapter = new MusicListAdapter(getLayoutInflater(), GlobalResources.getInstance().getCurrentMusicList());
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mDisc.playTo(i-1);
                mAlertViewMusicList.dismiss();
            }
        });
        mAlertViewMusicList.addExtView(extView);
//        mDisc.setMusicDataList(GlobalResources.getInstance().getCurrentMusicList());
    }

    public void updateMusicData(boolean needPlay){
        mDisc.addMusicInfo(needPlay);
    }

    private void stopUpdateSeekBarProgree() {
        mMusicHandler.removeMessages(MUSIC_MESSAGE);
    }

    private OnItemClickListener musicListItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(Object o, int position) {
            Music music = GlobalResources.getInstance().getCurrentMusicList().get(position);
            Snackbar.make(getWindow().getDecorView(), "Don't click me.please!." + music.getSinger(), Snackbar.LENGTH_SHORT).show();
        }
    };

    /*设置透明状态栏*/
    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initMusicDatas() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    private void try2UpdateMusicPicBackground(final String musicPicRes) {
        if (mRootLayout.isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRootLayout.setForeground(foregroundDrawable);
                            mRootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        Bitmap bitmap = getForegroundBitmap(musicPicRes);
        return getForegroundDrawable(bitmap);
    }

    private Drawable getForegroundDrawable(String musicPicRes) {
        if (musicPicRes == null || musicPicRes.length() <=1){
            Bitmap bitmap = getForegroundBitmap(R.mipmap.default_background_music);
            return getForegroundDrawable(bitmap);
        }
        FileCache fileCache = new FileCache(this.getParent());

        File f = fileCache.getFile(musicPicRes);
        String path = "";
        if (f.exists()) {
            path =  f.getPath();
        }else {
            // From Network
            try {
                URL imageUrl = new URL(musicPicRes);
                HttpURLConnection conn = (HttpURLConnection) imageUrl
                        .openConnection();
//                conn.setConnectTimeout(2000);
//                conn.setReadTimeout(2000);
                conn.setInstanceFollowRedirects(true);
                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(f);
                copyStream(is, os);
                os.close();
                conn.disconnect();
                path = f.getPath();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        File f1 = new File(path);
        if (f1.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            return getForegroundDrawable(bitmap);
        }else {
            Bitmap bitmap = getForegroundBitmap(R.mipmap.default_background_music);
            return getForegroundDrawable(bitmap);
        }
    }

    private void copyStream(InputStream is, OutputStream os) {
        int buffer_size = 1024;

        try {
            byte[] bytes = new byte[buffer_size];
            while (true) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }

        } catch (Exception e) {

        }
    }

    private Drawable getForegroundDrawable(Bitmap bitmap){
        /*得到屏幕的宽高比，以便按比例切割图片一部分*/
//        final float widthHeightSize = (float) (DisplayUtil.getScreenWidth(PlayActivity.this)
//                * 1.0 / DisplayUtil.getScreenHeight(this) * 1.0);


//        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
//        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);

        /*切割部分图片*/
//        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth,
//                bitmap.getHeight());
        /*缩小图片*/
        if (bitmap == null) bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.default_background_music);
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, DisplayUtil.getScreenWidth(PlayActivity.this)/10, DisplayUtil.getScreenHeight(PlayActivity.this)/10, false);
        /*模糊化*/
        final Bitmap blurBitmap = FastBlurUtil.blurBitmap(PlayActivity.this, scaleBitmap, 25.f);//doBlur(scaleBitmap, 200, true);

        final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
        /*加入灰色遮罩层，避免图片过亮影响其他控件*/
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    private Bitmap getForegroundBitmap(int musicPicRes) {
        int screenWidth = DisplayUtil.getScreenWidth(this);
        int screenHeight = DisplayUtil.getScreenHeight(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(getResources(), musicPicRes);
        }

        int sample = 2;
        int sampleX = imageWidth / DisplayUtil.getScreenWidth(this);
        int sampleY = imageHeight / DisplayUtil.getScreenHeight(this);

        if (sampleX > sampleY && sampleY > 1) {
            sample = sampleX;
        } else if (sampleY > sampleX && sampleX > 1) {
            sample = sampleY;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(getResources(), musicPicRes, options);
    }

    @Override
    public void onMusicInfoChanged(String musicName, String musicAuthor) {
        ((TextView)mToolbar.findViewById(R.id.title)).setText(musicName);
        ((TextView)mToolbar.findViewById(R.id.sub_title)).setText(musicAuthor);
    }

    @Override
    public void onMusicPicChanged(String musicPicRes) {
        try2UpdateMusicPicBackground(musicPicRes);
    }

    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {
        switch (musicChangedStatus) {
            case PLAY:{
                play();
                break;
            }
            case PAUSE:{
                pause();
                break;
            }
            case NEXT:{
                next();
                break;
            }
            case LAST:{
                last();
                break;
            }
            case STOP:{
                stop();
                break;
            }
            case PLAYCURRENT:
                playCurrent();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mIvPlayOrPause) {
            mDisc.playOrPause();
        } else if (v == mIvNext) {
            mDisc.next();
        } else if (v == mIvLast) {
            mDisc.last();
        } else if (v.getId() == R.id.back_to_main){
            moveTaskToBack(true);
        }else if (v.getId() == R.id.currentList){
            mAlertViewMusicList.show();
        }else if (v.getId() == R.id.sharemusic){
            shareToQQ(GlobalResources.getInstance().getCurrentMusicList().get(GlobalResources.getInstance().getCurrentMusicIndex()));
        }
    }

    private void shareToQQ(Music music){
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, music.getSinger() + " - " + music.getTitle());
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  music.getAlbum());
        switch (music.getOrigin()){
            case QQMusic:
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "https://y.qq.com/n/yqq/song/" + music.getMusicID() + ".html");
                break;
            case CloudMusix:
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "http://music.163.com/song?id=" + music.getMusicID());
                break;
            case XiaMiMusic:
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  "https://www.xiami.com/song/" + music.getMusicID());
                break;
        }
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, music.getAlbumImageUrl());
        params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, music.getPath());
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  "听");
        mTencent.shareToQQ(this, params, new BaseUIListener(this));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode,resultCode,data,new BaseUIListener(this) );
    }



    private void play() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PLAY);
        startUpdateSeekBarProgress();
    }

    private void pause() {
        optMusic(MusicService.ACTION_OPT_MUSIC_PAUSE);
        stopUpdateSeekBarProgree();
    }

    private void stop() {
        stopUpdateSeekBarProgree();
        mIvPlayOrPause.setImageResource(R.drawable.ic_play);
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
        mSeekBar.setProgress(0);
    }

    private void next() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_NEXT);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void last() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_LAST);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void playCurrent() {
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                optMusic(MusicService.ACTION_OPT_MUSIC_PLAY_CURRENT);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }
    private static int transIndex = 0;
    public void playToAndView(int index) {
        transIndex = index;
        mRootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                playTo(transIndex);
            }
        }, DURATION_NEEDLE_ANIAMTOR);
        stopUpdateSeekBarProgree();
        mTvMusicDuration.setText(duration2Time(0));
        mTvTotalMusicDuration.setText(duration2Time(0));
    }

    private void playTo(int index) {
        Music music = GlobalResources.getInstance().getCurrentMusicList().get(index);
        if(music.getPath() == null || music.getPath().length() <= 0){

        }
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_PLAY_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_PLAY_TO,index);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void complete(boolean isOver) {
        if (isOver) {
            mDisc.stop();
        } else {
            mDisc.next();
        }
    }

    private void optMusic(final String action) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));
    }

    private void seekTo(int position) {
        Intent intent = new Intent(MusicService.ACTION_OPT_MUSIC_SEEK_TO);
        intent.putExtra(MusicService.PARAM_MUSIC_SEEK_TO,position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void startUpdateSeekBarProgress() {
        /*避免重复发送Message*/
        stopUpdateSeekBarProgree();
        mMusicHandler.sendEmptyMessageDelayed(0,1000);
    }

    /*根据时长格式化称时间文本*/
    private String duration2Time(int duration) {
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        return (min < 10 ? "0" + min : min + "") + ":" + (sec < 10 ? "0" + sec : sec + "");
    }

    private void updateMusicDurationInfo(int totalDuration) {
        mSeekBar.setProgress(0);
        mSeekBar.setMax(totalDuration);
        mTvTotalMusicDuration.setText(duration2Time(totalDuration));
        mTvMusicDuration.setText(duration2Time(0));
        startUpdateSeekBarProgress();
    }

    class MusicReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MusicService.ACTION_STATUS_MUSIC_PLAY)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_pause);
                int currentPosition = intent.getIntExtra(MusicService.PARAM_MUSIC_CURRENT_POSITION, 0);
                mSeekBar.setProgress(currentPosition);
                if(!mDisc.isPlaying()){
                    mDisc.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_PAUSE)) {
                mIvPlayOrPause.setImageResource(R.drawable.ic_play);
                if (mDisc.isPlaying()) {
                    mDisc.playOrPause();
                }
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_DURATION)) {
                int duration = intent.getIntExtra(MusicService.PARAM_MUSIC_DURATION, 0);
                updateMusicDurationInfo(duration);
//                updateMusicData();
            } else if (action.equals(MusicService.ACTION_STATUS_MUSIC_COMPLETE)) {
                boolean isOver = intent.getBooleanExtra(MusicService.PARAM_MUSIC_IS_OVER, true);
                complete(isOver);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMusicReceiver);
    }
}

