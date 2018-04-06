package com.ks.plugin.widget.launcher;

import android.app.Application;

/**
 * Created by Administrator on 2018/4/4.
 */

public class MyApplication extends Application {
    private static MyApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static MyApplication getInstance() {
        return context;
    }

    private boolean flag = false;

    public void setFlag(boolean b) {
        flag = false;
    }

    public String getParams() {
        return params;
    }

    private String params;

    public void setParams(String params) {
        this.params = params;
    }

    public boolean getFlag() {
        return flag;
    }
}
