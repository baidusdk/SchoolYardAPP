package com.hd.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 全景图活动
 * http://yiban.fzu.edu.cn/m/pcindex.html
 */
public class PanoramaActivity extends AppCompatActivity {


    public static final String url = "http://yiban.fzu.edu.cn/m/pcindex.html";
    private WebView webView;
    private TextView titleName;
    private ProgressBar webViewInitProgress;
    private final int LOAD_WEB_VIEW = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("福大全景图");
        webViewInitProgress = (ProgressBar) findViewById(R.id.web_load_progress);
        webViewInitProgress.setVisibility(View.VISIBLE);
        webView = (WebView) findViewById(R.id.web_view);
        initWebView();
        webViewInitProgress.setVisibility(View.GONE);
    }

    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

}







//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//            if(webView.canGoBack()) {
//                //获取webView的浏览记录
//                WebBackForwardList mWebBackForwardList = webView.copyBackForwardList();
//                //这里的判断是为了让页面在有上一个页面的情况下，跳转到上一个html页面，而不是退出当前activity
//                if (mWebBackForwardList.getCurrentIndex() > 0) {
//                    String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() - 1).getUrl();
//                    if (!historyUrl.equals(url)) {
//                        webView.goBack();
//                        return true;
//                    }
//                }
//            } else {
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

