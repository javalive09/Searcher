package peter.util.searcher;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int API = android.os.Build.VERSION.SDK_INT;
    private WebView webview;
    private View status;
    private String searchWord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        status = findViewById(R.id.status);
        webview = (WebView) findViewById(R.id.wv);
        if (Build.VERSION.SDK_INT < 21) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);
        setUserAgent(this);

        WebChromeClient mWebchromeclient = new WebChromeClient();
        webview.setWebChromeClient(mWebchromeclient);
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                // Ignore SSL certificate errors
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setStatusLevel(1);
                Log.i("peter", "url=" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setStatusLevel(0);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        checkIntentData(getIntent());
    }

    private void setStatusLevel(int level) {
        LevelListDrawable d = (LevelListDrawable) status.getBackground();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String url = (String) intent.getSerializableExtra("url");
            if (!TextUtils.isEmpty(url)) {
                searchWord = intent.getStringExtra("word");
                loadUrl(url);
            }
        }
    }

    private void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            Log.i("peter", "url=" + url);
            webview.loadUrl(url);
            saveData(searchWord, url);
        }
    }

    private void saveData(final String word, final String url) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bean search = new Bean();
                search.name = word;
                search.time = System.currentTimeMillis();
                search.url = url;
                SqliteHelper.instance(getApplicationContext()).insertHistory(search);
                return null;
            }
        }.execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (webview != null && webview.canGoBack()) {
                webview.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onResume() {
        super.onResume();
        webview.onResume();
        webview.resumeTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webview.onPause();
        webview.pauseTimers();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i("peter", "onConfigurationChanged ORIENTATION_LANDSCAPE");

//                frame.hideBar();
                toggleFullscreen(true);

//                View title = findViewById(R.id.title);
//                if(title.getVisibility() == View.VISIBLE) {
//                    title.setVisibility(View.GONE);
//                    toggleFullscreen(true);
//                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i("peter", "onConfigurationChanged ORIENTATION_PORTRAIT");
//                title = findViewById(R.id.title);
//                if(title.getVisibility() != View.VISIBLE) {
//                    title.setVisibility(View.VISIBLE);
//                    toggleFullscreen(false);
//                }

//                frame.showBar();
                toggleFullscreen(false);
                break;
        }
    }

    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (webview.canGoBack()) {
                    webview.goBack();
                }
                break;
            case R.id.go:
                if (webview.canGoForward()) {
                    webview.goForward();
                }
                break;
            case R.id.close:
                finish();
                break;
            case R.id.favorite:
                if (webview.getUrl() != null) {
                    Bean bean = new Bean();
                    bean.name = getFavName(webview.getUrl());
                    bean.url = webview.getUrl();
                    bean.time = System.currentTimeMillis();
                    SqliteHelper.instance(MainActivity.this).insertFav(bean);
                    Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.share:
                if (webview.getUrl() != null) {
                    String url = webview.getUrl();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
                }
                break;
        }
    }

    public String getFavName(String engineName) {
        String word = searchWord;
        String title = webview.getTitle();
        Log.i("peter", "title = " + title);
        Log.i("peter", "engineName = " + engineName);
        if (getString(R.string.url_title_mark_cb).equals(engineName)) {//词霸
            title = word + " - " + engineName;
        } else if (getString(R.string.url_title_mark_yd).equals(engineName)) {//有道
            title = word + " - " + title;
        } else if (getString(R.string.url_title_mark_jd).equals(engineName)) {//京东
            title = word + " - " + engineName;
        } else if (getString(R.string.url_title_mark_tb).equals(engineName)) {//淘宝
            title = word + " - " + engineName;
        } else if (getString(R.string.url_title_mark_tx).equals(engineName)) {//腾讯视频
            title = word + " - " + engineName + " - " + title;
        } else if (getString(R.string.url_title_mark_sh).equals(engineName)) {//搜狐视频
            title = word + " - " + engineName + " - " + title;
        } else if (getString(R.string.url_title_mark_aqy).equals(engineName)) {//爱奇艺
            title = word + " - " + engineName + " - " + title;
        } else if (getString(R.string.url_title_mark_yyb).equals(engineName)) {//应用宝
            title = word + " - " + engineName;
        } else if (getString(R.string.url_title_mark_360zs).equals(engineName)) {//360助手
            title = word + " - " + engineName + " - " + title;
        } else if (getString(R.string.url_title_mark_bdzs).equals(engineName)) {//百度助手
            title = word + " - " + engineName + " - " + title;
        } else if (getString(R.string.url_title_mark_xm).equals(engineName)) {//小米
            title = word + " - " + engineName + " - " + title;
        }
        Log.i("peter", "title = " + title);
        return title;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setUserAgent(Context context) {
        if (webview == null) return;
        WebSettings settings = webview.getSettings();
        if (API >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setUserAgentString(WebSettings.getDefaultUserAgent(context));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webview != null) {
            // Check to make sure the WebView has been removed
            // before calling destroy() so that a memory leak is not created
            ViewGroup parent = (ViewGroup) webview.getParent();
            if (parent != null) {
                parent.removeView(webview);
            }
            webview.stopLoading();
            webview.onPause();
            webview.clearHistory();
            webview.setVisibility(View.GONE);
            webview.removeAllViews();
            webview.destroyDrawingCache();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //this is causing the segfault occasionally below 4.2
                webview.destroy();
            }
            webview = null;
        }
    }

}
