package com.ks.plugin.widget.launcher.print;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.ks.plugin.widget.launcher.Html2MHTCompiler;
import com.ks.plugin.widget.launcher.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicketActivity extends AppCompatActivity implements View.OnClickListener {
    WebView webView;
    Button vprint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        init();
    }

    private void init() {
        vprint = (Button) findViewById(R.id.vprint);
        vprint.setOnClickListener(this);
//        webView = new WebView(this);
//        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView = (WebView) findViewById(R.id.vweb);
        webView.setWebViewClient(new AndroidWebClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        webView.getSettings().setAppCacheEnabled(true);
        //设置 缓存模式
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 开启 DOM storage API 功能
        webView.getSettings().setDomStorageEnabled(true);
        //设置允许跨域
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        webView.loadUrl("file:///android_asset/print/test.html");
    }

    private String doReplace(String html, Map<String, String> keys) {
        String content = html;
        if (keys != null) {
            for (Map.Entry<String, String> entry : keys.entrySet()) {
                try {
                    //repaceAll是替换正则表达式,replace是替换字符串
//                    content = Pattern.compile("{{" + entry.getKey() + "}}", Pattern.CASE_INSENSITIVE).matcher(content).replaceAll(entry.getValue());
                    content=content.replace("${{" + entry.getKey() + "}}$", entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    Handler handler = new MyHandler();

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vprint:
                doPrintPDF();
                break;
        }
    }


    public void doPrintPDF() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Get a PrintManager instance
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            // Get a print adapter instance
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + "--" + webView.getTitle();
            printManager.print(jobName, printAdapter,
                    new PrintAttributes.Builder().build());
        } else {
            showToast("当前系统不支持该功能");
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Map<String, String> map;
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    showToast("保存成功");
                    break;
                case 2:
                    map = (HashMap<String, String>) msg.obj;
                    String fname = map.get("fname");
                    String mht = map.get("mht");
                    Html2MHTCompiler.mht2html(fname, fname.replace(".mht", ".html"), "index_files");
                    handler.sendEmptyMessage(1);
                    break;
                case 3:
                    break;
                case 4:
                    map = (HashMap<String, String>) msg.obj;
                    Map<String, String> keys = new HashMap<>();
                    keys.put("name", "张三");
                    keys.put("idcardno", "370102199902123456");
                    keys.put("ticketno", "A001");
                    keys.put("datetime", System.currentTimeMillis() + "");
                    keys.put("servicename", "综合服务");
                    keys.put("countnum", "12");
                    String html = doReplace(map.get("body"), keys);
                    Log.i("Replace", html);
                    webView.loadUrl("javascript:document.getElementsByTagName('body')[0].innerHTML='" + html + "';");
                    break;
            }
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
            //htmlToText.convert2(html);
            //htmlToText.delHTMLTag(html);
            handler.sendMessage(message);
        }

        @JavascriptInterface
        public void getBodyHtml(String html) {
            Log.d("html=", html);
            Message message = new Message();
            message.what = 4;
            Map<String, String> map = new HashMap<>();
            map.put("body", html);
            message.obj = map;
            handler.sendMessage(message);
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
            view.loadUrl("javascript:window.java_obj.getBodyHtml(document.getElementsByTagName('body')[0].innerHTML);");
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
