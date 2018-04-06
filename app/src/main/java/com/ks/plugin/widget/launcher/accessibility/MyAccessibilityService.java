package com.ks.plugin.widget.launcher.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.ks.plugin.widget.launcher.MyApplication;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

/**
 * Created by Administrator on 2018/4/4.
 */

public class MyAccessibilityService extends AccessibilityService {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("测试", "服务创建");
    }

    // 当前事件发生的包名
    String nowPackageName;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 获取当前事件的包名
        nowPackageName = event.getPackageName().toString();
        Log.i("测试", "是否运行" + nowPackageName + "-" + event.getEventType());
        // 判断是否为美团应用、并判断当前状态是否为运行状态
        if (nowPackageName.equals("com.sankuai.meituan")) {
            // 判断是否为我们所需的窗口状态变化
            if (event.getEventType() == TYPE_WINDOW_CONTENT_CHANGED || event.getEventType() == TYPE_WINDOW_STATE_CHANGED) {
                Log.i("测试", "窗口变化");
                // 获取事件活动的窗口布局根节点
                AccessibilityNodeInfo rootNode = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    rootNode = this.getRootInActiveWindow();
                    // 解析根节点
                    handle(rootNode);
                }
            }
        }

    }

    private boolean handle(AccessibilityNodeInfo info) {
        Log.i("测试", "节点");
        // 判断节点是否有子控件
        if (info.getChildCount() == 0) {
            Log.i("测试", "无子节点");
            // 判断节点是否有文字并且有“搜索”文字
            if (info.getText() != null && info.getText().toString().contains("搜索")) {

                // 美团的第一个页面布局：判断节点是否为TextView，文字是否匹配
                if ("搜索商家、品类或商圈".equals(info.getText().toString()) && "android.widget.TextView".equals(info.getClassName())) {
                    // 判断节点控件是否可点击，不可点击则点击其父布局，以此类推。
                    AccessibilityNodeInfo parent = info;
                    while (parent != null) {
                        if (parent.isClickable()) {
                            // 模拟点击，跳出循环
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }

                }  // 对第二个布局进行判断：判断是否为一个EidtText对象
                else if ("搜索商家、品类或商圈".equals(info.getText().toString()) && "android.widget.EditText".equals(info.getClassName())) {

                    // 以剪贴板的形式进行文字输入
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("scb", MyApplication.getInstance().getParams());
                    clipboardManager.setPrimaryClip(clipData);

                    // 模拟粘贴
                    info.performAction(AccessibilityNodeInfo.ACTION_PASTE);

                } // 对第三个布局进行判断：判断是否为一个文字为“搜索”的TextView
                else if ("搜索".equals(info.getText().toString()) && "android.widget.TextView".equals(info.getClassName())) {

                    // 以同样的原理进行控件点击
                    AccessibilityNodeInfo parent = info;
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }

                    // 服务进行结束后，设置Flag
                    MyApplication.getInstance().setFlag(false);
                    return true;
                } else {
                    // 其他条件下设置Flag
                    MyApplication.getInstance().setFlag(false);
                }
            }

        } else {
            Log.i("测试", "有子节点");
            // 当 当前节点 有子控件时，解析它的孩子，以此递归
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    handle(info.getChild(i));
                }

            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "zz", Toast.LENGTH_SHORT).show();
    }
}