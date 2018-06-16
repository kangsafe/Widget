package com.ks.plugin.widget.launcher.accessibility;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;

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
        Logger.i("聊天:" + id);
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
            Logger.i("WxId:" + wexin_id);
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

    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";

    public static String decrptWx(Context context) {
        execRootCmd("chmod 777 -R " + WX_ROOT_PATH);
        String ime = getPhoneIMEI(context);
        Logger.i("IME:" + ime);
        String wxuid = getCurrWxUin();
        Logger.i("WxUid:" + wxuid);
        return initDbPassword(ime, wxuid);
    }

    /**
     * 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
     *
     * @param imei
     * @param uin
     * @return
     */
    private static String initDbPassword(String imei, String uin) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
            Logger.w("初始化数据库密码失败：imei或uid为空");
            return "";
        }
        String md5 = md5(imei + uin);
        return md5.substring(0, 7).toLowerCase();
    }

    /**
     * md5加密
     *
     * @param content
     * @return
     */
    private static String md5(String content) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes("UTF-8"));
            byte[] encryption = md5.digest();//加密
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    sb.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";

    /**
     * 获取微信的uid
     * 微信的uid存储在SharedPreferences里面
     * 存储位置\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml
     */
    private static String getCurrWxUin() {
        FileInputStream in = null;
        String wxuid = "";
        try {
            File file = new File(WX_SP_UIN_PATH);
            in = new FileInputStream(file);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                if ("_auth_uin".equals(element.attributeValue("name"))) {
                    wxuid = element.attributeValue("value");
                    break;
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("获取微信uid失败，请检查auth_info_key_prefs文件权限");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return wxuid;
    }


    /**
     * 获取手机的imei码
     *
     * @return
     */
    private static String getPhoneIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        return tm.getDeviceId();
    }

    /**
     * 执行linux指令
     *
     * @param paramString
     */
    public static void execRootCmd(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            localObject = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}
