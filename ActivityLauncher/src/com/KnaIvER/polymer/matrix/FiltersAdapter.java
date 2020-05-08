package com.KnaIvER.polymer.matrix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.KnaIvER.polymer.R;
import com.bumptech.glide.RequestManager;

import java.util.List;

/**
 * Created by Martin on 2016/7/11 0011.
 */
public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.MyViewHolder> {


    private RequestManager manager;
    private LayoutInflater mInflater;
    private List<float[]> filters;

    public FiltersAdapter(RequestManager manager, LayoutInflater mInflater, List<float[]> filters) {
        this.manager = manager;
        this.mInflater = mInflater;
        this.filters = filters;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder;
        viewHolder = new MyViewHolder(
                mInflater.inflate(R.layout.list_item, parent, false));
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return filters.size();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        ColorFilter.imageViewColorFilter(holder.imageView, filters.get(position));
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.img);
        }
    }

}


