package com.ks.plugin.widget.launcher.accessibility;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

public class WxHelper {

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
        Log.i("聊天", id + "");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ClipData clip = ClipData.newPlainText("label", "你说什么");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            intent.setClipData(clip);
        }
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);

        intent.setDataAndType(Uri.withAppendedPath(
                ContactsContract.Data.CONTENT_URI, String.valueOf(id)),
                WEIXIN_CHATTING_MIMETYPE);
        context.startActivity(intent);
    }

    /**
     * 根据电话号码查询微信id
     **/
    public static int getChattingID(Context context, String querymobile, String mimeType) {
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
            Log.i("WxId", wexin_id + "");
            return wexin_id;
        }
        cursor.close();
        return 0;
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

    public static int getTalkWxId(Context context, String phone) {
        return getChattingID(context, phone, WEIXIN_SNS_MIMETYPE);
    }

    public static int getFriendWxId(Context context, String phone) {
        return getChattingID(context, phone, WEIXIN_SNS_MIMETYPE);
    }

    public static void shareToFriend(Context context, String phone, String msg) {
        shareToFriend(context, getTalkWxId(context, phone));
    }
}
