package com.oo_h_oo.musiccollection;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.adapter.MusicListAdapter;
import com.oo_h_oo.musiccollection.adapter.ViewPagerAdapter;
import com.oo_h_oo.musiccollection.defaultres.GlobalResources;
import com.oo_h_oo.musiccollection.image.FileCache;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.MusicListAndCount;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.MusicListAndPlayListDetail;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.utils.DisplayUtil;
import com.oo_h_oo.musiccollection.view.DiscoverFragment;
import com.oo_h_oo.musiccollection.view.PlayListCollectionFragment;
import com.oo_h_oo.musiccollection.view.RankingListFragment;
import com.oo_h_oo.musiccollection.widget.BaseFragment;
import com.oo_h_oo.musiccollection.widget.BottomNavigationViewHelper;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;
    private ImageView playview;

    private PullToRefreshListView musicListView;
    private MusicListAdapter musicAdapter;
    private PlayListCollection musicList = new PlayListCollection("","",new ArrayList<Music>());
    private Button backToPlayListButtno;
    private Button playAllButtno;
    private Button allToCurrentListButtno;
    private Button allToMyPlayListButtno;
    private EditText searchEditText;
    private View playListDetailLayout;


    private FloatingActionMenu fabMenu;
    private FloatingActionButton qqmusicFab;
    private FloatingActionButton cloudFab;
    private FloatingActionButton xiamiFab;

    private int offset,musicCount;
    private String searchStr;
    private NetMusicType pageType=NetMusicType.QQMusic;
    private boolean isSearchMusic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_main);
        makeStatusBarTransparent();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        //默认 >3 的选中效果会影响ViewPager的滑动切换时的效果，故利用反射去掉
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_find:
                                viewPager.setCurrentItem(0);
                                playListDetailLayout.setVisibility(View.GONE);
                                break;
                            case R.id.item_news:
                                viewPager.setCurrentItem(1);
                                playListDetailLayout.setVisibility(View.GONE);
                                break;
                            case R.id.item_lib:
                                viewPager.setCurrentItem(2);
                                playListDetailLayout.setVisibility(View.GONE);
                                playListCollectionFragment.updateData();
                                break;
                            case R.id.item_more:
                                viewPager.setCurrentItem(3);
                                playListDetailLayout.setVisibility(View.GONE);
                                break;
                        }
                        return false;
                    }
                });
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
                if (position == 2)
                    playListCollectionFragment.updateData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        //禁止ViewPager滑动
//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });


        GlobalResources.getInstance().setMainActivity(this);
        setupViewPager(viewPager);
        GlobalResources.getInstance().setPlayIntent(new Intent(MainActivity.this, PlayActivity.class));
        startActivity(GlobalResources.getInstance().getPlayIntent());
        LinearLayout ly = findViewById(R.id.searchTextAndplayView);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ly.getLayoutParams();
        lp.height = lp.height - DisplayUtil.getStatusHeight(this);
        lp.setMargins(0, DisplayUtil.getStatusHeight(this), 0, 0);
        ly.setLayoutParams(lp);
        playview = findViewById(R.id.playview);
        playview.setOnClickListener(clickListener);

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)  {
                if (actionId==EditorInfo.IME_ACTION_SEND ||(event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER))
                {
                    if (searchStr.length() > 0)
                        offset = 0;
                        new GetSearchMusicTask(true).execute();
                    return true;
                }
                return false;
            }
        });
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                searchStr = searchEditText.getText().toString();
                return false;
            }
        });
        playListDetailLayout = findViewById(R.id.layout_playlist_detail);
        fabMenu = playListDetailLayout.findViewById(R.id.menu_pagetype);
        qqmusicFab = playListDetailLayout.findViewById(R.id.qqmusicfab);
        cloudFab = playListDetailLayout.findViewById(R.id.cloudfab);
        xiamiFab = playListDetailLayout.findViewById(R.id.xiamifab);
        qqmusicFab.setOnClickListener(clickListener);
        cloudFab.setOnClickListener(clickListener);
        xiamiFab.setOnClickListener(clickListener);
        qqmusicFab.setEnabled(false);


        playAllButtno = playListDetailLayout.findViewById(R.id.playall);
        allToCurrentListButtno = playListDetailLayout.findViewById(R.id.all_add_to_currentlist);
        allToMyPlayListButtno = playListDetailLayout.findViewById(R.id.add_to_myplaylist);
        backToPlayListButtno = playListDetailLayout.findViewById(R.id.back_to_playlist);
        playAllButtno.setOnClickListener(clickListener);
        allToCurrentListButtno.setOnClickListener(clickListener);
        allToMyPlayListButtno.setOnClickListener(clickListener);
        backToPlayListButtno.setOnClickListener(clickListener);
        musicListView = findViewById(R.id.listview_playlist_detail);
        musicAdapter = new MusicListAdapter(getLayoutInflater(), musicList.getPlayList());
        musicListView.setAdapter(musicAdapter);
        this.setPullToRefreshLable();
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Music item = (Music)adapterView.getItemAtPosition(i);
                GlobalResources.getInstance().addMusic(item, true);
            }
        });
        musicListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullDownToRefresh");
                if (isSearchMusic && searchStr.length() > 0){
                    offset = 0;musicCount = 0;
                    new GetSearchMusicTask(true).execute();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullUpToRefresh");
                if (isSearchMusic && searchStr.length() > 0 && offset*30<musicCount){
                    new GetSearchMusicTask(false).execute();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        GlobalResources.getInstance().saveValuesToFile();
        super.onDestroy();
    }

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

    private PlayListCollectionFragment playListCollectionFragment;
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        playListCollectionFragment = new PlayListCollectionFragment();
        adapter.addFragment(new DiscoverFragment());
        adapter.addFragment(new RankingListFragment());
        adapter.addFragment(playListCollectionFragment);
        adapter.addFragment(BaseFragment.newInstance(4,"本地"));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File file = new File(Environment.getExternalStorageDirectory() + FileCache.DIR_NAME);
                        if (!file.exists()) {
                            Log.d("log", "path1 create:" + file.mkdirs());
                        }
                    }
                    break;
                }
        }
    }
        private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.qqmusicfab:
                case R.id.cloudfab:
                case R.id.xiamifab:
                    qqmusicFab.setEnabled(true);
                    cloudFab.setEnabled(true);
                    xiamiFab.setEnabled(true);
            }
            switch (v.getId()) {
                case R.id.playview:
                    startActivity(GlobalResources.getInstance().getPlayIntent());
                    break;
                case R.id.back_to_playlist:
                    musicList.getPlayList().clear();
                    musicAdapter.notifyDataSetChanged();
                    playListDetailLayout.setVisibility(View.GONE);
                    break;
                case R.id.playall:
                    if (musicList.getPlayList().size() > 0)
                        GlobalResources.getInstance().addMusic(musicList.getPlayList(), true);
                    break;
                case R.id.all_add_to_currentlist:
                    GlobalResources.getInstance().addMusic(musicList.getPlayList(), false);
                    break;
                case R.id.add_to_myplaylist:
                    GlobalResources.getInstance().addPlayListCollection(new PlayListCollection(musicList.getName(), musicList.getImgUrl(), new ArrayList<>(musicList.getPlayList()) ));
                    break;
                case R.id.qqmusicfab:
                    pageType = NetMusicType.QQMusic;
                    qqmusicFab.setEnabled(false);
                    break;
                case R.id.cloudfab:
                    pageType = NetMusicType.CloudMusix;
                    cloudFab.setEnabled(false);
                    break;
                case R.id.xiamifab:
                    pageType = NetMusicType.XiaMiMusic;
                    xiamiFab.setEnabled(false);
                    break;
            }
            switch (v.getId()) {
                case R.id.qqmusicfab:
                case R.id.cloudfab:
                case R.id.xiamifab:
                    offset = 0; musicCount=0;
                    new GetSearchMusicTask(true).execute();
            }
            fabMenu.close(true);
        }
    };

    public void showMusicList(Playlist playList, NetMusicType pageType){
        new GetMusicListDataTask(playList, pageType).execute();
    }

    public void showMusicList(PlayListCollection playListCollection){
        playListDetailLayout.setVisibility(View.VISIBLE);
        musicListView.setRefreshing();
        musicList.getPlayList().clear();
        musicList.getPlayList().addAll(playListCollection.getPlayList());
        musicList.setName(playListCollection.getName());
        musicList.setImgUrl(playListCollection.getImgUrl());
        musicAdapter.notifyDataSetChanged();
        musicListView.onRefreshComplete();
    }

    @SuppressLint("StaticFieldLeak")
    public class GetMusicListDataTask extends AsyncTask<Void, Void, MusicListAndPlayListDetail>
    {
        private Playlist playList;
        private NetMusicType pageType;
        public GetMusicListDataTask(Playlist playList, NetMusicType pageType){
            super();
            this.playList = playList;
            this.pageType = pageType;
        }

        @Override
        protected void onPreExecute(){
            playListDetailLayout.setVisibility(View.VISIBLE);
            musicList.getPlayList().clear();
            musicListView.setRefreshing();
            isSearchMusic = false;
            fabMenu.hideMenuButton(true);
        }

        @Override
        protected MusicListAndPlayListDetail doInBackground(Void... params)
        {
            return NetMusicHelper.getPlayListItems(playList.getUrl(),pageType);
        }

        @Override
        protected void onPostExecute(MusicListAndPlayListDetail result)
        {
            musicList.getPlayList().addAll(result.getList());
            musicList.setName(result.getName());
            musicList.setImgUrl(result.getImgUrl());
            musicAdapter.notifyDataSetChanged();
            musicListView.onRefreshComplete();
        }
    }

    public class GetSearchMusicTask extends AsyncTask<Void, Void, MusicListAndCount>{

        private boolean needClear;
        GetSearchMusicTask(boolean needClear){
            this.needClear = needClear;
            offset = 0;
        }
        @Override
        protected void onPreExecute(){
            playListDetailLayout.setVisibility(View.VISIBLE);
            if (needClear) musicList.getPlayList().clear();
            musicListView.setRefreshing();
            isSearchMusic = true;
            fabMenu.showMenuButton(true);
        }

        @Override
        protected MusicListAndCount doInBackground(Void... params)
        {
            return NetMusicHelper.getSearchMusicList(searchStr, offset, pageType);
        }

        @Override
        protected void onPostExecute(MusicListAndCount result)
        {
            offset++;
            musicCount = result.getCount();
            musicList.getPlayList().addAll(result.getList());
            musicList.setName(searchStr);
            musicList.setImgUrl("");
            musicAdapter.notifyDataSetChanged();
            musicListView.onRefreshComplete();
        }
    }
    private void setPullToRefreshLable(){
        ILoadingLayout startLabels = musicListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("一直划，划...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在刷新哦...");// 刷新时
        startLabels.setReleaseLabel("松开，松开就刷新...");// 下来达到一定距离时，显示的提示

        ILoadingLayout endLabels = musicListView.getLoadingLayoutProxy(false, true);
        endLabels.setPullLabel("一直划，划...");// 刚下拉时，显示的提示
        endLabels.setRefreshingLabel("正在加载哦...");// 刷新时
        endLabels.setReleaseLabel("松开，松开就加载...");// 下来达到一定距离时，显示的提示
    }
}
