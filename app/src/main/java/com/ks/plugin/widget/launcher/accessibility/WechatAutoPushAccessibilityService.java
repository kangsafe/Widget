package com.ks.plugin.widget.launcher.accessibility;

/**
 * Created by Administrator on 2018/4/5.
 */

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ks.plugin.widget.launcher.jpush.MyReceiver;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WechatAutoPushAccessibilityService extends AccessibilityService {
    private final static String MM_PNAME = "com.tencent.mm";
    boolean hasAction = false;
    boolean locked = false;
    boolean background = false;
    private String name;
    private String scontent;
    AccessibilityNodeInfo itemNodeinfo;
    private KeyguardManager.KeyguardLock kl;
    private Handler handler = new Handler();


    /**
     * 必须重写的方法，响应各种事件。
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        String nowPackageName = event.getPackageName().toString();

        Logger.d("maptrix:" + nowPackageName + " get event = " + eventType);
        itemNodeinfo = getRootInActiveWindow();
        if (itemNodeinfo == null) {
            return;
        }
        // 判断是否为微信应用、并判断当前状态是否为运行状态
        if (nowPackageName.equals("com.tencent.mm")) {
            String className1 = event.getClassName().toString();
            Logger.i("maptrix:" + className1);
            if (className1.equals("com.tencent.mm.ui.chatting.ChattingUI")) {
                if (fill()) {
                    send();
                } else {
                    if (itemNodeinfo != null) {
                        itemNodeinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (fill()) {
                                    send();
                                }
                                //back2Home();
                                release();
                                hasAction = false;
                            }
                        }, 1000);
                        return;
                    }
                }
            }

            bring2Front();
            //back2Home();
            release();
            if (!MyReceiver.mapQueues.isEmpty()) {
                MyReceiver.mapQueues.remove();
            }
        }
    }


    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    private void send() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo
                        .findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            pressBackButton();
        }

    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param event
     */
    private void sendNotifacationReply(AccessibilityEvent event) {
        hasAction = true;
        if (event.getParcelableData() != null
                && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event
                    .getParcelableData();
            String content = notification.tickerText.toString();
            String[] cc = content.split(":");
            name = cc[0].trim();
            scontent = cc[1].trim();

            Logger.i("maptrix:sender name =" + name);
            Logger.i("maptrix:sender content =" + scontent);


            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    Map<String, String> map = null;

    private boolean fill() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            try {
                map = MyReceiver.mapQueues.peek();
                Logger.d(map);
                if (map == null) return false;
                return findEditText(rootNode, map.get("msg"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        Logger.d(content);
        int count = rootNode.getChildCount();

        Logger.d("maptrix:root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + count);
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                Logger.d("maptrix:nodeinfo = null");
                continue;
            }

            Logger.d("maptrix:class=" + nodeInfo.getClassName());
            //Logger.e("maptrix:ds=" + nodeInfo.getContentDescription());
            if (nodeInfo.getContentDescription() != null) {
//                int nindex = nodeInfo.getContentDescription().toString().indexOf(name);
//                int cindex = nodeInfo.getContentDescription().toString().indexOf(scontent);
//                Logger.e("maptrix:nindex=" + nindex + " cindex=" + cindex);
//                if (nindex != -1) {
//                    itemNodeinfo = nodeInfo;
//                    Logger.i("maptrix:find node info");
//                }
                Logger.e(nodeInfo.getContentDescription().toString());
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                Logger.i("maptrix:==================");
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }

            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 判断指定的应用是否在前台运行
     *
     * @param packageName
     * @return
     */
    private boolean isAppForeground(String packageName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName)) {
            return true;
        }

        return false;
    }


    /**
     * 将当前应用运行到前台
     */
    private void bring2Front() {
        ActivityManager activtyManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activtyManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos) {
            if (this.getPackageName().equals(runningTaskInfo.topActivity.getPackageName())) {
                activtyManager.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }

    /**
     * 回到系统桌面
     */
    private void back2Home() {
//        Intent home = new Intent(Intent.ACTION_MAIN);
//
//        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        home.addCategory(Intent.CATEGORY_HOME);
//
//        startActivity(home);
        pressBackButton();
    }


    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    private boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    private void wakeAndUnlock() {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        //点亮屏幕
        wl.acquire(1000);

        //得到键盘锁管理器对象
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");

        //解锁
        kl.disableKeyguard();

    }

    private void release() {

        if (locked && kl != null) {
            Logger.d("maptrix:release the lock");
            //得到键盘锁管理器对象
            kl.reenableKeyguard();
            locked = false;
        }
    }
}
