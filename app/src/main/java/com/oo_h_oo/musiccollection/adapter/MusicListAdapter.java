package com.oo_h_oo.musiccollection.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oo_h_oo.musiccollection.R;
import com.oo_h_oo.musiccollection.image.ImageLoader;
import com.oo_h_oo.musiccollection.musicmanage.Music;
import com.oo_h_oo.musiccollection.musicmanage.Playlist;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {
    private List<Music> list;
    private LayoutInflater inflater;
    public MusicListAdapter(LayoutInflater inflater, List<Music> list) {
        this.inflater = inflater;
        this.list = list;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View itemView = inflater.inflate(R.layout.layout_musiclist_helper, null);
        Music info = list.get(position);
        TextView titleView = itemView.findViewById(R.id.title);
        titleView.setText(info.getTitle());
        TextView suntitleView = itemView.findViewById(R.id.sub_title);
        suntitleView.setText(info.getSinger() + (info.getAlbum().length() > 0 ? " - " + info.getAlbum() : ""));

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
