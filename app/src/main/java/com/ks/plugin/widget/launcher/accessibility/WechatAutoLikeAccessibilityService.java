package com.ks.plugin.widget.launcher.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/5.
 */

public class WechatAutoLikeAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        Logger.i("onServiceConnected");
    }

    String description;

    ArrayList<Integer> topList = new ArrayList<>();

    List<AccessibilityNodeInfo> lvs;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            String nowPackageName = event.getPackageName().toString();
            //event.getSource().getViewIdResourceName();
            //微信UI界面的根节点，开始遍历节点
            AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
            if (rootNodeInfo == null) {
                return;
            }
            // 判断是否为微信应用、并判断当前状态是否为运行状态
            if (nowPackageName.equals("com.tencent.mm")) {
//                Log.i("TAG", "开始");
                List<AccessibilityNodeInfo> infos = rootNodeInfo.findAccessibilityNodeInfosByText("发现");
                List<AccessibilityNodeInfo> pyqs = rootNodeInfo.findAccessibilityNodeInfosByText("朋友圈");
                if (pyqs != null && pyqs.size() > 0) {
                    if (infos != null && infos.size() > 0) {
                        clickByNode(pyqs.get(0));
                    } else {
                        //处理朋友圈
                        autoLike(rootNodeInfo);
                    }
                } else {
                    if (infos != null && infos.size() > 0) {
                        clickByNode(infos.get(0));
                    } else {
                        pressBackButton();
                    }
                }
//                Log.i("TAG", "结束");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void autoLike(AccessibilityNodeInfo rootNodeInfo) {
        description = "";
        if (rootNodeInfo.getContentDescription() != null) {
            description = rootNodeInfo.getContentDescription().toString();
        }

        //自动点赞流程
        if (mUserName.equals("")) {
            //Lv
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                lvs = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cn0");
            }
            Logger.d("找到的Lv数量: " + lvs.size());
            //如果size不为0，证明当前在朋友圈页面下,开始执行逻辑
            if (lvs.size() != 0) {
                //1.先记录用户名
                List<AccessibilityNodeInfo> userNames =
                        null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    userNames = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/afa");
                }
                if (userNames.size() != 0) {
                    if (userNames.get(0).getParent() != null && userNames.get(0).getParent().getChildCount() == 4) {
                        mUserName = userNames.get(0).getText().toString();
                        if (!mUserName.equals("") && !ifOnce) {
                            Logger.d("初始化，只会执行一次");
                            Logger.d("当前的用户名:" + mUserName);
                            ifOnce = true;
                            //测试朋友圈点赞
                            test3(rootNodeInfo);
                        }
                    }
                }
            } else {
                ifOnce = false;
                mUserName = "";
            }
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        }
//        AccessibilityEvent.getSource().performAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 根据文本查找节点
     *
     * @param root
     * @param key
     * @return
     */
    public AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo root, String key) {
        if (root.getChildCount() == 0) {
            Logger.i(root.getText() != null ? root.getText().toString() : "无文本");
            if (root.getText() != null && root.getText().toString().contains(key)) {
                Logger.i("TAG", root.getText().toString());
                return root;
            }
        } else {
            // 当 当前节点 有子控件时，解析它的孩子，以此递归
            for (int i = 0; i < root.getChildCount(); i++) {
                if (root.getChild(i) != null) {
                    return findNodeByText(root.getChild(i), key);
                }
            }
        }
        return null;
    }

    public void clickByNode(AccessibilityNodeInfo info) {
        AccessibilityNodeInfo parent = info;
        while (parent != null) {
            if (parent.isClickable()) {
                // 模拟点击，跳出循环
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            parent = parent.getParent();
        }
    }

    private boolean handle(AccessibilityNodeInfo info, String key) {
        // 判断节点是否有子控件
        if (info.getChildCount() == 0) {
            try {
                Logger.d(info.getClassName().toString() + (info.getText() == null ? "" : info.getText().toString()));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            // 判断节点是否有文字并且有“搜索”文字
            if (info.getText() != null && info.getText().toString().contains(key)) {
                AccessibilityNodeInfo parent = info;
                while (parent != null) {
                    if (parent.isClickable()) {
                        // 模拟点击，跳出循环
                        //       parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
//
//                // 美团的第一个页面布局：判断节点是否为TextView，文字是否匹配
//                if ("搜索商家、品类或商圈".equals(info.getText().toString()) && "android.widget.TextView".equals(info.getClassName())) {
//                    // 判断节点控件是否可点击，不可点击则点击其父布局，以此类推。
//                    AccessibilityNodeInfo parent = info;
//                    while (parent != null) {
//                        if (parent.isClickable()) {
//                            // 模拟点击，跳出循环
//                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            break;
//                        }
//                        parent = parent.getParent();
//                    }
//
//                }  // 对第二个布局进行判断：判断是否为一个EidtText对象
//                else if ("搜索商家、品类或商圈".equals(info.getText().toString()) && "android.widget.EditText".equals(info.getClassName())) {
//
//                    // 以剪贴板的形式进行文字输入
//                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//                    ClipData clipData = ClipData.newPlainText("scb", MyApplication.getInstance().getParams());
//                    clipboardManager.setPrimaryClip(clipData);
//
//                    // 模拟粘贴
//                    info.performAction(AccessibilityNodeInfo.ACTION_PASTE);
//
//                } // 对第三个布局进行判断：判断是否为一个文字为“搜索”的TextView
//                else if ("搜索".equals(info.getText().toString()) && "android.widget.TextView".equals(info.getClassName())) {
//
//                    // 以同样的原理进行控件点击
//                    AccessibilityNodeInfo parent = info;
//                    while (parent != null) {
//                        if (parent.isClickable()) {
//                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            break;
//                        }
//                        parent = parent.getParent();
//                    }
//
//                    // 服务进行结束后，设置Flag
//                    MyApplication.getInstance().setFlag(false);
//                    return true;
//                } else {
//                    // 其他条件下设置Flag
//                    MyApplication.getInstance().setFlag(false);
//                }
            }

        } else {
            // 当 当前节点 有子控件时，解析它的孩子，以此递归
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    handle(info.getChild(i), key);
                }
            }
        }
        return false;
    }


    String mUserName = "";
    private boolean ifOnce = false;

    /**
     * com.tencent.mm:id/cn0
     * 朋友圈点赞 (目前实现手动滚动全部点赞)
     * 上方固定显示的名字：com.tencent.mm:id/afa
     * 下方点赞：显示id：com.tencent.mm:id/cnn
     * 每发现一个【评论按钮】，就去搜索当前同父组件下的点赞区域有没有自己的ID。
     * 如果有就不点赞，如果没有就点赞
     * 这里要改成不通过Id抓取提高稳定性
     *
     * @param rootNodeInfo
     */
    private synchronized void test3(AccessibilityNodeInfo rootNodeInfo) {
        Logger.d("当前线程:" + Thread.currentThread());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        topList.clear();

        if (!mUserName.equals("")) {

            //测试获得评论按钮的父节点，再反推出点赞按钮
            List<AccessibilityNodeInfo> fuBtns =
                    null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                fuBtns = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/co0");
            }

            Logger.d("fuBtns数量：" + fuBtns.size());

            if (fuBtns.size() != 0) {

                //删掉超出屏幕的fuBtn
                AccessibilityNodeInfo lastFuBtn = fuBtns.get(fuBtns.size() - 1);
                Rect lastFuBtnOutBound = new Rect();
                lastFuBtn.getBoundsInScreen(lastFuBtnOutBound);
                if (lastFuBtnOutBound.top > Config.height) {
                    fuBtns.remove(lastFuBtn);
                }

                for (int i = 0; i < fuBtns.size(); i++) {
                    AccessibilityNodeInfo fuBtn = fuBtns.get(i);
                    Logger.d("fuBtn的子节点数量:" + fuBtn.getChildCount());//3-4个
                    List<AccessibilityNodeInfo> plBtns = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        plBtns = fuBtn.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cj9");
                    }
                    Logger.d("从这里发现评论按钮:" + plBtns.size());

                    if (plBtns.size() == 0) {
                        if (lvs.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                test3(getRootInActiveWindow());
                            }
                        }
                        return;
                    }

                    AccessibilityNodeInfo plbtn = plBtns.get(0);    //评论按钮
                    List<AccessibilityNodeInfo> zanBtns = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        zanBtns = fuBtn.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cnn");
                    }
                    Logger.d("从这里发现点赞文字显示区域:" + zanBtns.size());
                    if (zanBtns.size() != 0) {
                        //2.如果不为空，则查找有没有自己点过赞，有则不点，没有则点
                        AccessibilityNodeInfo zanbtn = zanBtns.get(0);
                        Logger.d("点赞的人是:" + zanbtn.getText().toString());
                        if (zanbtn != null && zanbtn.getText() != null &&
                                zanbtn.getText().toString().contains(mUserName)) {
                            Logger.d("*********************这一条已经被赞过辣");
                            //判断是否需要翻页，如果当前所有页面的父节点都没点过了，就需要翻页
                            boolean ifxuyaofanye = false;
                            Logger.d("Ｏ(≧口≦)Ｏ: i=" + i + "  fuBtns.size():" + fuBtns.size());
                            if (i == fuBtns.size() - 1) {
                                ifxuyaofanye = true;
                            }
                            if (ifxuyaofanye) {
                                //滑动前检测一下是否还有没有点过的点
                                if (jianceIfLou()) {
                                    Logger.d("还有遗漏的点！！！！再检查一遍!!!!!!!!!!");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        test3(getRootInActiveWindow());
                                    }
                                } else {
                                    if (lvs.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            test3(getRootInActiveWindow());
                                        }
                                        return;
                                    }
                                }
                            }

                        } else {
                            Logger.d("**************************:自己没有赞过!");
                            //开始执行点赞流程
                            if (plBtns.size() != 0) {
                                Rect outBounds = new Rect();
                                plbtn.getBoundsInScreen(outBounds);
                                int top = outBounds.top;

                                //根据top判断如果已经点开了就不重复点开了
                                if (topList.contains(top)) {
                                    return;
                                }
                                //com.tencent.mm:id/cj5 赞
                                if (plbtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                    List<AccessibilityNodeInfo> zanlBtns = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                        zanlBtns = rootNodeInfo.
                                                findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cj3");
                                    }
                                    if (zanlBtns.size() != 0) {
                                        if (!topList.contains(top) && zanlBtns.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                            topList.add(top);
                                            Logger.d("topList:" + topList.toString());

                                            //判断是否需要翻页，如果当前所有页面的父节点都没点过了，就需要翻页
                                            boolean ifxuyaofanye = false;
                                            Logger.d("Ｏ(≧口≦)Ｏ: i=" + i + "  fuBtns.size():" + fuBtns.size());
                                            if (i == fuBtns.size() - 1) {
                                                ifxuyaofanye = true;
                                            }
                                            if (ifxuyaofanye) {
                                                //滑动前检测一下是否还有没有点过的点
                                                if (jianceIfLou()) {
                                                    Logger.d("还有遗漏的点！！！！再检查一遍!!!!!!!!!!");
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                        test3(getRootInActiveWindow());
                                                    }
                                                } else {
                                                    if (lvs.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                            test3(getRootInActiveWindow());
                                                        }
                                                        return;
                                                    }
                                                }


                                            }

                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        Logger.d("**************************:点赞区域为空!plBtns.size() :" + plBtns.size());

                        //开始执行点赞流程
                        if (plBtns.size() != 0) {

                            Rect outBounds = new Rect();
                            plbtn.getBoundsInScreen(outBounds);
                            int top = outBounds.top;

                            //根据top判断如果已经点开了就不重复点开了
                            if (topList.contains(top)) {
                                return;
                            }
                            //com.tencent.mm:id/cj5 赞
                            if (plbtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                List<AccessibilityNodeInfo> zanlBtns = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                    zanlBtns = rootNodeInfo.
                                            findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cj3");
                                }
                                if (zanlBtns.size() != 0) {
                                    if (!topList.contains(top) && zanlBtns.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                        topList.add(top);
                                        Logger.d("topList:" + topList.toString());

                                        //判断是否需要翻页，如果当前所有页面的父节点都没点过了，就需要翻页
                                        boolean ifxuyaofanye = false;
                                        Logger.d("Ｏ(≧口≦)Ｏ: i=" + i + "  fuBtns.size():" + fuBtns.size());
                                        if (i == fuBtns.size() - 1) {
                                            ifxuyaofanye = true;
                                        }
                                        if (ifxuyaofanye) {
                                            //滑动前检测一下是否还有没有点过的点
                                            if (jianceIfLou()) {
                                                Logger.d("还有遗漏的点！！！！再检查一遍!!!!!!!!!!");
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                    test3(getRootInActiveWindow());
                                                }
                                            } else {
                                                if (lvs.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                        test3(getRootInActiveWindow());
                                                    }
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }


    private boolean jianceIfLou() {
        boolean result = false;
        List<AccessibilityNodeInfo> fuBtns =
                null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                fuBtns = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/co0");
            }
        }
        Logger.d("检查的父节点数量:" + fuBtns.size());
        if (fuBtns.size() != 0) {
            for (AccessibilityNodeInfo fuBtn : fuBtns) {
                //点赞区域
                List<AccessibilityNodeInfo> zanBtns = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    zanBtns = fuBtn.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cnn");
                }
                Logger.d("检查的父节点的点赞区域数量:" + zanBtns.size());
                if (zanBtns.size() != 0) {
                    AccessibilityNodeInfo zanbtn = zanBtns.get(0);
                    Logger.d(" zanbtn.getText().toString():" + zanbtn.getText().toString());
                    if (zanbtn != null && zanbtn.getText() != null &&
                            zanbtn.getText().toString().contains(mUserName)) {
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }
        }

        return result;
    }


    @Override
    public void onInterrupt() {
        Logger.d("onInterrupt");
    }
}
