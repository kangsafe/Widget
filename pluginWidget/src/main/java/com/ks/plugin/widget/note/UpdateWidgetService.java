package com.ks.plugin.widget.note;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

import static com.ks.plugin.widget.note.NoteAppWidgetProvider.CLICK_ACTION;

/**
 * Created by Administrator on 2018/2/16.
 */
public class UpdateWidgetService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {

    private Timer timer;
    private TimerTask task;

    public UpdateWidgetService() {
        registerClipEvents(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
//                int runningTaskCount = SystemInfoUtils.getRunningTaskCount(UpdateWidgetService.this);
//                long avaliMem = SystemInfoUtils.getAvaliMem(UpdateWidgetService.this);
//                ComponentName componentName = new ComponentName(UpdateWidgetService.this, MyWidget.class);
//                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.appwidget_view);
//
//                //设置Widget中Textview的显示内容
//                remoteViews.setTextViewText(R.id.tv_runprocessnumber, "正在运行软件:" + runningTaskCount);
//                remoteViews.setTextViewText(R.id.tv_avalimem, "可用内存:" + Formatter.formatFileSize(UpdateWidgetService.this, avaliMem));
//
//                //点击widget的一键清理按钮，发送广播，在AutoKillTaskReceiver广播中杀掉所有的进程。
//                Intent intent = new Intent(UpdateWidgetService.this, AutoKillTaskReceiver.class);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(UpdateWidgetService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                remoteViews.setOnClickPendingIntent(R.id.btn_killall, pendingIntent);
//
//                //点击widget显示信息部分，跳到程序管理页面
//                Intent startActivityIntent = new Intent(UpdateWidgetService.this, TaskManagerActivity.class);
//                startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                PendingIntent processInfoIntent = PendingIntent.getActivity(UpdateWidgetService.this, 0, startActivityIntent, PendingIntent.FLAG_ONE_SHOT);
//                remoteViews.setOnClickPendingIntent(R.id.ll_processinfo, processInfoIntent);
//
//                //由AppWidgetManager处理Wiget。
//                AppWidgetManager awm = AppWidgetManager.getInstance(getApplicationContext());
//                awm.updateAppWidget(componentName, remoteViews);
                updateWidget();
            }
        };
        timer.schedule(task, 0, 3000);
        super.onCreate();
    }


    private void updateWidget() {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                NoteAppWidgetProvider.class);
        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.app_widget_note);
        // 点击列表触发事件
        Intent clickIntent = new Intent(getApplicationContext(), NoteAppWidgetProvider.class);
        // 设置Action，方便在onReceive中区别点击事件
        clickIntent.setAction(CLICK_ACTION);
        //折叠
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_extend, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_text, pendingIntent);
        String content = getClipboard(getApplicationContext());
        if (content != null && !content.isEmpty()) {
            Log.d("widget", "onUpdate: 剪贴板有数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, content);
        } else {
            Log.d("widget", "onUpdate: 剪贴板无数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, "剪贴板暂无数据");
        }
        // 更新 Widget
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

    @Override
    public void onPrimaryClipChanged() {

        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {

            updateWidget();
            CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();

            if (addedText != null) {
                Log.d("TAG", "copied text: " + addedText);
            }
//            manager.c
        }
    }

    private String getClipboard(Context context) {
        // 获取 剪切板数据
        ClipboardManager cm = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData cd2 = cm != null ? cm.getPrimaryClip() : null;
        return cd2 != null ? cd2.getItemAt(0).getText().toString() : "剪贴板没有可用内容";
    }

    private void setClipborad(Context context, String content) {
        //设置剪切板数据
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
//        clipboardManager.setText(content);  //本方法已被淘汰，API11以后。
        ClipData clipData = ClipData.newPlainText("label", content); //文本型数据 clipData 的构造方法。
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData); // 将 字符串 str 保存 到剪贴板。
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterClipEvents();
        timer.cancel();
        task.cancel();
        timer = null;
        task = null;
    }
}
