package com.ks.plugin.widget.launcher.accessibility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ks.plugin.widget.launcher.MyApplication;
import com.ks.plugin.widget.launcher.R;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_CONTACTS;
import static com.ks.plugin.widget.launcher.accessibility.WxHelper.WEIXIN_SNS_MIMETYPE;
import static com.ks.plugin.widget.launcher.accessibility.WxHelper.WEIXIN_VIDIO_MIMETYPE;
import static com.ks.plugin.widget.launcher.accessibility.WxHelper.WEIXIN_VOICE_MIMETYPE;

public class AccessibilityActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private static final int RC_CONTACT_READ = 101;
    // 用来获取获取打开美团的intent信使
    PackageManager packageManager;

    @BindView(R.id.vmeituan)
    Button btnMeituan;
    @BindView(R.id.vfriend)
    Button btnWxFriend;
    @BindView(R.id.vmsg)
    Button btnWxMsg;
    @BindView(R.id.vwxtalk)
    Button btnOpenWxTalk;
    @BindView(R.id.vwxpengyouquan)
    Button btnOpenWxPenyouquan;
    @BindView(R.id.vreadcontact)
    Button btnReadContact;
    @BindView(R.id.vphone)
    EditText txtPhone;
    @BindView(R.id.vwritecontact)
    Button btnWriteContact;
    @BindView(R.id.vopencontact)
    Button btnOpenContact;
    @BindView(R.id.vinitcontact)
    Button btnInitContact;
    @BindView(R.id.vclearcontact)
    Button btnClearContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility);
        ButterKnife.bind(this);
        init();
        // 获取安装包管理器
        packageManager = this.getPackageManager();
    }

    private void init() {
        btnMeituan.setOnClickListener(this);
        btnWxFriend.setOnClickListener(this);
        btnWxMsg.setOnClickListener(this);
        btnOpenWxTalk.setOnClickListener(this);
        btnOpenWxPenyouquan.setOnClickListener(this);
        btnReadContact.setOnClickListener(this);
        btnWriteContact.setOnClickListener(this);
        btnOpenContact.setOnClickListener(this);
        btnInitContact.setOnClickListener(this);
        btnClearContact.setOnClickListener(this);
    }

    @Override
//    @OnClick({R.id.vmeituan, R.id.vmeituan, R.id.vfriend, R.id.vmsg, R.id.vwxtalk, R.id.vwxpengyouquan})
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
                startActivity(intent);
                break;
            case R.id.vwxtalk:
                pickDocClicked(view);
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
                startActivity(intent);
                break;
            case R.id.vwxpengyouquan:
                pickDocClicked(view);
                break;
            case R.id.vmsg:
                openAccessibility(WechatAutoReplyAccessibilityService.class.getCanonicalName(), this);
                startService(new Intent(this, WechatAutoReplyAccessibilityService.class));
//                intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
                break;
            case R.id.vreadcontact:
                doReadContactData();
                break;
            case R.id.vwritecontact:
                insertContactData(txtPhone.getText().toString());
                break;
            case R.id.vopencontact:
                intent = packageManager.getLaunchIntentForPackage("com.android.contacts");
                if (intent == null) {
                    intent = packageManager.getLaunchIntentForPackage("com.android.providers.contacts");
                    if (intent == null) {
                        intent = new Intent();
                        intent.setClassName("com.android.contacts", "com.android.contacts.activities.PeopleActivity");
                    }
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.vinitcontact:
                doInitContact();
                break;
            case R.id.vclearcontact:
                doClearContact();
                break;
        }
    }


    private void doReadContactData() {
        Cursor cursor = null;
        try {
            //cursor指针 query询问 contract协议 kinds种类
            cursor = getContentResolver().query(Uri.parse("content://com.android.contacts/data"),
                    null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                    String dataversion = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA_VERSION));
                    String data1 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
                    String data2 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA2));
                    String data3 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA3));
                    String data4 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA4));
                    String data5 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA5));
                    String data6 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA6));
                    String data7 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA7));
                    String data8 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA8));
                    String data9 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA9));
                    String data10 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA10));
                    String data11 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA11));
                    String data12 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA12));
                    String data13 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA13));
                    String data14 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA14));
                    String data15 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA15));
                    String sync1 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.SYNC1));
                    String sync2 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.SYNC2));
                    String sync3 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.SYNC3));
                    String sync4 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.SYNC4));
                    String mimetype = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
                    String contactid = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                    String isreadonly = "";//cursor.getString(cursor.getColumnIndex(ContactsContract.Data.IS_READ_ONLY));
                    String isprimary = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.IS_PRIMARY));
                    String issuperprimary = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.IS_SUPER_PRIMARY));
//                    list.add(displayName + '\n' + number);
                    Log.i("CONTACT_DATA", id + "," + mimetype + "," + contactid + "," + isreadonly + "," + isprimary + "," + issuperprimary +
                            "," + dataversion + "," + data1 + "," + data2 + "," + data3 + "," + data4 + "," + data5 + "," + data6 + "," + data7 + "," + data8 + "," + data9 + "," +
                            data10 + "," + data11 + "," + data12 + "," + data13 + "," + data14 + "," + data15 + "," + sync1 + "," + sync2 + "," + sync3 + "," + sync4);
                }
                //notify公布
                //adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //    private ListView listView;
//    private TextView empty;
    private ContentResolver resolver;
    // 联系人表的uri
    private Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
    private Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private Uri emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;

    protected void queryPhone() {
        Log.d("flag", "------------->NUMBER: " + ContactsContract.CommonDataKinds.Phone.NUMBER);
        //initView();
        resolver = getContentResolver();
        Cursor contactsCursor = resolver.query(contactsUri, null, null, null, null);
        List data = parseCursor(contactsCursor);
//        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item, new String[]{"display_name", "phone", "email"}, new int[]{R.id.displayName, R.id.phone, R.id.email});
//        listView.setAdapter(adapter);
//        listView.setEmptyView(empty);
    }

    //读取联系人
    private List parseCursor(Cursor contactsCursor) {
        List data = new ArrayList();
        while (contactsCursor.moveToNext()) {
            Map map = new HashMap();
            // 获取联系人的id
            int _id = contactsCursor.getInt(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            map.put("display_name", name);
            // 根据联系人的_id(此处获取的_id和raw_contact_id相同，因为contacts表就是通过这两个字段和raw_contacts表取得联系)查询电话和email
            // 查询电话
            Cursor phoneCursor = resolver.query(phoneUri, new String[]{ContactsContract.CommonDataKinds.Phone.DATA1},
                    "raw_contact_id = ?", new String[]{_id + ""}, null);
            String phone = "";
            while (phoneCursor.moveToNext()) {
                phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            map.put("phone", phone);
            phoneCursor.close();
            phoneCursor = null;
            // 查询邮箱
            Cursor emailCursor = resolver.query(emailUri, new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS}, "raw_contact_id = ?", new String[]{_id + ""}, null);
            String email = null;
            while (emailCursor.moveToNext()) {
                email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            }
            map.put("email", email);
            data.add(map);
        }
        return data;
    }

    private void doClearContact() {
        resolver = getContentResolver();
        Cursor contactsCursor = resolver.query(contactsUri, null, null, null, null);
        while (contactsCursor.moveToNext()) {
            int _id = contactsCursor.getInt(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int delete = resolver.delete(Uri.parse("content://com.android.contacts/raw_contacts"),
                    ContactsContract.Data._ID + " = ?", new String[]{_id + ""});
            //resolver.delete(contactsUri, ContactsContract.Contacts._ID + "=?", new String[]{_id + ""});
            if (delete >= 0) {
                Log.i("清空通讯录", "删除成功:" + name);
            } else {
                Log.e("清空通讯录", "删除失败" + name);
            }
        }
        contactsCursor.close();
    }

    private void doInitContact() {
        ContentResolver resolver = getContentResolver();
        Type t = new TypeToken<List<HashMap<String, String>>>() {
        }.getType();
        try {
            List<HashMap<String, String>> datas = new Gson().fromJson(new InputStreamReader(
                    getAssets().open("contacts/demo1.json")), t);
            for (HashMap<String, String> m : datas
                    ) {

                ContentValues values = new ContentValues();
                //添加一条空记录
                Uri insert = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
                //获取插入到数据库中的记录id
                long _id = ContentUris.parseId(insert);

                //向data表中添加数据,向data表中插入数据是分条添加，因为调用一次insert只能向数据库中添加一个字段的数据
                ContentValues valuesName = new ContentValues();
                //向哪一条联系人中添加数据
                valuesName.put(ContactsContract.Data.RAW_CONTACT_ID, _id);
                //添加名字
                valuesName.put(ContactsContract.CommonDataKinds.StructuredName.DATA1, m.get("name"));
                //指定类型
                valuesName.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                resolver.insert(ContactsContract.Data.CONTENT_URI, valuesName);

                //昵称
                ContentValues valuesNickName = new ContentValues();
                //向哪一条联系人中添加数据
                valuesNickName.put(ContactsContract.Data.RAW_CONTACT_ID, _id);
                //添加名字
                valuesNickName.put(ContactsContract.CommonDataKinds.StructuredName.DATA1, m.get("name"));
                //指定类型
                valuesNickName.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
                resolver.insert(ContactsContract.Data.CONTENT_URI, valuesNickName);

                //添加电话
                ContentValues valuesNumber = new ContentValues();
                //指定_id
                valuesNumber.put(ContactsContract.Data.RAW_CONTACT_ID, _id);
                //添加电话号码
                valuesNumber.put(ContactsContract.CommonDataKinds.Phone.NUMBER, m.get("phone"));
                //指定数据类型
                valuesNumber.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                resolver.insert(ContactsContract.Data.CONTENT_URI, valuesNumber);

                //添加公司
                ContentValues valuesEmail = new ContentValues();
                //指定_id
                valuesEmail.put(ContactsContract.Data.RAW_CONTACT_ID, _id);
                //添加单位名称
                valuesEmail.put(ContactsContract.CommonDataKinds.Organization.DATA1, m.get("company"));
                //添加职位
                valuesEmail.put(ContactsContract.CommonDataKinds.Organization.DATA4, m.get("position"));
                //指定数据类型
                valuesEmail.put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
                resolver.insert(ContactsContract.Data.CONTENT_URI, valuesEmail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertContactData(String phone) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
//        Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{ContactsContract.Data._ID},
//                ContactsContract.Data.MIMETYPE + "='vnd.android.cursor.item/phone_v2' and ", null, null, null);
//        ContactsContract.RawContactsEntity;
        //插入空记录
        Uri insert = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        //获取插入到数据库中的记录id
        long _id = ContentUris.parseId(insert);
        //查询数据库
//        resolver.query()
        //发送消息
        //doInsertWeiXin(_id, phone, "发送消息", WEIXIN_CHATTING_MIMETYPE);
        //免费视频通话
        doInsertWeiXin(_id, phone, "免费视频通话", WEIXIN_VIDIO_MIMETYPE);
        //查看朋友圈
        doInsertWeiXin(_id, phone, "查看朋友圈", WEIXIN_SNS_MIMETYPE);
        //微信语音
        doInsertWeiXin(_id, phone, "", WEIXIN_VOICE_MIMETYPE);
    }

    private void doInsertWeiXin(long _id, String phone, String keywords, String mimetype) {
        ContentResolver resolver = getContentResolver();//
        //向data表中添加数据,向data表中插入数据是分条添加，因为调用一次insert只能向数据库中添加一个字段的数据
        ContentValues valuesName = new ContentValues();
        //向哪一条联系人中添加数据
        valuesName.put(ContactsContract.Data.RAW_CONTACT_ID, _id);
        //电话号码
        valuesName.put(ContactsContract.CommonDataKinds.StructuredName.DATA1, phone);
        //微信
        valuesName.put(ContactsContract.CommonDataKinds.StructuredName.DATA2, "微信");
        //文本
        valuesName.put(ContactsContract.CommonDataKinds.StructuredName.DATA3, keywords);
        //电话号码的md5值
        valuesName.put(ContactsContract.CommonDataKinds.StructuredName.DATA4, md5(phone));
        //类型为聊天
        valuesName.put(ContactsContract.Data.MIMETYPE, mimetype);
        resolver.insert(ContactsContract.Data.CONTENT_URI, valuesName);
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result.toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void doReadContact() {
        Cursor cursor = null;
        try {
            //cursor指针 query询问 contract协议 kinds种类
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                    list.add(displayName + '\n' + number);
                }
                //notify公布
                //adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 该辅助功能开关是否打开了
     *
     * @param accessibilityServiceName：指定辅助服务名字
     * @param context：上下文
     * @return
     */
    private boolean isAccessibilitySettingsOn(String accessibilityServiceName, Context
            context) {
        int accessibilityEnable = 0;
        String serviceName = context.getPackageName() + "/" + accessibilityServiceName;
        try {
            accessibilityEnable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        } catch (Exception e) {
            Logger.e("get accessibility enable failed, the err:" + e.getMessage());
        }
        if (accessibilityEnable == 1) {
            TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        Logger.v("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Logger.d("Accessibility service disable");
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

    @AfterPermissionGranted(RC_CONTACT_READ)
    public void pickDocClicked(View view) {
        if (EasyPermissions.hasPermissions(this, READ_CONTACTS)) {
            doLaunch(view);
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "读取通讯录",
                    RC_CONTACT_READ,
                    READ_CONTACTS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_CONTACT_READ) {
            doLaunch(null);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    private void doLaunch(View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.vwxtalk:
                    WxHelper.shareToFriend(this, WxHelper.getTalkWxId(this, txtPhone.getText().toString()));
                    break;
                case R.id.vwxpengyouquan:
                    WxHelper.shareToTimeLine(this, WxHelper.getFriendWxId(this, txtPhone.getText().toString()));
                    break;
            }
        }
    }
}
