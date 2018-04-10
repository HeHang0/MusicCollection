package com.oo_h_oo.musiccollection.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.adapter.MusicListAdapter;
import com.oo_h_oo.musiccollection.musicapi.NetMusicHelper;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.NetMusicType;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.musicmanage.RankingListType;

import java.util.ArrayList;
import java.util.List;

public class RankingListFragment extends Fragment implements View.OnClickListener {
    private PullToRefreshListView musicListView;
    private MusicListAdapter adapter;
    private List<Music> list = new ArrayList<>();
    private RankingListType rankingListType = RankingListType.HotList;
    private NetMusicType pageType = NetMusicType.QQMusic;
    Button hotButtno;
    Button newsButtno;
    Button soarButtno;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rankinglist, null);
        musicListView = view.findViewById(R.id.listview_rankinglist);
        adapter = new MusicListAdapter(inflater, list);
        setPullToRefreshLable();
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Music item = (Music)adapterView.getItemAtPosition(i);
                Snackbar.make(view, "Don't click me.please!.I'am " + item.getTitle(), Snackbar.LENGTH_SHORT).show();
            }
        });
        musicListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullDownToRefresh");
                //这里写下拉刷新的任务
                new RankingListFragment.GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullUpToRefresh");
                //这里写上拉加载更多的任务
            }
        });
        new RankingListFragment.GetDataTask().execute();

        hotButtno = view.findViewById(R.id.hot_list);
        newsButtno = view.findViewById(R.id.new_song_list);
        soarButtno = view.findViewById(R.id.soar_list);
        hotButtno.setOnClickListener(this);
        newsButtno.setOnClickListener(this);
        soarButtno.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.hot_list:
                hotButtno.setEnabled(false);
                newsButtno.setEnabled(true);
                soarButtno.setEnabled(true);
                rankingListType = RankingListType.HotList;
                new RankingListFragment.GetDataTask().execute();
                break;
            case R.id.new_song_list:
                hotButtno.setEnabled(true);
                newsButtno.setEnabled(false);
                soarButtno.setEnabled(true);
                rankingListType = RankingListType.NewSongList;
                new RankingListFragment.GetDataTask().execute();
                break;
            case R.id.soar_list:
                hotButtno.setEnabled(true);
                newsButtno.setEnabled(true);
                soarButtno.setEnabled(false);
                rankingListType = RankingListType.SoarList;
                new RankingListFragment.GetDataTask().execute();
                break;
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, List<Music>>
    {
        @Override
        protected List<Music> doInBackground(Void... params)
        {
            return getData();
        }

        @Override
        protected void onPostExecute(List<Music> result)
        {
            list.clear();
            list.addAll(result);
            adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            musicListView.onRefreshComplete();
        }
    }

    private List<Music> getData(){
        return NetMusicHelper.getRankingMusicList(rankingListType, pageType);
    }

    private void setPullToRefreshLable(){
        ILoadingLayout startLabels = musicListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("一直划，划...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在刷新哦...");// 刷新时
        startLabels.setReleaseLabel("松开，松开就刷新...");// 下来达到一定距离时，显示的提示
    }
}
