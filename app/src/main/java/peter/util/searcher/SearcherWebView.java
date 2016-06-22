package peter.util.searcher;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import peter.util.searcher.download.MyDownloadListener;

/**
 * Created by peter on 16/5/9.
 */
public class SearcherWebView {

    private static final int API = Build.VERSION.SDK_INT;
    private WebView webview;
    private View progressBar;
    private MainActivity activity;
    private String searchWord;
    private String url;
    private View rootView;
    private int curShowColor;
    private int mainColor = -1;

    public SearcherWebView(MainActivity activity, String url, String searchWord) {
        this.activity = activity;
        this.searchWord = searchWord;
        this.url = url;
        init();
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void loadUrl(MainActivity activity, String url, String searchWord) {
        this.activity = activity;
        this.searchWord = searchWord;
        this.url = url;
        loadUrl(url);
    }

    private void init() {
        rootView = View.inflate(activity, R.layout.searcher_webview, null);
        webview = (WebView) rootView.findViewById(R.id.wv);
        progressBar = rootView.findViewById(R.id.status);
        initWebview(webview);
        initializeSettings(webview);
        loadUrl(url);
    }

    public Bitmap getThumbNail() {
        if (webview != null) {
            return webview.getDrawingCache();
        }
        return null;
    }

    public int getMainColor() {
        if(mainColor == -1) {
            return activity.getResources().getColor(R.color.colorPrimary);
        }
        return mainColor;
    }

    public void setMainColor(Bitmap favicon) {
        Palette.from(favicon).generate(new Palette.PaletteAsyncListener() {

            @Override
            public void onGenerated(Palette palette) {
                int defaultColor = activity.getResources().getColor(R.color.colorPrimary);
                int curColor = palette.getVibrantColor(defaultColor);
                if (curShowColor != curColor) {
                    curShowColor = curColor;
                    mainColor = getSearchBarColor(curColor);
                    int startSearchColor = getSearchBarColor(defaultColor);
                    if (startSearchColor != mainColor) {
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startSearchColor, mainColor);
                        colorAnimation.setDuration(300); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                int animColor = (int) animator.getAnimatedValue();
                                activity.setBottomBarColor(animColor);
                                if (API >= 21) {
                                    activity.setStatusColor(animColor);
                                }
                            }

                        });
                        colorAnimation.start();
                    }
                }
            }
        });
    }

    private int getSearchBarColor(int requestedColor) {
        return DrawableUtils.mixColor(0.25f, requestedColor, Color.WHITE);
    }

    public View getRootView() {
        return rootView;
    }

    private void initWebview(WebView webview) {
        webview.setDrawingCacheBackgroundColor(Color.WHITE);
        webview.setFocusableInTouchMode(true);
        webview.setFocusable(true);
        webview.setDrawingCacheEnabled(true);
        webview.setWillNotCacheDrawing(false);
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

        setUserAgent(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
        }

        WebChromeClient mWebChromeClient = new MyWebChromeClient(this, activity);
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
                setOptStatus(view);
                Log.i("peter", "url=" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setStatusLevel(0);
                setOptStatus(view);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webview.setDownloadListener(new MyDownloadListener(activity));
    }

    /**
     * Initialize the settings of the WebView that are intrinsic to Lightning and cannot
     * be altered by the user. Distinguish between Incognito and Regular tabs here.
     */
    @SuppressLint("NewApi")
    private void initializeSettings(WebView webview) {
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
        LevelListDrawable d = (LevelListDrawable) progressBar.getBackground();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    private void setOptStatus(WebView view) {
        if (view.canGoBack()) {
            activity.findViewById(R.id.back).setEnabled(true);
        } else {
            activity.findViewById(R.id.back).setEnabled(false);
        }

        if (view.canGoForward()) {
            activity.findViewById(R.id.go).setEnabled(true);
        } else {
            activity.findViewById(R.id.go).setEnabled(false);
        }
//        activity.refreshMultiCount();
    }

    private void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            Log.i("peter", "url=" + url);
            webview.loadUrl(url);
            if (!TextUtils.isEmpty(searchWord.trim())) {
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
                SqliteHelper.instance(activity).insertHistory(search);
                return null;
            }
        }.execute();
    }


    public void onResume() {
        if (webview != null) {
            webview.onResume();
            webview.resumeTimers();
        }
    }

    public void onPause() {
        if (webview != null) {
            webview.onPause();
            webview.pauseTimers();
        }
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

    public void onDestroy() {
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

    public synchronized void pauseTimers() {
        if (webview != null) {
            webview.pauseTimers();
        }
    }

    /**
     * Resumes the JavaScript timers of the
     * WebView instance, which will trigger a
     * resume for all WebViews in the app.
     */
    public synchronized void resumeTimers() {
        if (webview != null) {
            webview.resumeTimers();
        }
    }

    /**
     * Requests focus down on the WebView instance
     * if the view does not already have focus.
     */
    public void requestFocus() {
        if (webview != null && !webview.hasFocus()) {
            webview.requestFocus();
        }
    }

    public boolean canGoBack() {
        if (webview != null) {
            return webview.canGoBack();
        }
        return false;
    }

    public void goBack() {
        if (webview != null) {
            webview.goBack();
        }
    }

    public boolean canGoForward() {
        if (webview != null) {
            return webview.canGoForward();
        }
        return false;
    }

    public void goForward() {
        if (webview != null) {
            webview.goForward();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        if (webview != null) {
            return webview.getTitle();
        }
        return null;
    }

    public String getFavName() {
        String engineName = url;
        String word = searchWord;
        String title = webview.getTitle();
        Log.i("peter", "title = " + title);
        Log.i("peter", "engineName = " + engineName);
        if (webview.getContext().getString(R.string.url_title_mark_cb).equals(engineName)) {//词霸
            title = word + " - " + engineName;
        } else if (webview.getContext().getString(R.string.url_title_mark_yd).equals(engineName)) {//有道
            title = word + " - " + title;
        } else if (webview.getContext().getString(R.string.url_title_mark_jd).equals(engineName)) {//京东
            title = word + " - " + engineName;
        } else if (webview.getContext().getString(R.string.url_title_mark_tb).equals(engineName)) {//淘宝
            title = word + " - " + engineName;
        } else if (webview.getContext().getString(R.string.url_title_mark_tx).equals(engineName)) {//腾讯视频
            title = word + " - " + engineName + " - " + title;
        } else if (webview.getContext().getString(R.string.url_title_mark_sh).equals(engineName)) {//搜狐视频
            title = word + " - " + engineName + " - " + title;
        } else if (webview.getContext().getString(R.string.url_title_mark_aqy).equals(engineName)) {//爱奇艺
            title = word + " - " + engineName + " - " + title;
        } else if (webview.getContext().getString(R.string.url_title_mark_yyb).equals(engineName)) {//应用宝
            title = word + " - " + engineName;
        } else if (webview.getContext().getString(R.string.url_title_mark_360zs).equals(engineName)) {//360助手
            title = word + " - " + engineName + " - " + title;
        } else if (webview.getContext().getString(R.string.url_title_mark_bdzs).equals(engineName)) {//百度助手
            title = word + " - " + engineName + " - " + title;
        } else if (webview.getContext().getString(R.string.url_title_mark_xm).equals(engineName)) {//小米
            title = word + " - " + engineName + " - " + title;
        }
        Log.i("peter", "title = " + title);
        return title;
    }

    public void refresh() {
        if (webview != null) {
            webview.reload();
        }
    }

}
