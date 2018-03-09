package com.ks.plugin.widget.note;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by Administrator on 2018/2/16.
 */
public class WidgetProvider extends AppWidgetProvider {
    private static final int UPDATE_DURATION = 10 * 1000; // Widget 更新间隔

    private PendingIntent pendingIntent = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, WidgetService.class);
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getService(context, 0,
                    alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), UPDATE_DURATION, pendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }
}
