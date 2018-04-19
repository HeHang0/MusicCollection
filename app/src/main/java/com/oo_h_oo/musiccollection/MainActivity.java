package com.oo_h_oo.musiccollection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.adapter.MusicListAdapter;
import com.oo_h_oo.musiccollection.adapter.ViewPagerAdapter;
import com.oo_h_oo.musiccollection.defaultres.GlobalResources;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.MusicListAndPlayListDetail;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.view.DiscoverFragment;
import com.oo_h_oo.musiccollection.view.PlayListCollectionFragment;
import com.oo_h_oo.musiccollection.view.RankingListFragment;
import com.oo_h_oo.musiccollection.widget.BaseFragment;
import com.oo_h_oo.musiccollection.widget.BottomNavigationViewHelper;

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
    private View playListDetailLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_main);
        makeStatusBarTransparent();

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
                                break;
                            case R.id.item_lib:
                                viewPager.setCurrentItem(2);
                                playListDetailLayout.setVisibility(View.GONE);
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

        playview = findViewById(R.id.playview);
        playview.setOnClickListener(clickListener);

        playListDetailLayout = findViewById(R.id.layout_playlist_detail);

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
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Music item = (Music)adapterView.getItemAtPosition(i);
                GlobalResources.getInstance().addMusic(item, true);
            }
        });
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


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
                    GlobalResources.getInstance().getPlayListCollection().add(new PlayListCollection(musicList.getName(), musicList.getImgUrl(), new ArrayList<Music>(musicList.getPlayList()) ));
                    break;
            }
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
            musicListView.setRefreshing();
        }

        @Override
        protected MusicListAndPlayListDetail doInBackground(Void... params)
        {
            return NetMusicHelper.getPlayListItems(playList.getUrl(),pageType);
        }

        @Override
        protected void onPostExecute(MusicListAndPlayListDetail result)
        {
            musicList.getPlayList().clear();
            musicList.getPlayList().addAll(result.getList());
            musicList.setName(result.getName());
            musicList.setImgUrl(result.getImgUrl());
            musicAdapter.notifyDataSetChanged();
            musicListView.onRefreshComplete();
        }
    }
}
