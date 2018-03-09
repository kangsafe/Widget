package com.ks.plugin.widget.note;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.ks.plugin.widget.note.NoteAppWidgetProvider.CLICK_ACTION;

/**
 * Created by Administrator on 2018/2/16.
 */

public class MyService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {
    private String TAG = getClass().getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerClipEvents(this);
        updateWidget("", "open");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateWidget(String content, String extend) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                MyWidgetProvider.class);
        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.app_widget_note);
        // 点击列表触发事件
        Intent clickIntent = new Intent(getApplicationContext(), MyWidgetProvider.class);
        // 设置Action，方便在onReceive中区别点击事件
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_extend, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_text, pendingIntent);
        if (content != null && !content.isEmpty()) {
            Log.d("widget", "onUpdate: 剪贴板有数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, content);
        } else {
            Log.d("widget", "onUpdate: 剪贴板无数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, "剪贴板暂无数据");
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    ClipboardManager manager;

    private void registerClipEvents(Context context) {

        manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);

        manager.addPrimaryClipChangedListener(this);
    }

    private void unregisterClipEvents() {
        manager.removePrimaryClipChangedListener(this);
    }

    private long previousTime = 0;

    @Override
    public void onPrimaryClipChanged() {
        long now = System.currentTimeMillis();
        if (now - previousTime < 200) {
            previousTime = now;
            return;
        }
        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
            CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
            if (addedText != null) {
                Log.i(TAG, "copied text: " + addedText);
            }
            updateWidget(addedText.toString(), "");
        }

        previousTime = now;
    }

}