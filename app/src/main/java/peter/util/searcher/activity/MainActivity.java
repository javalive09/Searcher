package peter.util.searcher.activity;

import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import com.umeng.analytics.MobclickAgent;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.SettingsManager;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.R;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.net.DownloadHandler;
import peter.util.searcher.net.MyDownloadListener;
import peter.util.searcher.net.UpdateController;
import peter.util.searcher.tab.HomeTab;
import peter.util.searcher.tab.LocalViewTab;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.tab.WebViewTab;
import peter.util.searcher.utils.Constants;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.view.SearchWebView;
import peter.util.searcher.view.TextDrawable;
import peter.util.searcher.view.WebViewContainer;

/**
 * 主页activity
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.webView_container)
    WebViewContainer webViewContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.top_search)
    EditText topText;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.menu_anchor)
    View menuAnchor;
    @BindView(R.id.top_bar)
    View mTopBar;
    @BindView(R.id.find_control)
    View findControlView;
    @BindView(R.id.find_content_txt)
    EditText findControlContent;
    @BindView(R.id.count_find)
    TextView findControlCount;

    private PopupMenu popup;
    private TextDrawable tabsDrawable;
    private final HashMap<String, Class> router = new HashMap<>();
    private boolean realBack = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    /**
     * test insert url
     *
     * @param searchWord 搜索词
     */
    public void flushUrl(String searchWord) {
        String engineUrl = getString(R.string.default_engine_url);
        String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
        TabData tabData = new TabData();
        tabData.setTitle(searchWord);
        tabData.setUrl(url);
        TabGroupManager.getInstance().load(tabData, true);
    }

    private void init() {
//        Debug.startMethodTracing("searcher");
        TabGroupManager.getInstance().init(this);
        installLocalTabRouter();
        initTopBar();
        DaoManager.getInstance().restoreAllTabs();
        checkIntentData(getIntent());
        UpdateController.instance().autoCheckVersion(MainActivity.this);
    }

    private void initTopBar() {
        setSupportActionBar(toolbar);
        tabsDrawable = new TextDrawable(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_tabs).setIcon(tabsDrawable);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        SearchWebView.ContextMenuInfo info = (SearchWebView.ContextMenuInfo) menuInfo;
        popupContextMenu(info);
    }

    private void dismissContextMenu() {
        if (popup != null) {
            popup.dismiss();
        }
    }

    private void popupContextMenu(SearchWebView.ContextMenuInfo info) {
        menuAnchor.setX(info.getX());
        menuAnchor.setY(info.getY());
        dismissContextMenu();
        popup = new PopupMenu(MainActivity.this, menuAnchor);
        popup.getMenuInflater().inflate(R.menu.context, popup.getMenu());

        WebViewTab webViewTab = (WebViewTab) TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
        Message msg = webViewHandler.obtainMessage();
        msg.setTarget(webViewHandler);
        msg.obj = info;
        webViewTab.getView().requestFocusNodeHref(msg);
    }

    private final Handler webViewHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String url = msg.getData().getString("url");
            SearchWebView.ContextMenuInfo info = (SearchWebView.ContextMenuInfo) msg.obj;
            contextMenuListener.setUrl(url);
            contextMenuListener.setInfo(info);
            popup.setOnMenuItemClickListener(contextMenuListener);
            WebView.HitTestResult hitTestResult = info.getResult();
            if (hitTestResult != null) {
                switch (hitTestResult.getType()) {
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 带有链接的图片类型
                    case WebView.HitTestResult.IMAGE_TYPE: //图片类型
                        popup.getMenu().setGroupVisible(R.id.picture, true);
                        popup.getMenu().getItem(0).getMenuInfo();
                        popup.show();
                        break;
                    case WebView.HitTestResult.PHONE_TYPE:// 拨号类型
                    case WebView.HitTestResult.EMAIL_TYPE:// Email类型
                    case WebView.HitTestResult.EDIT_TEXT_TYPE:// 选中的文字类型
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE://链接类型
                        popup.getMenu().setGroupVisible(R.id.txt_link, true);
                        popup.show();
                    default:
                        break;
                }
            }
        }
    };

    private final SearchWebView.OnMenuItemClickListener contextMenuListener = new SearchWebView.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            SearchWebView.ContextMenuInfo info = getInfo();
            String url = getUrl();
            if (info.getResult() != null) {
                url = info.getResult().getExtra();
            }
            switch (item.getItemId()) {
                case R.id.open_pic_new_tab:
                    TabData tabData = new TabData();
                    tabData.setUrl(url);
                    tabData.setTime(System.currentTimeMillis());
                    TabGroup parentTabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
                    TabGroupManager.getInstance().load(tabData, true);
                    TabGroupManager.getInstance().getCurrentTabGroup().setParent(parentTabGroup);
                    break;
                case R.id.copy_pic_link:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(url, url);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.save_pic:
                    String mineTye = DownloadHandler.getMimeType(url);
                    if (TextUtils.isEmpty(mineTye)) {
                        mineTye = "image/jpeg";
                    }

                    new MyDownloadListener(MainActivity.this).onDownloadStart(url, "", "", mineTye, 0);
                    break;
                case R.id.shard_pic:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                    break;

                case R.id.open_url_new_tab:
                    tabData = new TabData();
                    tabData.setUrl(url);
                    tabData.setTime(System.currentTimeMillis());
                    parentTabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
                    TabGroupManager.getInstance().load(tabData, true);
                    TabGroupManager.getInstance().getCurrentTabGroup().setParent(parentTabGroup);
                    break;

                case R.id.copy_link:
                    clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clip = ClipData.newPlainText(url, url);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

            return false;
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        TabGroup currentGroup = TabGroupManager.getInstance().getCurrentTabGroup();
        SearcherTab currentTab = currentGroup.getCurrentTab();
        if (currentTab instanceof LocalViewTab) {
            menu.setGroupVisible(R.id.web_sites, false);
        } else {
            menu.setGroupVisible(R.id.web_sites, true);
            menu.findItem(R.id.action_auto_fullscreen).setChecked(SettingsManager.getInstance().isAutoFullScreen());
            WebViewTab webViewTab = (WebViewTab) currentTab;
            menu.findItem(R.id.action_desktop).setChecked(webViewTab.isDeskTopUA());
        }

        if (currentGroup.canGoForward()) {
            menu.findItem(R.id.action_goforward).setVisible(true);
        } else {
            menu.findItem(R.id.action_goforward).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tabs:
                startActivity(new Intent(MainActivity.this, TabsActivity.class));
                break;
            case R.id.action_share:
                String url = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getUrl();
                String title = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getTitle();
                Intent sendIntent = new Intent();
                if (!TextUtils.isEmpty(title)) {
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                    url = title + "\n" + url;
                }
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                break;
            case R.id.action_favorite:
                url = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getUrl();
                if (!TextUtils.isEmpty(url) && !url.startsWith(peter.util.searcher.tab.Tab.LOCAL_SCHEMA)) {
                    final TabData tabData = new TabData();
                    tabData.setTitle(TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getTitle());
                    if (TextUtils.isEmpty(tabData.getTitle())) {
                        tabData.setTitle(TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getUrl());
                    }

                    tabData.setUrl(url);
                    tabData.setTime(System.currentTimeMillis());
                    Observable<Boolean> result = Observable.create(e -> {
                        boolean suc = DaoManager.getInstance().insertFavorite(tabData) != 0L;
                        e.onNext(suc);
                        e.onComplete();
                    });
                    result.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(o -> {
                        if (o) {
                            Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case R.id.action_copy_link:
                url = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getUrl();
                title = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getTitle();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(title, url);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_download:
                Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.action_setting:
//                startActivity(new Intent(MainActivity.this, SettingsActivity.class));

                String searchWord = "a";
                for (int i = 0; i < 90; i++) {
                    flushUrl(searchWord + i);
                }

                break;
            case R.id.action_exit:
                exit();
                break;
            case R.id.action_bookmark:
                startActivity(new Intent(MainActivity.this, BookMarkActivity.class));
                break;
            case R.id.action_goforward:
                TabGroupManager.getInstance().getCurrentTabGroup().goForward();
                break;
            case R.id.action_refresh:
                TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().reload();
                break;
            case R.id.action_auto_fullscreen:
                if (item.isChecked()) {//auto
                    SettingsManager.getInstance().saveAutoFullScreenSp(false);
                } else {
                    SettingsManager.getInstance().saveAutoFullScreenSp(true);
                }
                TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getView().requestLayout();
                break;
            case R.id.action_desktop:
                WebViewTab tab = (WebViewTab) TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
                if (tab.isDeskTopUA()) {
                    tab.setUA(Constants.MOBILE_USER_AGENT);
                } else {
                    tab.setUA(Constants.DESKTOP_USER_AGENT);
                }
                tab.reload();
                break;
            case R.id.action_find:
                showFindControlView(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFindControlView(boolean show) {
        if (show) {
            SearcherTab searcherTab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
            if (searcherTab instanceof WebViewTab) {
                final WebViewTab webViewTab = (WebViewTab) searcherTab;
                webViewTab.getView().setFindListener(findListener);

                findControlContent.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!TextUtils.isEmpty(s)) {
                            webViewTab.getView().findAllAsync(s.toString());
                        } else {
                            findControlCount.setText("");
                        }
                    }
                });
            }
            findControlContent.setText("");
            findControlCount.setText("");
            findControlContent.requestFocus();
            findControlView.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        } else {
            findControlView.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(findControlContent.getWindowToken(), 0);
            }
        }

    }

    private final WebView.FindListener findListener = new WebView.FindListener() {
        @Override
        public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
            if (isDoneCounting) {
                if (numberOfMatches > 0) {
                    String title = String.format(getString(R.string.page_search_title), activeMatchOrdinal + 1, numberOfMatches);
                    findControlCount.setText(title);
                } else {
                    findControlCount.setText("");
                }
            } else {
                if (findControlCount != null) {
                    findControlCount.setText("...");
                }
            }
        }
    };

    private void touchSearch() {
        String content = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getSearchWord();
        int pageNo = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab().getPageNo();
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        TabData bean = new TabData();
        bean.setTitle(content);
        bean.setPageNo(pageNo);
        intent.putExtra(NAME_BEAN, bean);
        startActivity(intent);
    }

    private void installLocalTabRouter() {
        router.put(peter.util.searcher.tab.Tab.URL_HOME, HomeTab.class);
    }

    public Class getRouterClass(String url) {
        return router.get(url);
    }

    public void setCurrentView(View view) {
        webViewContainer.setCurrentView(view);
        progressBar.setVisibility(View.INVISIBLE);
        showTopbar();
    }

    public ViewGroup getWebViewContainer() {
        return webViewContainer;
    }

    public void showTopbar() {
        if (isTopBarHide() && SettingsManager.getInstance().isAutoFullScreen()) {
            ObjectAnimator.ofFloat(mTopBar, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
            ObjectAnimator.ofFloat(webViewContainer, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
        }
    }

    public void hideTopbar() {
        if (isTopBarShow() && SettingsManager.getInstance().isAutoFullScreen()) {
            ObjectAnimator.ofFloat(mTopBar, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
            ObjectAnimator.ofFloat(webViewContainer, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
        }
    }

    private boolean isTopBarHide() {
        return mTopBar.getTranslationY() == -Constants.getActionBarH(this);
    }

    private boolean isTopBarShow() {
        return mTopBar.getTranslationY() == 0;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntentData(intent);
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_INNER_BROWSE.equals(action)) { // inner invoke
                TabData bean = (TabData) intent.getSerializableExtra(NAME_BEAN);
                if (bean != null && !TextUtils.isEmpty(bean.getUrl())) {
                    TabGroupManager.getInstance().load(bean, false);
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    TabData tabData = new TabData();
                    tabData.setUrl(url);
                    TabGroupManager.getInstance().load(tabData, true);
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                TabData tabData = new TabData();
                tabData.setTitle(searchWord);
                tabData.setUrl(url);
                TabGroupManager.getInstance().load(tabData, true);
            } else if (Intent.ACTION_MAIN.equals(action)) {
                if (TabGroupManager.getInstance().getTabGroupCount() == 0) {
                    loadHome();
                }
            } else if (Intent.ACTION_ASSIST.equals(action)) {
                if (TabGroupManager.getInstance().getTabGroupCount() == 0) {
                    loadHome();
                } else {
                    SearcherTab searcherTab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
                    if (searcherTab instanceof WebViewTab) {//webView
                        if (TabGroupManager.getInstance().getTabGroupCount() < TabGroupManager.MAX_TAB) {
                            loadHome();
                        }
                    }
                }
                touchSearch();
            }
        }
    }

    private void loadHome() {
        TabData tabData = new TabData();
        tabData.setUrl(Tab.URL_HOME);
        TabGroupManager.getInstance().load(tabData, true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (findControlView.getVisibility() == View.VISIBLE) {
                showFindControlView(false);
                return true;
            }

            TabGroup tabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
            if (tabGroup.canGoBack()) {
                tabGroup.goBack();
                return true;
            } else {
                TabGroup parentTabGroup = tabGroup.getParent();
                if (parentTabGroup != null) {
                    TabGroupManager.getInstance().removeIndex(tabGroup);
                    TabGroupManager.getInstance().switchTabGroup(parentTabGroup);
                    return true;
                }
            }

            if (!realBack) {
                realBack = true;
                Toast.makeText(MainActivity.this, R.string.exit_hint, Toast.LENGTH_SHORT).show();
                webViewHandler.postDelayed(() -> realBack = false, 1000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onResume() {
        super.onResume();
        TabGroupManager.getInstance().getCurrentTabGroup().onResume();
//        MobclickAgent.onResume(this);
//        Debug.stopMethodTracing();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Debug.startMethodTracing("savetabs");
        TabGroupManager.getInstance().getCurrentTabGroup().onPause();
        DaoManager.getInstance().saveTabs();
//        Debug.stopMethodTracing();

//        MobclickAgent.onPause(this);
    }

    public void refreshTitle() {
        final TabGroup tabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
        if (tabGroup != null) {
            final SearcherTab searcherTab = tabGroup.getCurrentTab();
            if (searcherTab != null) {
                String host = searcherTab.getHost();
                refreshTopText(host);
                int count = TabGroupManager.getInstance().getTabGroupCount();
                Log.e("refreshTitle", "count = " + count);
                tabsDrawable.setText(count, 10);
            }
        }
    }

    public void refreshTopText(String host) {
        switch (host) {
            case Tab.LOCAL_HOST:
                topText.setText(R.string.search_hint);
                break;
            default:
                topText.setText(host);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top_search:
                touchSearch();
                break;
            case R.id.up_find:
                SearcherTab searcherTab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
                if (searcherTab instanceof WebViewTab) {
                    WebViewTab webViewTab = (WebViewTab) searcherTab;
                    webViewTab.getView().findNext(false);
                }
                break;
            case R.id.down_find:
                searcherTab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
                if (searcherTab instanceof WebViewTab) {
                    WebViewTab webViewTab = (WebViewTab) searcherTab;
                    webViewTab.getView().findNext(true);
                }
                break;
            case R.id.close_find:
                showFindControlView(false);
                searcherTab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
                if (searcherTab instanceof WebViewTab) {
                    WebViewTab webViewTab = (WebViewTab) searcherTab;
                    webViewTab.getView().clearMatches();
                }
                break;
            default:
                break;
        }
    }

    /**
     * @param enabled   status bar
     * @param immersive total fullscreen
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

    public void refreshProgress(WebViewTab webViewTab, int progress) {
        final TabGroup tabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
        if (tabGroup != null) {
            if (tabGroup.getCurrentTab() == webViewTab) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.post(() -> progressBar.setVisibility(View.INVISIBLE));
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TabGroupManager.getInstance().clear();
        DaoManager.getInstance().clear();
    }

}
