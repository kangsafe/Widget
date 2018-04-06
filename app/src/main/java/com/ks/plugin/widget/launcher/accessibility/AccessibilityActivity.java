package com.ks.plugin.widget.launcher.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ks.plugin.widget.launcher.MyApplication;
import com.ks.plugin.widget.launcher.R;

public class AccessibilityActivity extends AppCompatActivity implements View.OnClickListener {
    // 用来获取获取打开美团的intent信使
    PackageManager packageManager;

    private String TAG = getClass().getSimpleName();
    Button btnMeituan;
    Button btnWxFriend;
    Button btnWxMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility);
        init();
        // 获取安装包管理器
        packageManager = this.getPackageManager();
    }

    private void init() {
        btnMeituan = findViewById(R.id.vmeituan);
        btnWxFriend = findViewById(R.id.vfriend);
        btnWxMsg = findViewById(R.id.vmsg);
        btnWxFriend.setOnClickListener(this);
        btnMeituan.setOnClickListener(this);
        btnWxMsg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.vmeituan:
                // 设置辅助服务为运行状态。这样做主要为了不破坏用户对应用的自主操作
                MyApplication.getInstance().setFlag(true);
                // 设置美团搜索的信息变量
                MyApplication.getInstance().setParams("鸡排");
                openAccessibility(MyAccessibilityService.class.getCanonicalName(), this);
                startService(new Intent(this, MyAccessibilityService.class));
                // 获取启动美团的intent
                intent = packageManager.getLaunchIntentForPackage("com.sankuai.meituan");//"com.sankuai.meituan"就是我们获得要启动美团应用的包名
                // 每次启动美团应用时，但是以重新启动应用的形式打开
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // 跳转
//                startActivity(intent);
                shareToFriend(this, getTalkWxId());
                break;
            case R.id.vfriend:
                openAccessibility(WechatAutoLikeAccessibilityService.class.getCanonicalName(), this);
                startService(new Intent(this, WechatAutoLikeAccessibilityService.class));
                intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
//                ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.TimeLineUI");
//                intent.setComponent(comp);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                shareToTimeLine(this,getFriendWxId());
                break;
            case R.id.vmsg:
                openAccessibility(WechatAutoReplyAccessibilityService.class.getCanonicalName(), this);
                startService(new Intent(this, WechatAutoReplyAccessibilityService.class));
//                intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
                break;
        }
    }

    public final static String WEIXIN_CHATTING_MIMETYPE = "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile";//微信聊天
    public final static String WEIXIN_SNS_MIMETYPE = "vnd.android.cursor.item/vnd.com.tencent.mm.plugin.sns.timeline";//微信朋友圈
    public final static String WEIXIN_VIDIO_MIMETYPE = "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video";//微信视频
    public final static String WEIXIN_VOICE_MIMETYPE = "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voiceaction";//微信视频

    /**
     * 进去聊天界面
     *
     * @param context
     * @param id      手机通讯录中版本的微信的自动增长列（下面有一个方法或告诉大家如何获取）
     */
    public static void shareToFriend(Context context, int id) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.withAppendedPath(
                ContactsContract.Data.CONTENT_URI, String.valueOf(id)),
                WEIXIN_CHATTING_MIMETYPE);
        context.startActivity(intent);
    }

    /**
     * 朋友圈
     *
     * @param context
     * @param id
     */
    public static void shareToTimeLine(Context context, int id) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.withAppendedPath(
                ContactsContract.Data.CONTENT_URI, String.valueOf(id)),
                WEIXIN_SNS_MIMETYPE);
        context.startActivity(intent);
    }

    /**
     * 根据电话号码查询微信id
     **/
    public int getChattingID(Context context, String querymobile, String mimeType) {
        if (context == null || querymobile == null || querymobile.equals("")) {
            return 0;
        }
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/data");
        StringBuilder sb = new StringBuilder();
        sb.append(ContactsContract.Data.MIMETYPE).append(" = ").append("'");
        sb.append(mimeType).append("'");
        sb.append(" AND ").append("replace(data1,' ','')").append(" = ").append("'").append(querymobile).append("'");
//        sb.append(" AND ").append("replace(data1,' ','')").append(" = ").append("'").append(ToolClass.getPhone(querymobile)).append("'");
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data._ID}, sb.toString(), null, null);
        while (cursor.moveToNext()) {
            int wexin_id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data._ID));
            return wexin_id;
        }
        cursor.close();
        return 0;
    }

    public int getTalkWxId() {
        return getChattingID(this, "15053126163", WEIXIN_SNS_MIMETYPE);
    }
    public int getFriendWxId() {
        return getChattingID(this, "15053126163", WEIXIN_SNS_MIMETYPE);
    }

    /**
     * 该辅助功能开关是否打开了
     *
     * @param accessibilityServiceName：指定辅助服务名字
     * @param context：上下文
     * @return
     */
    private boolean isAccessibilitySettingsOn(String accessibilityServiceName, Context context) {
        int accessibilityEnable = 0;
        String serviceName = context.getPackageName() + "/" + accessibilityServiceName;
        try {
            accessibilityEnable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        } catch (Exception e) {
            Log.e(TAG, "get accessibility enable failed, the err:" + e.getMessage());
        }
        if (accessibilityEnable == 1) {
            TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "Accessibility service disable");
        }
        return false;
    }

    /**
     * 跳转到系统设置页面开启辅助功能
     *
     * @param accessibilityServiceName：指定辅助服务名字
     * @param context：上下文
     */
    private void openAccessibility(String accessibilityServiceName, Context context) {
        if (!isAccessibilitySettingsOn(accessibilityServiceName, context)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
//            //注意 这里可能为空（也就是如果当前没有任何一个无障碍服务被授权的时候 就为空了  感谢评论里面指出bug的同学）
//            String enabledServicesSetting = Settings.Secure.getString(
//                    getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
//
//            ComponentName selfComponentName = new ComponentName(getPackageName(),
//                    accessibilityServiceName);
//            String flattenToString = selfComponentName.flattenToString();
//            if (enabledServicesSetting == null ||
//                    !enabledServicesSetting.contains(flattenToString)) {
//                enabledServicesSetting += flattenToString;
//            }
//            Settings.Secure.putString(getContentResolver(),
//                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
//                    enabledServicesSetting);
//            Settings.Secure.putInt(getContentResolver(),
//                    Settings.Secure.ACCESSIBILITY_ENABLED, 1);
        }
    }
//
//    private void doIt(Context context) {
////        4.使用超级管理员的功能:
//        //清除数据功能
//        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        dpm.wipeData(0);
//        abortBroadcast();
//        //锁屏功能
//        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        dpm.lockNow();
//        abortBroadcast();
//
////        5.弹出激活超级管理员的界面:
//
//        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//        ComponentName cn = new ComponentName(this, AdminReceiver.class);//组件名字
//        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
//        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "可以实现清除数据,锁屏功能");
//        startActivity(intent);
//
////        6.移除超级管理员权限:
//        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        ComponentName cn = new ComponentName(this, AdminReceiver.class);//组件名字
//        dpm.removeActiveAdmin(cn);//移除操作
//
//    }
}
