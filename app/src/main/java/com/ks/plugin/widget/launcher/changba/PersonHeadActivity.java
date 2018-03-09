package com.ks.plugin.widget.launcher.changba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ks.plugin.widget.launcher.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class PersonHeadActivity extends AppCompatActivity implements OnItemClickListener {
    RecyclerView vrecyclerview;
    HomeRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_head);
        vrecyclerview = findViewById(R.id.vrecyclerview);
        adapter = new HomeRecyclerViewAdapter(this);
        adapter.setOnItemClickListener(this);
        vrecyclerview.setAdapter(adapter);
        vrecyclerview.setLayoutManager(new GridLayoutManager(this, 3));
        Gson g = new Gson();
        try {
            InputStream input = getResources().getAssets().open("changba/titlephotos.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            Type type = new TypeToken<BResult<ResultBean>>() {
            }.getType();
            BResult<ResultBean> result = g.fromJson(reader, type);
            adapter.itemList=result.getResult().getItems();
            adapter.notifyDataSetChanged();
        } catch (Exception ex) {

        }

    }

    @Override
    public void onItemClick(View view, int position) {
//        Intent intent = new Intent();
//        intent.setClass(this, RecyclerViewActivity.class);
//        intent.putExtra("sid", adapter.itemList.get(position).getSid());
//        startActivity(intent);
//        setActivityInAnim();
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

}
