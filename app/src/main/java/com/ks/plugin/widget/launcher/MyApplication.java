package com.ks.plugin.widget.launcher;

import android.app.Application;
import android.content.Intent;

import com.facebook.stetho.Stetho;
import com.ks.plugin.widget.launcher.accessibility.WechatAutoPushAccessibilityService;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Administrator on 2018/4/4.
 */

public class MyApplication extends Application {
    private static MyApplication context;

    @Override
    public void onCreate() {
        Logger.d("Application Start");
        //开启日志
        Logger.addLogAdapter(new AndroidLogAdapter(PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                //.logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag("Widget")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        //开启文件日志
        Logger.addLogAdapter(new DiskLogAdapter(CsvFormatStrategy.newBuilder()
                .tag("Widget")
                .build()));
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        context = this;
        //极光推送
        JPushInterface.setDebugMode(BuildConfig.DEBUG);    // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush
        //设置别名
        JPushInterface.setAlias(this, 1, "wxauto");
        //启动微信自动推送服务
        startService(new Intent(this, WechatAutoPushAccessibilityService.class));
    }
//
//    @Override
//    protected void onDestroy() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//        super.onDestroy();
//    }


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
