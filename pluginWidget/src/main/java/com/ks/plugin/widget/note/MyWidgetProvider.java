package com.ks.plugin.widget.note;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.ks.plugin.widget.note.NoteAppWidgetProvider.CLICK_ACTION;

/**
 * Created by Administrator on 2018/2/16.
 */

public class MyWidgetProvider extends AppWidgetProvider {
    // 保存 widget 的id的HashSet，每新建一个 widget 都会为该 widget 分配一个 id。
    private static Set idsSet = new HashSet();
    private Context mContext;
    private String TAG = getClass().getSimpleName();
    private static Intent intent;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        mContext = context;
        Log.d(TAG, "onUpdate(): appWidgetIds.length=" + appWidgetIds.length);
        // 在第一个 widget 被创建时，开启服务
        intent = new Intent(context, MyService.class);
        context.startService(intent);
//        context.startService(new Intent(context, MyService.class));
        // 每次 widget 被创建时，对应的将widget的id添加到set中
        for (int appWidgetId : appWidgetIds) {
            idsSet.add(Integer.valueOf(appWidgetId));
        }
        prtSet();
    }

    // 第一个widget被创建时调用
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        mContext = context;
        Log.i(MyWidgetProvider.class.getSimpleName(), "onEnable");
    }

    // 最后一个widget被删除时调用
    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled");
        // 在最后一个 widget 被删除时，终止服务
        context.stopService(intent);
    }

    // 当 widget 被初次添加 或者 当 widget 的大小被改变时，被调用
    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);
    }

    // widget被删除时调用
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted(): appWidgetIds.length=" + appWidgetIds.length);

        // 当 widget 被删除时，对应的删除set中保存的widget的id
        for (int appWidgetId : appWidgetIds) {
            idsSet.remove(Integer.valueOf(appWidgetId));
        }
        prtSet();

    }

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        super.onReceive(context, intent);
        Log.i("Widget", intent.getAction());
        if (CLICK_ACTION.equals(intent.getAction())) {
            Toast.makeText(context, "hello dog!", Toast.LENGTH_SHORT).show();
            try {
                for (String key : intent.getExtras().keySet()
                        ) {
                    Log.i(TAG, intent.getStringExtra(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (intent.getStringExtra("key").equals("extend")) {
                updateWidget("", intent.getStringExtra("extend"), intent.getStringExtra("key"));
            }
        }
    }

    private void updateWidget(String content, String extend, String key) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(mContext,
                MyWidgetProvider.class);
        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.app_widget_note);
        Intent clickIntent = new Intent(mContext, MyWidgetProvider.class);
        clickIntent.setAction(CLICK_ACTION);
        clickIntent.putExtra("key", "extend");
        if (extend == null || extend.isEmpty() || extend.equals("open")) {
            clickIntent.putExtra("extend", "close");
            remoteViews.setTextViewText(R.id.app_widget_note_extend, "折叠");
            remoteViews.setViewVisibility(R.id.app_widget_note_op_2, VISIBLE);
//            remoteViews.setViewVisibility(R.id.app_widget_note_clipbaord, GONE);
        } else {
            clickIntent.putExtra("extend", "open");
            remoteViews.setTextViewText(R.id.app_widget_note_extend, "展开");
            remoteViews.setViewVisibility(R.id.app_widget_note_op_2, GONE);
//            remoteViews.setViewVisibility(R.id.app_widget_note_clipbaord, VISIBLE);
        }
        //折叠
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_extend, pendingIntent);
        //文本
        pendingIntent = PendingIntent.getBroadcast(mContext, 1, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_text, pendingIntent);
        if (content != null && !content.isEmpty()) {
            Log.d("widget", "onUpdate: 剪贴板有数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, content);
        } else {
            Log.d("widget", "onUpdate: 剪贴板无数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, "剪贴板暂无数据");
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    // 调试用：遍历set
    private void prtSet() {
        int index = 0;
        int size = idsSet.size();
        Iterator it = idsSet.iterator();
        Log.d(TAG, "total:" + size);
        while (it.hasNext()) {
            Log.d(TAG, index + " -- " + ((Integer) it.next()).intValue());
        }
    }
}