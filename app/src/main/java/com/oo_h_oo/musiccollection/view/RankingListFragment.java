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
import android.widget.GridView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.adapter.MusicListAdapter;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;

import java.util.ArrayList;
import java.util.List;

public class RankingListFragment extends Fragment {
    private PullToRefreshListView musicListView;
    private MusicListAdapter adapter;
    private List<Music> list = new ArrayList<>();
    private int offset = 0;
    private int itenCount = 0;
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
                Playlist item = (Playlist)adapterView.getItemAtPosition(i);
                Snackbar.make(view, "Don't click me.please!.I'am " + item.getUrl(), Snackbar.LENGTH_SHORT).show();
            }
        });
        musicListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullDownToRefresh");
                offset = 0;
                itenCount = 0;
                //这里写下拉刷新的任务
                new RankingListFragment.GetDataTask(true).execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullUpToRefresh");
                //这里写上拉加载更多的任务
                new RankingListFragment.GetDataTask(false).execute();
            }
        });
        new RankingListFragment.GetDataTask(true).execute();
        return view;
    }

    private class GetDataTask extends AsyncTask<Void, Void, List<Music>>
    {
        private boolean isPullDown;
        public GetDataTask(boolean isPullDown){
            super();
            this.isPullDown = isPullDown;
        }
        @Override
        protected List<Music> doInBackground(Void... params)
        {
            return getData(offset,itenCount);
        }

        @Override
        protected void onPostExecute(List<Music> result)
        {
            if (this.isPullDown) list.clear();
            offset++;
            itenCount += result.size();
            list.addAll(result);
            adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            musicListView.onRefreshComplete();
        }
    }

    private List<Music> getData(int offset, int count){
        List<Music> list = new ArrayList<>();
        if (count>=200) return list;
        for (int i=0; i < 30;i++){
            Music music = new Music();
            music.setTitle("测试标题" + (count + i+1));
            music.setSinger("歌手" + (count + i+1));
            music.setAlbum("专辑" + (count + i+1));
            list.add(music);
        }
        return list;
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
