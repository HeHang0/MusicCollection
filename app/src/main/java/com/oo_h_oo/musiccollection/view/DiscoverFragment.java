package com.oo_h_oo.musiccollection.view;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.defaultres.GlobalResources;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicapi.returnhelper.PlayListAndCount;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.adapter.PlayListAdapter;

import java.util.ArrayList;
import java.util.List;


public class DiscoverFragment extends Fragment {

    private PullToRefreshGridView playListView;
    private PlayListAdapter adapter;
    private List<Playlist> list = new ArrayList<>();
//    private MusicListAdapter musicAdapter;
//    private PlayListCollection musicList = new PlayListCollection("","",new ArrayList<Music>());
//    private PullToRefreshListView musicListView;
    private NetMusicType pageType = NetMusicType.QQMusic;
    private int offset = 0;
    private int pageCount = 1;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton qqmusicFab;
    private FloatingActionButton cloudFab;
    private FloatingActionButton xiamiFab;
//    private Button backToPlayListButtno;
//    private Button playAllButtno;
//    private Button allToCurrentListButtno;
//    private Button allToMyPlayListButtno;
//    private View playListDetailLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_discover, null);
        playListView = view.findViewById(R.id.listview_discover);
        adapter = new PlayListAdapter(inflater, list);
        setPullToRefreshLable();
        playListView.setAdapter(adapter);
        playListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Playlist item = (Playlist)adapterView.getItemAtPosition(i);
                GlobalResources.getInstance().getMainActivity().showMusicList(item, pageType);
            }
        });

//        musicListView = view.findViewById(R.id.listview_playlist_detail);
//        musicAdapter = new MusicListAdapter(inflater, musicList.getPlayList());
//        musicListView.setAdapter(musicAdapter);
//        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Music item = (Music)adapterView.getItemAtPosition(i);
//                GlobalResources.getInstance().addMusic(item, true);
//            }
//        });

        playListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView){
                Log.e("TAG", "onPullDownToRefresh");
                offset = 0;
                //这里写下拉刷新的任务
                new GetDataTask(true).execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView){
                Log.e("TAG", "onPullUpToRefresh");
                //这里写上拉加载更多的任务
                new GetDataTask(false).execute();
            }
        });

        fabMenu = view.findViewById(R.id.menu_pagetype);
        qqmusicFab = view.findViewById(R.id.qqmusicfab);
        cloudFab = view.findViewById(R.id.cloudfab);
        xiamiFab = view.findViewById(R.id.xiamifab);
        qqmusicFab.setOnClickListener(clickListener);
        cloudFab.setOnClickListener(clickListener);
        xiamiFab.setOnClickListener(clickListener);
        qqmusicFab.setEnabled(false);

//        playListDetailLayout = view.findViewById(R.id.layout_playlist_detail);
//        playAllButtno = view.findViewById(R.id.playall);
//        allToCurrentListButtno = view.findViewById(R.id.all_add_to_currentlist);
//        allToMyPlayListButtno = view.findViewById(R.id.add_to_myplaylist);
//        backToPlayListButtno = view.findViewById(R.id.back_to_playlist);
//        playAllButtno.setOnClickListener(clickListener);
//        allToCurrentListButtno.setOnClickListener(clickListener);
//        allToMyPlayListButtno.setOnClickListener(clickListener);
//        backToPlayListButtno.setOnClickListener(clickListener);

        new GetDataTask(true).execute();
        return view;
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
                    playListView.setRefreshing();
            }
            switch (v.getId()) {
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
//                case R.id.back_to_playlist:
//                    musicList.getPlayList().clear();
//                    musicAdapter.notifyDataSetChanged();
//                    playListDetailLayout.setVisibility(View.GONE);
//                    fabMenu.showMenuButton(true);
//                    break;
//                case R.id.playall:
//                    if (musicList.getPlayList().size() > 0)
//                        GlobalResources.getInstance().addMusic(musicList.getPlayList(), true);
//                    break;
//                case R.id.all_add_to_currentlist:
//                    GlobalResources.getInstance().addMusic(musicList.getPlayList(), false);
//                    break;
//                case R.id.add_to_myplaylist:
//                    GlobalResources.getInstance().getPlayListCollection().add(new PlayListCollection(musicList.getName(), musicList.getImgUrl(), new ArrayList<Music>(musicList.getPlayList()) ));
//                    break;
            }
            switch (v.getId()) {
                case R.id.qqmusicfab:
                case R.id.cloudfab:
                case R.id.xiamifab:
                    offset = 0; pageCount=1;
                    new GetDataTask(true).execute();
            }
            fabMenu.close(true);
        }
    };
    @SuppressLint("StaticFieldLeak")
    private class GetDataTask extends AsyncTask<Void, Void, List<Playlist>>
    {
        private boolean isPullDown;
        GetDataTask(boolean isPullDown){
            super();
            this.isPullDown = isPullDown;
        }

        @Override
        protected void onPreExecute(){
            playListView.setRefreshing();
        }

        @Override
        protected List<Playlist> doInBackground(Void... params)
        {
            return getData();
        }

        @Override
        protected void onPostExecute(List<Playlist> result)
        {
            if (this.isPullDown) list.clear();
            offset++;
            list.addAll(result);
            adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            playListView.onRefreshComplete();
        }
    }

    private List<Playlist> getData(){
        List<Playlist> list = new ArrayList<>();
        if (offset > 5) return list;
        if (offset < pageCount){
            PlayListAndCount playListAndCount = NetMusicHelper.getPlayList(offset,pageType);
            list.addAll(playListAndCount.getList());
            pageCount = playListAndCount.getCount();
        }
        return list;
    }

    private void setPullToRefreshLable(){
        ILoadingLayout startLabels = playListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("一直划，划...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在刷新哦...");// 刷新时
        startLabels.setReleaseLabel("松开，松开就刷新...");// 下来达到一定距离时，显示的提示

        ILoadingLayout endLabels = playListView.getLoadingLayoutProxy(false, true);
        endLabels.setPullLabel("一直划，划...");// 刚下拉时，显示的提示
        endLabels.setRefreshingLabel("正在加载哦...");// 刷新时
        endLabels.setReleaseLabel("松开，松开就加载...");// 下来达到一定距离时，显示的提示
    }
}
