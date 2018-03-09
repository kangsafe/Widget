package com.ks.plugin.widget.launcher.changba;

import android.view.View;

/**
 * Created by Admin on 2016/7/6 0006.
 */
// 点击事件接口
public interface OnItemClickListener {
    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);
}
