package peter.util.searcher;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import peter.util.searcher.download.MyDownloadListener;

/**
 * Created by peter on 16/5/9.
 */
public class SearcherWebView {

    private static final int API = Build.VERSION.SDK_INT;
    private WebView webview;
    private View progressBar;
    private View rootView;
    private int mainColor = -1;
    private int defaultColor;

    public SearcherWebView() {
        init();
    }

    public Bitmap getFavIcon() {
        return webview.getFavicon();
    }

    private void init() {
        Activity activity = SearcherWebViewManager.instance().getActivity();
        rootView = View.inflate(activity, R.layout.searcher_webview, null);
        webview = (WebView) rootView.findViewById(R.id.wv);
        progressBar = rootView.findViewById(R.id.status);
        defaultColor =activity.getResources().getColor(R.color.colorPrimary);
        initWebview(webview);
        initializeSettings(webview);
    }

    private void resetCacheMode() {
        if (webview.getSettings().getCacheMode() != WebSettings.LOAD_DEFAULT) {
            webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }
    }

    public void refreshAllStatus() {
        resetCacheMode();
        setStatusLevel(0);
        setOptStatus();
    }

    public void reSetAllStatus() {
        setStatusLevel(1);
        setOptStatus();
        refreshStatusColor(mainColor == -1 ? defaultColor : mainColor);
    }

    public Bitmap getThumbNail() {
        if (webview != null) {
            return webview.getDrawingCache();
        }
        return null;
    }

    public int getMainColor() {
        if (mainColor == -1) {
            mainColor = SearcherWebViewManager.instance().getActivity().getResources().getColor(R.color.colorPrimary);
        }
        return mainColor;
    }

    private void refreshStatusColor(int animColor) {
        MainActivity activity = SearcherWebViewManager.instance().getActivity();
        if(activity != null) {
            activity.setBottomBarColor(animColor);
            if (API >= 21) {
                activity.setStatusColor(animColor);
            }
        }
    }

    public void setStatusMainColor() {
        refreshStatusColor(getMainColor());
    }

    public void setMainColor(Bitmap favicon) {
        Palette.from(favicon).generate(new Palette.PaletteAsyncListener() {

            @Override
            public void onGenerated(Palette palette) {
                mainColor = getSearchBarColor(palette.getVibrantColor(getMainColor()));//real main color
                refreshStatusColor(mainColor);
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
        webview.setWebChromeClient(new MyWebChromeClient(this));
        webview.setWebViewClient(new MyWebClient(this));
        webview.setDownloadListener(new MyDownloadListener());
        setUserAgent(SearcherWebViewManager.instance().getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
        }
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
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
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

    public void setOptStatus() {
        Activity activity = SearcherWebViewManager.instance().getActivity();
        if(activity != null) {
            final View back = activity.findViewById(R.id.back);
            if (webview.canGoBack()) {
                if (back != null && !back.isEnabled()) {
                    back.setEnabled(true);
                }
            } else {
                if (back != null && back.isEnabled()) {
                    back.setEnabled(false);
                }
            }
            final View go = SearcherWebViewManager.instance().getActivity().findViewById(R.id.go);
            if (webview.canGoForward()) {
                if (go != null && !go.isEnabled()) {
                    go.setEnabled(true);
                }
            } else {
                if (go != null && go.isEnabled()) {
                    go.setEnabled(false);
                }
            }
        }
    }

    public void loadUrl(String url, String searchWord) {
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
                SqliteHelper.instance(SearcherWebViewManager.instance().getActivity()).insertHistory(search);
                return null;
            }
        }.execute();
    }


    public void onResume() {
        if (webview != null) {
            webview.onResume();
        }
    }

    public void onPause() {
        if (webview != null) {
            webview.onPause();
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
        if (webview != null) {
            return webview.getUrl();
        }
        return null;
    }

    public String getTitle() {
        if (webview != null) {
            return webview.getTitle();
        }
        return null;
    }

    public String getFavName() {
        String title = webview.getTitle() + "-" + getUrl();
        Log.i("peter", "title = " + title);
        return title;
    }

    public void refresh() {
        if (webview != null) {
            webview.reload();
        }
    }

}
