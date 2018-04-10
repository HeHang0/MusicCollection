package com.oo_h_oo.musiccollection.view;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.adapter.PlayListCollectionAdapter;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;

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
                Snackbar.make(view, "Don't click me.please!.I'am " + item.getName(), Snackbar.LENGTH_SHORT).show();
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
            list.addAll(result);
            adapter.notifyDataSetChanged();
            // Call onRefreshComplete when the list has been refreshed.
            playListCollectionView.onRefreshComplete();
        }
    }

    private List<PlayListCollection> getData(){
        List<PlayListCollection> list = new ArrayList<>();
        for (int i=0; i < 15;i++){
            list.add(new PlayListCollection("收藏歌单" + (i+1), "https://p1.music.126.net/ImfNhEZ47Wfx825XLTf-vw==/109951163214338579.jpg?param=150y150", new ArrayList<Music>() ));
        }
        return list;
    }
}
