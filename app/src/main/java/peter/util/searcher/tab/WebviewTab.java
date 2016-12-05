package peter.util.searcher.tab;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.HashMap;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.download.MyDownloadListener;
import peter.util.searcher.net.MyWebChromeClient;
import peter.util.searcher.net.MyWebClient;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.utils.Utils;

/**
 * Created by peter on 2016/11/17.
 */

public class WebViewTab extends SearcherTab {

    private static final int API = Build.VERSION.SDK_INT;
    private WebView mWebView;
    private HashMap<String, String> searchMap = new HashMap<>();

    public WebViewTab(MainActivity activity) {
        super(activity);
    }

    public void onCreate() {
        initmWebView();
        initializeSettings();
    }

    @Override
    public View getView() {
        return mWebView;
    }

    @Override
    public void onDestory() {
        if(mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            mWebView.freeMemory();
            mWebView.pauseTimers();
            mWebView = null;
        }
    }

    public void onResume() {
        if(mWebView != null) {
            mWebView.resumeTimers();
            mWebView.onResume();
        }
    }

    public void onPause() {
        if(mWebView != null) {
            mWebView.pauseTimers();
            mWebView.onPause();
        }
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_webview;
    }

    public void loadUrl(String url, String searchWord) {
        if (!TextUtils.isEmpty(url)) {
            Log.i("peter", "url=" + url);
            if (mWebView == null) {
                int resId = onCreateViewResId();
                mWebView = (WebView) mainActivity.setCurrentView(resId);
                onCreate();
                if (!Tab.NEW_WINDOW.equals(url)) {
                    mWebView.loadUrl(url);
                }

            } else {
                if (!getUrl().equals(url)) {
                    mWebView.loadUrl(url);
                }
                mainActivity.setCurrentView(mWebView);
            }
            if (!TextUtils.isEmpty(searchWord)) {
                searchMap.put(url, searchWord);
                saveData(searchWord, url);
            }
        }
    }

    public String getUrl() {
        return mWebView.getUrl();
    }

    @Override
    public String getSearchWord() {
        return searchMap.get(mWebView.getUrl());
    }

    public void reload() {
        mWebView.reload();
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    public void goBack() {
        mWebView.goBack();
    }

    public boolean canGoForward() {
        return mWebView.canGoForward();
    }

    public void goForward() {
        mWebView.goForward();
    }

    public String getTitle() {
        return mWebView.getTitle();
    }

    private void saveData(final String word, final String url) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bean search = new Bean();
                search.name = word;
                search.time = System.currentTimeMillis();
                search.url = url;
                SqliteHelper.instance(mainActivity).insertHistory(search);
                return null;
            }
        }.execute();
    }

    private void initmWebView() {
        mWebView.setDrawingCacheBackgroundColor(Color.WHITE);
        mWebView.setFocusableInTouchMode(true);
        mWebView.setFocusable(true);
        mWebView.setDrawingCacheEnabled(false);
        mWebView.setWillNotCacheDrawing(true);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            mWebView.setAnimationCacheEnabled(false);
            //noinspection deprecation
            mWebView.setAlwaysDrawnWithCacheEnabled(false);
        }
        mWebView.setBackgroundColor(Color.WHITE);
        mWebView.setScrollbarFadingEnabled(true);
        mWebView.setSaveEnabled(true);
        mWebView.setNetworkAvailable(true);
        mWebView.setWebChromeClient(new MyWebChromeClient(WebViewTab.this, mainActivity));
        mWebView.setWebViewClient(new MyWebClient(mainActivity));
        mWebView.setDownloadListener(new MyDownloadListener(mainActivity));
        String def = WebSettings.getDefaultUserAgent(mWebView.getContext());
        WebSettings settings = mWebView.getSettings();
        settings.setUserAgentString(def);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }
    }

    @SuppressLint("NewApi")
    private void initializeSettings() {
        final WebSettings settings = mWebView.getSettings();
        settings.setMediaPlaybackRequiresUserGesture(true);
        if (API >= Build.VERSION_CODES.LOLLIPOP) {
            // We're in Incognito mode, reject
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportMultipleWindows(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
    }

}
