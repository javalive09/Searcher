package peter.util.searcher.tab;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.URI;
import java.net.URISyntaxException;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.net.MyDownloadListener;
import peter.util.searcher.net.MyWebChromeClient;
import peter.util.searcher.net.MyWebClient;
import peter.util.searcher.utils.Constants;

/**
 * Created by peter on 2016/11/17.
 */

public class WebViewTab extends SearcherTab {

    private WebView mWebView;
    private Bean bean;
    private String currentUA;

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
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            mWebView.pauseTimers();
            mWebView = null;
        }
    }

    public void onResume() {
        if (mWebView != null) {
            mWebView.resumeTimers();
            mWebView.onResume();
        }
    }

    public void onPause() {
        if (mWebView != null) {
            mWebView.pauseTimers();
            mWebView.onPause();
        }
    }

    public void setUA(String ua) {
        currentUA = ua;
        mWebView.getSettings().setUserAgentString(ua);
    }

    public String getDefaultUA() {
        return WebSettings.getDefaultUserAgent(mWebView.getContext());
    }

    public boolean isDeskTopUA() {
        return Constants.DESKTOP_USER_AGENT.equals(currentUA);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_webview;
    }

    public String getUrl() {
        return mWebView.getUrl();
    }

    @Override
    public void loadUrl(Bean bean) {
        if (!TextUtils.isEmpty(bean.url)) {
            this.bean = bean;
            Log.i("peter", "url=" + bean.url);
            if (mWebView == null) {
                int resId = onCreateViewResId();
                mWebView = (WebView) mainActivity.setCurrentView(resId);
                onCreate();
                if (!Tab.NEW_WINDOW.equals(bean.url)) {
                    mWebView.loadUrl(bean.url);
                }

            } else {
                if (!getUrl().equals(bean.url)) {
                    mWebView.loadUrl(bean.url);
                }
                mainActivity.setCurrentView(mWebView);
            }
            if (!TextUtils.isEmpty(bean.name)) {
                saveData(bean);
            }
        }
    }

    @Override
    public String getSearchWord() {
        return bean.name;
    }

    @Override
    public int getPageNo() {
        return bean.pageNo;
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

    private void saveData(final Bean bean) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                bean.time = System.currentTimeMillis();
                SqliteHelper.instance(mainActivity).insertHistory(bean);
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
        setUA(getDefaultUA());
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
    }

    private void initializeSettings() {
        final WebSettings settings = mWebView.getSettings();
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
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

    @Override
    public String getHost() {
        String url = getUrl();
        if (!TextUtils.isEmpty(url)) {
            URI uri = null;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (uri != null) {
                String domain = uri.getHost();
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
        }
        return null;
    }

}
