package peter.util.searcher;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import java.io.File;
import java.util.Observable;

import peter.util.searcher.download.MyDownloadListener;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int API = android.os.Build.VERSION.SDK_INT;
    private WebView webview;
    private View status, bottomBar;
    private String searchWord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        status = findViewById(R.id.status);
        bottomBar = findViewById(R.id.bottom_bar);
        webview = (WebView) findViewById(R.id.wv);
        webview.setDrawingCacheBackgroundColor(Color.WHITE);
        webview.setFocusableInTouchMode(true);
        webview.setFocusable(true);
        webview.setDrawingCacheEnabled(false);
        webview.setWillNotCacheDrawing(true);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            webview.setAnimationCacheEnabled(false);
            //noinspection deprecation
            webview.setAlwaysDrawnWithCacheEnabled(false);
        }
        webview.setBackgroundColor(Color.WHITE);
        webview.setScrollbarFadingEnabled(true);
        webview.setSaveEnabled(true);
        webview.setNetworkAvailable(true);

        setUserAgent(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
        }

        WebChromeClient mWebChromeClient = new MyWebChromeClient(this);
        webview.setWebChromeClient(mWebChromeClient);
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
        webview.setDownloadListener(new MyDownloadListener(this));
        initializeSettings();
        checkIntentData(getIntent());
    }

    /**
     * Initialize the settings of the WebView that are intrinsic to Lightning and cannot
     * be altered by the user. Distinguish between Incognito and Regular tabs here.
     */
    @SuppressLint("NewApi")
    private void initializeSettings() {
        if (webview == null) {
            return;
        }
        final WebSettings settings = webview.getSettings();
        if (API < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            settings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        if (API < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection deprecation
            settings.setEnableSmoothTransition(true);
        }
        if (API > Build.VERSION_CODES.JELLY_BEAN) {
            settings.setMediaPlaybackRequiresUserGesture(true);
        }
        if (API >= Build.VERSION_CODES.LOLLIPOP) {
            // We're in Incognito mode, reject
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        if (API >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
    }

    private void setStatusLevel(int level) {
        LevelListDrawable d = (LevelListDrawable) status.getBackground();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusColor(int color) {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(color);
    }

    private int curShowColor;

    public void setMainColor(Bitmap favicon) {
        Palette.from(favicon).generate(new Palette.PaletteAsyncListener() {

            @Override
            public void onGenerated(Palette palette) {

                int defaultColor = getResources().getColor(R.color.colorPrimary);
                int curColor = palette.getVibrantColor(defaultColor);
                if (curShowColor != curColor) {
                    curShowColor = curColor;
                    final int startSearchColor = getSearchBarColor(defaultColor);
                    final int finalSearchColor = getSearchBarColor(curColor);
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startSearchColor, finalSearchColor);
                    colorAnimation.setDuration(250); // milliseconds
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int animColor = (int) animator.getAnimatedValue();
                            bottomBar.setBackgroundColor(animColor);
                            if (API >= 21) {
                                setStatusColor(animColor);
                            }
                        }

                    });
                    colorAnimation.start();
                }
            }
        });
    }

    private int getSearchBarColor(int requestedColor) {
        return DrawableUtils.mixColor(0.25f, requestedColor, Color.WHITE);
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if("peter.util.searcher".equals(action)) { // inner invoke
                String url = (String) intent.getSerializableExtra("url");
                if (!TextUtils.isEmpty(url)) {
                    searchWord = intent.getStringExtra("word");
                    loadUrl(url);
                }
            }else if(Intent.ACTION_MAIN.equals(action)) {//launcher invoke
                if(intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
                    Intent startIntent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(startIntent);
                    finish();
                }
            }else if(Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if(!TextUtils.isEmpty(url)) {
                    loadUrl(url);
                }
            }
        }
    }

    private void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            Log.i("peter", "url=" + url);
            webview.loadUrl(url);
            if (!TextUtils.isEmpty(searchWord)) {
                saveData(searchWord, url);
            }
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
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
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
            String def = WebSettings.getDefaultUserAgent(context);
            settings.setUserAgentString(def);
        } else {
            settings.setUserAgentString(settings.getUserAgentString());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i("peter", "onConfigurationChanged ORIENTATION_LANDSCAPE");
                bottomBar.setVisibility(View.GONE);
                setFullscreen(true, true);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i("peter", "onConfigurationChanged ORIENTATION_PORTRAIT");
                bottomBar.setVisibility(View.VISIBLE);
                setFullscreen(false, false);
                break;
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

    /**
     * @param enabled   status bar
     * @param immersive
     */
    public void setFullscreen(boolean enabled, boolean immersive) {
        Window window = getWindow();
        View decor = window.getDecorView();
        if (enabled) {
            if (immersive) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }


}
