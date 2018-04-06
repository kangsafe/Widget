package com.ks.plugin.widget.launcher.accessibility;

import android.util.Log;

public class LogUtils {
    public static void d(String onServiceConnected) {
        if (onServiceConnected != null && !onServiceConnected.isEmpty()) {
            Log.d("LogUtils", onServiceConnected);
        }
    }
}
