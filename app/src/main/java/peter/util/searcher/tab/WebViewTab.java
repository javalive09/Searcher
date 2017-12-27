package peter.util.searcher.tab;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import peter.util.searcher.R;
import peter.util.searcher.SettingsManager;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.net.MyDownloadListener;
import peter.util.searcher.net.MyWebChromeClient;
import peter.util.searcher.net.MyWebClient;
import peter.util.searcher.utils.Constants;

/**
 * webView类型的标签
 * Created by peter on 2016/11/17.
 */

public class WebViewTab extends SearcherTab {

    private WebView mWebView;
    private TabData tabData;
    private String currentUA;
    private MyWebChromeClient myWebChromeClient;
    private static final String HEADER_DNT = "DNT";
    private final Map<String, String> mRequestHeaders = new ArrayMap<>();

    public WebViewTab(MainActivity activity) {
        super(activity);
    }

    void onCreate() {
        initWebView();
        initializeSettings();
        mainActivity.registerForContextMenu(mWebView);
    }

    @Override
    public WebViewTab create(TabData tabData) {
        if (!TextUtils.isEmpty(tabData.getUrl())) {
            this.tabData = tabData;
            Log.i("peter", "url=" + tabData.getUrl());
            if (mWebView == null) {
                int resId = onCreateViewResId();
                mWebView = (WebView) mainActivity.setCurrentView(resId);
                onCreate();
            }
        }
        return this;
    }

    @Override
    public WebView getView() {
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

    public MainActivity getActivity() {
        return mainActivity;
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

    public TabData getTabData() {
        return tabData;
    }

    public Map<String, String> getRequestHeaders() {
        return mRequestHeaders;
    }

    public void setUA(String ua) {
        currentUA = ua;
        mWebView.getSettings().setUserAgentString(ua);
    }

    private String getDefaultUA() {
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
        String url = mWebView.getUrl();
        if(TextUtils.isEmpty(url)) {
            url = tabData.getUrl();
        }
        return url;
    }

    @Override
    public void loadUrl(TabData bean) {
        if (!TextUtils.isEmpty(bean.getUrl())) {
            if (!peter.util.searcher.tab.Tab.ACTION_NEW_WINDOW.equals(bean.getUrl())
                    || !getUrl().equals(bean.getUrl())) {
                mWebView.loadUrl(bean.getUrl());
                mainActivity.setCurrentView(mWebView);
                if (!TextUtils.isEmpty(bean.getTitle())) {
                    saveData(bean);
                }
            }
        }
    }

    @Override
    public String getSearchWord() {
        return tabData.getSearchWord();
    }

    @Override
    public int getPageNo() {
        return tabData.getPageNo();
    }

    public void reload() {
        byte[] data = tabData.getBundle();
        if(data != null) {
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            Bundle bundle = parcel.readBundle(ClassLoader.getSystemClassLoader());
            mWebView.restoreState(bundle);

            parcel.recycle();
            tabData.setBundle(null);
            DaoManager.getInstance().deleteTabData(tabData);
        }

        mWebView.reload();
    }

    public boolean canGoBack() {
        return mWebView.canGoBack() || myWebChromeClient.isCustomViewShow();
    }

    public void goBack() {
        if (myWebChromeClient.isCustomViewShow()) {
            myWebChromeClient.hideCustomView();
        } else {
            mWebView.goBack();
        }
    }

    public boolean canGoForward() {
        return mWebView.canGoForward();
    }

    public void goForward() {
        mWebView.goForward();
    }

    public String getTitle() {
        String title = mWebView.getTitle();
        if(TextUtils.isEmpty(title)) {
            title = tabData.getTitle();
        }
        return title;
    }

    private void saveData(final TabData bean) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                bean.setTime(System.currentTimeMillis());
                DaoManager.getInstance().insertHistory(bean);
                return null;
            }
        }.execute();
    }

    private void initWebView() {
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
        mWebView.setWebChromeClient(myWebChromeClient = new MyWebChromeClient(WebViewTab.this));
        mWebView.setWebViewClient(new MyWebClient(WebViewTab.this));
        mWebView.setDownloadListener(new MyDownloadListener(mainActivity));
        setUA(getDefaultUA());
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        if (SettingsManager.getInstance().isNoTrack()) {
            mRequestHeaders.put(HEADER_DNT, "1");
        } else {
            mRequestHeaders.remove(HEADER_DNT);
        }

    }

    private void initializeSettings() {
        final WebSettings settings = mWebView.getSettings();
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDatabaseEnabled(true);

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setGeolocationEnabled(true);

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
        settings.setBlockNetworkImage(false);
        settings.setSupportMultipleWindows(true);

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        //需要加上否则播放不了一些视频如今日头条的视频
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);

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
                if (!TextUtils.isEmpty(domain)) {
                    return domain.startsWith("www.") ? domain.substring(4) : domain;
                }
            }
        }
        return null;
    }

}
