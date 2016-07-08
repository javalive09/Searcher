package peter.util.searcher.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.Set;

import peter.util.searcher.bean.Bean;
import peter.util.searcher.utils.DrawableUtils;
import peter.util.searcher.net.MyWebChromeClient;
import peter.util.searcher.net.MyWebClient;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.download.MyDownloadListener;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int API = Build.VERSION.SDK_INT;
    private View bottomBar;
    private WebView webview;
    private View progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public String getWebViewTitle() {
        String title = "";
        if (webview != null) {
            title = webview.getTitle();
            title = TextUtils.isEmpty(title) ? webview.getUrl() : title;
        }
        return title;
    }

    private void init() {
        webview = (WebView) findViewById(R.id.wv);
        progressBar = findViewById(R.id.status);
        bottomBar = findViewById(R.id.bottom_bar);
        Intent intent = getIntent();
        if (intent != null) {
            Set<String> category = intent.getCategories();
            if (category != null && category.contains(Intent.CATEGORY_LAUNCHER)) {//launcher invoke
                startActivity(new Intent(MainActivity.this, EnterActivity.class));
                finish();
            } else {
                checkIntentData(intent);
            }
        }
        initWebView(webview);
        initializeSettings(webview);
    }

    private void initWebView(WebView webview) {
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
        webview.setWebChromeClient(new MyWebChromeClient(this));
        webview.setWebViewClient(new MyWebClient(this));
        webview.setDownloadListener(new MyDownloadListener(this));
        setUserAgent(MainActivity.this);
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

    public void setMainColor(Bitmap favicon) {
        Palette.from(favicon).generate(new Palette.PaletteAsyncListener() {

            @Override
            public void onGenerated(Palette palette) {
                int defaultColor = getResources().getColor(R.color.colorPrimary);
                int mainColor = getSearchBarColor(palette.getVibrantColor(defaultColor));//real main color
                refreshStatusColor(mainColor);
            }
        });
    }

    private int getSearchBarColor(int requestedColor) {
        return DrawableUtils.mixColor(0.25f, requestedColor, Color.WHITE);
    }

    private void refreshStatusColor(int animColor) {
        setBottomBarColor(animColor);
        if (API >= 21) {
            setStatusColor(animColor);
        }
    }

    public void setBottomBarColor(int animColor) {
        bottomBar.setBackgroundColor(animColor);
    }

    public void setStatusLevel(int level) {
        LevelListDrawable d = (LevelListDrawable) progressBar.getBackground();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    public void refreshOptStatus() {
        final View back = findViewById(R.id.back);
        if (webview.canGoBack()) {
            if (back != null && !back.isEnabled()) {
                back.setEnabled(true);
            }
        } else {
            if (back != null && back.isEnabled()) {
                back.setEnabled(false);
            }
        }
        final View go = findViewById(R.id.go);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntentData(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusColor(int color) {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(color);
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_INNER_BROWSE.equals(action)) { // inner invoke
                String url = (String) intent.getSerializableExtra(NAME_URL);
                if (!TextUtils.isEmpty(url)) {
                    String searchWord = intent.getStringExtra(NAME_WORD);
                    loadUrl(url, searchWord);
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    loadUrl(url, "");
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                loadUrl(url, searchWord);
            }
        }
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

    private void loadUrl(String url, String searchWord) {
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

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            case R.id.home:
                startActivity(new Intent(MainActivity.this, EnterActivity.class));
                break;
            case R.id.refresh:
                webview.reload();
                break;
            case R.id.menu:
                popupMenu(v);
                break;
        }
    }

    private void popupMenu(View menu) {
        PopupMenu popup = new PopupMenu(this, menu);
        popup.getMenuInflater().inflate(R.menu.web, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_browser:
                        String url = webview.getUrl();
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(url);
                        intent.setData(content_url);
                        startActivity(intent);
                        break;
                    case R.id.action_share:
                        url = webview.getUrl();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                        break;
                    case R.id.action_collect:
                        if (!TextUtils.isEmpty(webview.getUrl())) {
                            Bean bean = new Bean();
                            bean.name = webview.getTitle();
                            if(TextUtils.isEmpty(bean.name)) {
                                bean.name = webview.getUrl();
                            }
                            bean.url = webview.getUrl();
                            bean.time = System.currentTimeMillis();
                            SqliteHelper.instance(MainActivity.this).insertFav(bean);
                            Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.action_copy_link:
                        url = webview.getUrl();
                        String title = webview.getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_exit:
                        exit();
                        break;
                }
                return true;
            }
        });
        popup.show();
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
