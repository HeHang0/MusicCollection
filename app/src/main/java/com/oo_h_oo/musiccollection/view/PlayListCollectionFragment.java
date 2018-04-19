package com.oo_h_oo.musiccollection.view;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.adapter.PlayListCollectionAdapter;
import com.oo_h_oo.musiccollection.defaultres.GlobalResources;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;

import java.util.ArrayList;
import java.util.List;


public class PlayListCollectionFragment extends Fragment {
    private PullToRefreshListView playListCollectionView;
    private PlayListCollectionAdapter adapter;
    private List<PlayListCollection> list = new ArrayList<>();
//    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_playlist_collection, null);
        playListCollectionView = view.findViewById(R.id.listview_playlist_collection);
        adapter = new PlayListCollectionAdapter(inflater, list);
        playListCollectionView.setAdapter(adapter);
        playListCollectionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PlayListCollection item = (PlayListCollection) adapterView.getItemAtPosition(i);
                GlobalResources.getInstance().getMainActivity().showMusicList(item);
            }
        });

        playListCollectionView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullDownToRefresh");
                //这里写下拉刷新的任务
                new PlayListCollectionFragment.GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView){
                Log.e("TAG", "onPullUpToRefresh");
                //这里写上拉加载更多的任务
            }
        });

        new PlayListCollectionFragment.GetDataTask().execute();
        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetDataTask extends AsyncTask<Void, Void, List<PlayListCollection>>
    {
        @Override
        protected List<PlayListCollection> doInBackground(Void... params)
        {
            return getData();
        }

        @Override
        protected void onPostExecute(List<PlayListCollection> result)
        {
            list.clear();
            list.addAll(result);
            adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            playListCollectionView.onRefreshComplete();
        }
    }

    private List<PlayListCollection> getData(){
        return GlobalResources.getInstance().getPlayListCollection();
    }

    public void updateData(){
        new PlayListCollectionFragment.GetDataTask().execute();
    }
}
