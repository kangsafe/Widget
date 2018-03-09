package com.ks.plugin.widget.note;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Administrator on 2018/2/14.
 */

public class NoteAppWidgetProvider extends AppWidgetProvider {
    public static final String CLICK_ACTION = "com.ks.plugin.widget.note.action.CLICK"; // 点击事件的广播ACTION
    private Context mContext;

    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        mContext = context;
        updateWidget(context, appWidgetManager);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context,
                NoteAppWidgetProvider.class);
        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_note);
        // 点击列表触发事件
        Intent clickIntent = new Intent(context, NoteAppWidgetProvider.class);
        // 设置Action，方便在onReceive中区别点击事件
        clickIntent.setAction(CLICK_ACTION);
        //折叠
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_extend, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.app_widget_note_text, pendingIntent);
        String content = getClipboard(context);
        if (content != null && !content.isEmpty()) {
            Log.d("widget", "onUpdate: 剪贴板有数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, content);
        } else {
            Log.d("widget", "onUpdate: 剪贴板无数据");
            remoteViews.setTextViewText(R.id.app_widget_note_clipbaord, "剪贴板暂无数据");
        }
        // 更新 Widget
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_note);
        if (appWidgetManager == null) {
            appWidgetManager = AppWidgetManager.getInstance(context);
        }
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
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

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i("Widget", intent.getAction());
        if (CLICK_ACTION.equals(intent.getAction())) {
            Toast.makeText(context, "hello dog!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 当小部件大小改变时
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}
