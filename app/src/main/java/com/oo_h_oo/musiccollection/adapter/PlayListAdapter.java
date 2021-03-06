package com.oo_h_oo.musiccollection.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oo_h_oo.musiccollection.image.ImageLoader;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;
import com.oo_h_oo.musiccollection.R;

import java.util.List;

public class PlayListAdapter extends BaseAdapter {
    private List<Playlist> list;
    private LayoutInflater inflater;
    public PlayListAdapter(LayoutInflater inflater, List<Playlist> list) {
        this.inflater = inflater;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View itemView = inflater.inflate(R.layout.layout_playlist_helper, null);
        Playlist info = list.get(position);
        TextView titleView = itemView.findViewById(R.id.title);
        titleView.setText(info.getName());
        ImageView imageView = itemView.findViewById(R.id.img);
        if(info.getImgUrl() != null && info.getImgUrl().length() > 0){
            ImageLoader imageLoader = new ImageLoader(inflater.getContext());
            imageLoader.disPlayImage(info.getImgUrl(), imageView);
        }
        return itemView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
