package com.ks.plugin.widget.launcher.changba;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ks.plugin.widget.launcher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2017/5/4 0004 10:51.
 * Author: kang
 * Email: kangsafe@163.com
 */

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeViewHolder> {

    public List<TitlePhoto> itemList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private OnItemClickListener listener;
    private Context mContext;

    public HomeRecyclerViewAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //根据不同的viewType，创建并返回相应的ViewHolder
        View view = mLayoutInflater.inflate(R.layout.activity_person_head_item, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, final int position) {
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v, position);
                }
            });
        }
        holder.bindHolder(itemList.get(position));
    }

    //因为要添加一个FooterView所以总体返回的数据应给＋2
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}