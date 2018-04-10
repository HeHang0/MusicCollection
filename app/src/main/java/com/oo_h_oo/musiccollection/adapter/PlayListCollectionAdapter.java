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
import com.oo_h_oo.musiccollection.musicmanage.PlayListCollection;

import java.util.List;

public class PlayListCollectionAdapter extends BaseAdapter {
    private List<PlayListCollection> list;
    private LayoutInflater inflater;

    public PlayListCollectionAdapter(LayoutInflater inflater, List<PlayListCollection> list) {
        this.inflater = inflater;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View itemView = inflater.inflate(R.layout.layout_playlist_collection_helper, null);
        PlayListCollection info = list.get(position);
        TextView titleView = itemView.findViewById(R.id.title);
        titleView.setText(info.getName());
        TextView subTitleView = itemView.findViewById(R.id.sub_title);
        subTitleView.setText("" + info.getCount() + "首");
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
