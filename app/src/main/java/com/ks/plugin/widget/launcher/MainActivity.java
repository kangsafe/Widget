package com.ks.plugin.widget.launcher;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ks.plugin.widget.launcher.changba.PersonHeadActivity;
import com.ks.plugin.widget.note.NoteAppWidgetProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener {
    Button btn;
    Button btnPhoto;
    EditText tx;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.saveWeb);
        btnPhoto = findViewById(R.id.vtitlephoto);

        btn.setOnClickListener(this);
        btnPhoto.setOnClickListener(this);
        tx = findViewById(R.id.vurl);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            Log.i(TAG, "HOST:" + uri.getHost());
            Log.i(TAG, "PATH:" + uri.getPath());
            Log.i(TAG, "PARAM:" + uri.getQueryParameter("guid"));
        }
        init();
    }

    NoteAppWidgetProvider mBroadcastReceiver;

    @Override
    protected void onResume() {
        super.onResume();
//        // 1. 实例化BroadcastReceiver子类 &  IntentFilter
//        mBroadcastReceiver = new NoteAppWidgetProvider();
//        IntentFilter intentFilter = new IntentFilter();
//
//        // 2. 设置接收广播的类型
//        intentFilter.addAction("android.appwidget.action.APPWIDGET_UPDATE");
////        intentFilter.addAction("com.seewo.homewidgets.action.CLICK");
//        // 3. 动态注册：调用Context的registerReceiver（）方法
//        registerReceiver(mBroadcastReceiver, intentFilter);
        registerClipEvents(this);
    }

    // 注册广播后，要在相应位置记得销毁广播
// 即在onPause() 中unregisterReceiver(mBroadcastReceiver)
// 当此Activity实例化时，会动态将MyBroadcastReceiver注册到系统中
// 当此Activity销毁时，动态注册的MyBroadcastReceiver将不再接收到相应的广播。
    @Override
    protected void onPause() {
        super.onPause();
//        //销毁在onResume()方法中的广播
//        unregisterReceiver(mBroadcastReceiver);
        unregisterClipEvents();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveWeb:
                saveWebHtml(tx.getText().toString());
                break;
            case R.id.vtitlephoto:
                Intent intent = new Intent();
                intent.setClass(this, PersonHeadActivity.class);
//                intent.putExtra("sid", adapter.itemList.get(position).getSid());
                startActivity(intent);
                break;
        }
    }

    WebView webView;

    private void init() {
        webView = new WebView(this);
//        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        WebView webView = (WebView) findViewById(R.id.vweb);
        webView.setWebViewClient(new AndroidWebClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        webView.getSettings().setAppCacheEnabled(true);
        //设置 缓存模式
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        webView.getSettings().setDomStorageEnabled(true);
    }

    private void saveWebHtml(String url) {

        if (url == null || url.isEmpty()) {
            webView.loadUrl("http://www.baidu.com");
        } else {
            webView.loadUrl(url);
        }
    }

    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void getSource(String html) {
            Log.d("html=", html);
            Message message = new Message();
            message.what = 3;
            Map<String, String> map = new HashMap<>();
            map.put("html", html);
            map.put("text", html);
            HtmlToText htmlToText = new HtmlToText();
            htmlToText.Convert(html);
            htmlToText.convert2(html);
            htmlToText.delHTMLTag(html);
            handler.sendMessage(message);
        }
    }


    ClipboardManager manager;

    private void registerClipEvents(Context context) {

        manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);

        manager.addPrimaryClipChangedListener(this);
    }

    private void unregisterClipEvents() {
        manager.removePrimaryClipChangedListener(this);
    }


    private long previousTime = 0;

    @Override
    public void onPrimaryClipChanged() {
        long now = System.currentTimeMillis();
        if (now - previousTime < 200) {
            previousTime = now;
            return;
        }
        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
            CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
            if (addedText != null) {
                Log.i(TAG, "copied text: " + addedText);
            }
            //updateWidget(addedText.toString(), "");
            tx.setText(addedText);
        }

        previousTime = now;
    }

    Handler handler = new MyHandler();

    private void showToast() {
        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    showToast();
                    break;
                case 2:
                    Map<String, String> map = (HashMap<String, String>) msg.obj;
                    String fname = map.get("fname");
                    String mht = map.get("mht");
                    Html2MHTCompiler.mht2html(fname, fname.replace(".mht", ".html"), "index_files");
                    handler.sendEmptyMessage(1);
                    break;
                case 3:
                    break;
            }
        }
    }

    private class AndroidWebClient extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }

        @Override
        public void onPageStarted(WebView view, String url,
                                  android.graphics.Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.java_obj.getSource('<html>'+" +
                    "document.getElementsByTagName('html')[0].innerHTML+'</html>');");

            final File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "kwidget" + File.separator + UUID.randomUUID().toString(), "index.mht");
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            view.saveWebArchive(f.getAbsolutePath());
            view.saveWebArchive(f.getAbsolutePath(), false, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.i("TAG", s);
                    Message message = new Message();
                    message.what = 2;
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("fname", f.getAbsolutePath());
                    map.put("mht", s);
                    message.obj = map;
                    handler.sendMessage(message);
                }
            });
            //            view.saveWebArchive(f.getAbsolutePath().replace(".mht", ".html"));
//            Html2MHTCompiler h2t = new Html2MHTCompiler(strText, strUrl, strEncoding, "test.mht");
//        h2t.compile();
            super.onPageFinished(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    }

    public class HtmlToText {
        public String Convert(String html) {
            System.out.println("去除文本1");
            String result;

            //remove line breaks,tabs
            result = Pattern.compile("/r", Pattern.CASE_INSENSITIVE).matcher(html).replaceAll(" ");
            result = Pattern.compile("/n", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(" ");
            result = Pattern.compile("/t", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(" ");

            //remove the header
            result = Pattern.compile("<head[^>]*?>[\\s\\S]*?</head>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("");
            //remove style
            result = Pattern.compile("<style[^>]*?>[\\s\\S]*?</style>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("");
            //remove script
            result = Pattern.compile("<script[^>]*?>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("");
            //insert tabs in spaces of <td> tags
            result = Pattern.compile("<td[^>]*?>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll(" ");
            //insert line breaks in places of <br> and <li> tags
            result = Pattern.compile("<br[^>]*?>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("/r");
            result = Pattern.compile("<li[^>]*?>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("/r");
            //insert line paragraphs in places of <tr> and <p> tags
            result = Pattern.compile("<tr[^>]*?>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("/r/r");
            result = Pattern.compile("<p[^>]*?>", Pattern.CASE_INSENSITIVE).matcher(result).replaceAll("/r/r");
            //remove anything thats enclosed inside < >
            result = result.replaceAll("<[^>]*>", "");

            //replace special characters:
            result = result.replaceAll("&amp;", "&");
            result = result.replaceAll("&nbsp;", " ");
            result = result.replaceAll("&lt;", "<");
            result = result.replaceAll("&gt;", ">");
            result = result.replaceAll("&(.{2,6});", "");

            //remove extra line breaks and tabs
            result = result.replaceAll("( )+", " ");
            result = result.replaceAll("/r( )+(/r)", "/r/r");
            result = result.replaceAll("(/r/r)+", "/r/n");
            System.out.println(result);
            return result;
        }

        //将html转换为纯文本，此方法最后保留了&nbps空格，使用时注意将空格替换掉
        public String delHTMLTag(String htmlStr) {
            System.out.println("去除文本3");
            String regEx_script = "<script[^>]*?>[\\s\\S]*?</script>"; //定义script的正则表达式
            String regEx_style = "<style[^>]*?>[\\s\\S]*?</style>"; //定义style的正则表达式
            String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

            Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            Matcher m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); //过滤script标签

            Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            Matcher m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); //过滤style标签

            Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            Matcher m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); //过滤html标签

            //删除HTML
            htmlStr = Pattern.compile("<(.[^>]*)>", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("");
            htmlStr = Pattern.compile("([\\r\\n])[\\s]+", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("");
            htmlStr = Pattern.compile("-->", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("");
            htmlStr = Pattern.compile("<!--.*", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("");

            htmlStr = Pattern.compile("&(quot|#34);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("\"");
            htmlStr = Pattern.compile("&(amp|#38);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("&");
            htmlStr = Pattern.compile("&(lt|#60);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("<");
            htmlStr = Pattern.compile("&(gt|#62);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll(">");
            htmlStr = Pattern.compile("&(nbsp|#160);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("   ");
            htmlStr = Pattern.compile("&(iexcl|#161);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("\\xa1");
            htmlStr = Pattern.compile("&(cent|#162);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("\\xa2");
            htmlStr = Pattern.compile("&(pound|#163);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("\\xa3");
            htmlStr = Pattern.compile("&(copy|#169);", Pattern.CASE_INSENSITIVE).matcher(htmlStr).replaceAll("\\xa9");
            htmlStr = htmlStr.replaceAll("&#(\\d+);", "");

            htmlStr = htmlStr.replaceAll("<", "");
            htmlStr = htmlStr.replaceAll(">", "");
            htmlStr = htmlStr.replaceAll("\r\n", "");

            System.out.println(htmlStr);
            return htmlStr.trim(); //返回文本字符串
        }

        public String convert2(String html) {
            System.out.println("去除文本2");
            String reg = "<[a-zA-Z]+.*?>([\\s\\S]*?)</[a-zA-Z]*>";
            Pattern p = Pattern.compile(reg, Pattern.MULTILINE);
            String str = html.replace("&nbsp;", " ");
            Matcher m = p.matcher(str);
            while (m.find()) {
                String data = m.group(1).trim();
                if (!"".equals(data)) {
                    System.out.println(data);
                }
            }
            return "";
        }

    }
}
