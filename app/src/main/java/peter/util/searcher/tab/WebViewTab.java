package peter.util.searcher.tab;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.SettingsManager;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.net.MyDownloadListener;
import peter.util.searcher.net.MyWebChromeClient;
import peter.util.searcher.net.MyWebClient;
import peter.util.searcher.utils.Constants;
import peter.util.searcher.view.SearchWebView;

/**
 * webView类型的标签
 * Created by peter on 2016/11/17.
 */

public class WebViewTab extends SearcherTab {

    private WebView mWebView;

    private String currentUA;
    private MyWebChromeClient myWebChromeClient;
    private static final String HEADER_DNT = "DNT";
    private final Map<String, String> mRequestHeaders = new ArrayMap<>();

    WebViewTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public WebView getView() {
        if (mWebView == null) {
            mWebView = new SearchWebView(mainActivity);
            initWebView();
            initializeSettings();
            mainActivity.registerForContextMenu(mWebView);
        }
        return mWebView;
    }

    @Override
    public void onDestroy() {
        getView().clearHistory();
        getView().clearCache(true);
        getView().loadUrl("about:blank");
        getView().pauseTimers();
        mWebView = null;
    }

    public void onResume() {
        getView().resumeTimers();
        getView().onResume();
    }

    public void onPause() {
        getView().pauseTimers();
        getView().onPause();
    }

    public Map<String, String> getRequestHeaders() {
        return mRequestHeaders;
    }

    public void setUA(String ua) {
        currentUA = ua;
        getView().getSettings().setUserAgentString(ua);
    }

    private String getDefaultUA() {
        return WebSettings.getDefaultUserAgent(getView().getContext());
    }

    public boolean isDeskTopUA() {
        return Constants.DESKTOP_USER_AGENT.equals(currentUA);
    }

    public String getUrl() {
        String url = getView().getUrl();
        if (TextUtils.isEmpty(url)) {
            url = getTabData().getUrl();
        }
        return url;
    }

    @Override
    public void loadUrl(TabData bean) {
        if (!TextUtils.isEmpty(bean.getUrl())) {
            if (!peter.util.searcher.tab.Tab.ACTION_NEW_WINDOW.equals(bean.getUrl())
                    || !getUrl().equals(bean.getUrl())) {
                getView().loadUrl(bean.getUrl());
                mainActivity.setCurrentView(getView());
                if (!TextUtils.isEmpty(bean.getTitle())) {
                    saveData(bean);
                }
            }
        }
    }

    @Override
    public String getSearchWord() {
        return getTabData().getSearchWord();
    }

    @Override
    public int getPageNo() {
        return getTabData().getPageNo();
    }

    public void reload() {
        byte[] data = getTabData().getBundle();
        if (data != null) {
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            Bundle bundle = parcel.readBundle(ClassLoader.getSystemClassLoader());
            getView().restoreState(bundle);

            parcel.recycle();
            getTabData().setBundle(null);
            DaoManager.getInstance().deleteTabData(getTabData());
        }

        getView().reload();
    }

    public boolean canGoBack() {
        return getView().canGoBack() || myWebChromeClient.isCustomViewShow();
    }

    public void goBack() {
        if (myWebChromeClient.isCustomViewShow()) {
            myWebChromeClient.hideCustomView();
        } else {
            getView().goBack();
        }
    }

    public boolean canGoForward() {
        return getView().canGoForward();
    }

    public void goForward() {
        getView().goForward();
    }

    public String getTitle() {
        String title = getView().getTitle();
        if (TextUtils.isEmpty(title)) {
            title = getTabData().getTitle();
        }
        return title;
    }

    private void saveData(final TabData bean) {
        Observable.just("saveData").observeOn(Schedulers.io()).subscribe(s -> {
            bean.setTime(System.currentTimeMillis());
            DaoManager.getInstance().insertHistory(bean);
        });
    }

    private void initWebView() {
        getView().setDrawingCacheBackgroundColor(Color.WHITE);
        getView().setFocusableInTouchMode(true);
        getView().setFocusable(true);
        getView().setDrawingCacheEnabled(false);
        getView().setWillNotCacheDrawing(true);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            getView().setAnimationCacheEnabled(false);
            //noinspection deprecation
            getView().setAlwaysDrawnWithCacheEnabled(false);
        }
        getView().setBackgroundColor(Color.WHITE);
        getView().setScrollbarFadingEnabled(true);
        getView().setSaveEnabled(true);
        getView().setNetworkAvailable(true);
        getView().setWebChromeClient(myWebChromeClient = new MyWebChromeClient(WebViewTab.this));
        getView().setWebViewClient(new MyWebClient(WebViewTab.this));
        getView().setDownloadListener(new MyDownloadListener(mainActivity));
        setUA(getDefaultUA());
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        if (SettingsManager.getInstance().isNoTrack()) {
            mRequestHeaders.put(HEADER_DNT, "1");
        } else {
            mRequestHeaders.remove(HEADER_DNT);
        }

    }

    private void initializeSettings() {
        final WebSettings settings = getView().getSettings();
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
        CookieManager.getInstance().setAcceptThirdPartyCookies(getView(), true);

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
