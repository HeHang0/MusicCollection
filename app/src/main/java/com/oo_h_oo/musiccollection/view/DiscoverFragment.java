package com.oo_h_oo.musiccollection.view;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.adapter.PlayListAdapter;

import java.util.ArrayList;
import java.util.List;


public class DiscoverFragment extends Fragment {

    private PullToRefreshGridView playListView;
    private PlayListAdapter adapter;
    private List<Playlist> list = new ArrayList<>();
    private int offset = 0;
    private int itenCount = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, null);
        playListView = view.findViewById(R.id.listview_discover);
        adapter = new PlayListAdapter(inflater, list);
        setPullToRefreshLable();
        playListView.setAdapter(adapter);
        playListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Playlist item = (Playlist)adapterView.getItemAtPosition(i);
                Snackbar.make(view, "Don't click me.please!.I'am " + item.getUrl(), Snackbar.LENGTH_SHORT).show();
            }
        });
        playListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView){
                Log.e("TAG", "onPullDownToRefresh");
                offset = 0;
                itenCount = 0;
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
        new GetDataTask(true).execute();
        return view;
    }

    private class GetDataTask extends AsyncTask<Void, Void, List<Playlist>>
    {
        private boolean isPullDown;
        public GetDataTask(boolean isPullDown){
            super();
            this.isPullDown = isPullDown;
        }
        @Override
        protected List<Playlist> doInBackground(Void... params)
        {
            return getData(offset,itenCount);
        }

        @Override
        protected void onPostExecute(List<Playlist> result)
        {
            if (this.isPullDown) list.clear();
            offset++;
            itenCount += result.size();
            list.addAll(result);
            adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            playListView.onRefreshComplete();
        }
    }

    private List<Playlist> getData(int offset, int count){
        List<Playlist> list = new ArrayList<>();
        if (count>=200) return list;
        for (int i=0; i < 30;i++){
            list.add(new Playlist((count + 1 + i) + "测试测试测试测试测试测试测试测试测试测试测试测试", "https://p1.music.126.net/ImfNhEZ47Wfx825XLTf-vw==/109951163214338579.jpg?param=150y150", "" + (count + i + 1)));
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