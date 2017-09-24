package peter.util.searcher.activity;

import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.SettingsManager;
import peter.util.searcher.TabManager;
import peter.util.searcher.adapter.MultiWindowAdapter;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.R;
import peter.util.searcher.db.DaoManager;
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
import peter.util.searcher.utils.FileUtils;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.view.SearchWebView;
import peter.util.searcher.view.TextDrawable;
import peter.util.searcher.view.WebViewContainer;

/**
 * 主页activity
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.webview_container)
    WebViewContainer webViewContainer;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tabs)
    ListView multiTabListView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.top_txt)
    View topText;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.menu_anchor)
    View menuAnchor;
    private PopupMenu popup;
    private TextDrawable multiWindowDrawable;
    private TabManager tabManager;
    private HashMap<String, Class> router = new HashMap<>();
    private MultiWindowAdapter multiWindowAdapter;
    private boolean realBack = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String BUNDLE_KEY_SIGN = "&";
    private static final String BUNDLE_KEY_TAB_SIZE = "KEY_TAB_SIZE";
    private static final String BUNDLE_KEY_SEARCH_WORD = "KEY_SEARCH_WORD";
    private static final String BUNDLE_KEY_GROUP_SIZE = "KEY_GROUP_SIZE";
    private static final String BUNDLE_KEY_CURRENT_GROUP = "KEY_CURRENT_GROUP";
    private static final String BUNDLE_KEY_CURRENT_TAB = "KEY_CURRENT_TAB";
    private static final String URL_KEY = "URL_KEY";
    private static final String BUNDLE_STORAGE = "SAVED_TABS.parcel";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        tabManager = new TabManager(MainActivity.this);
        installLocalTabRounter();
        initTopBar();
        initTabs();
        checkIntentData(getIntent());
        UpdateController.instance().autoCheckVersion(MainActivity.this);
    }

    private void initTopBar() {
        topText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchSearch();
                    return true;
                }
                return false;
            }
        });
        setSupportActionBar(toolbar);
        multiWindowDrawable = new TextDrawable(MainActivity.this);
        toolbar.setNavigationIcon(multiWindowDrawable);
        toolbar.setNavigationContentDescription(R.string.app_name);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        drawerLayout.openDrawer(Gravity.LEFT);
                    }
                });
    }

    private void initTabs() {
        multiWindowAdapter = new MultiWindowAdapter();
        multiTabListView.setAdapter(multiWindowAdapter);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                updateMultiwindow();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                updateMultiwindow();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        updateMultiwindow();

        restoreLostTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

        WebView.HitTestResult hitTestResult = info.getResult();
        if (hitTestResult.getExtra() != null) {
            switch (hitTestResult.getType()) {
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 带有链接的图片类型
                case WebView.HitTestResult.IMAGE_TYPE:
                    popup.getMenu().setGroupVisible(R.id.picture, true);
                    popup.getMenu().getItem(0).getMenuInfo();
                    break;
                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                    popup.getMenu().setGroupVisible(R.id.txt_link, true);
                    break;
            }

            contextMenuListener.setInfo(hitTestResult);
            contextMenuListener.setSearchWebView(info.getSearchWebView());
            popup.setOnMenuItemClickListener(contextMenuListener);
            popup.show();
        }
    }

    private SearchWebView.OnMenuItemClickListener contextMenuListener = new SearchWebView.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            WebView.HitTestResult info = getInfo();
            switch (item.getItemId()) {
                case R.id.open_pic_new_tab:
                    String url = info.getExtra();
                    Bean bean = new Bean();
                    bean.url = url;
                    bean.time = System.currentTimeMillis();
                    TabGroup parentTabGroup = getTabManager().getCurrentTabGroup();
                    tabManager.loadUrl(bean, true);
                    getTabManager().getCurrentTabGroup().setParent(parentTabGroup);
                    break;
                case R.id.copy_pic_link:
                    url = info.getExtra();
                    String title = info.getExtra();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(title, url);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.save_pic:
                    url = info.getExtra();
                    String mineTye = DownloadHandler.getMimeType(url);
                    if (TextUtils.isEmpty(mineTye)) {
                        mineTye = "image/jpeg";
                    }

                    new MyDownloadListener(MainActivity.this).onDownloadStart(url, "", "", mineTye, 0);
                    break;
                case R.id.shard_pic:
                    url = info.getExtra();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                    break;

                case R.id.open_url_new_tab:
                    url = info.getExtra();
                    bean = new Bean();
                    bean.url = url;
                    bean.time = System.currentTimeMillis();
                    parentTabGroup = getTabManager().getCurrentTabGroup();
                    tabManager.loadUrl(bean, true);
                    getTabManager().getCurrentTabGroup().setParent(parentTabGroup);
                    break;

                case R.id.copy_txt_link_free:

                    break;
            }

            return false;
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        TabGroup currentGroup = tabManager.getCurrentTabGroup();
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
            case R.id.action_share:
                String url = tabManager.getCurrentTabGroup().getUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                break;
            case R.id.action_favorite:
                url = tabManager.getCurrentTabGroup().getCurrentTab().getUrl();
                if (!TextUtils.isEmpty(url) && !url.startsWith(Tab.LOCAL_SCHEMA)) {
                    final Bean bean = new Bean();
                    bean.name = tabManager.getCurrentTabGroup().getCurrentTab().getTitle();
                    if (TextUtils.isEmpty(bean.name)) {
                        bean.name = tabManager.getCurrentTabGroup().getCurrentTab().getUrl();
                    }
                    bean.url = url;
                    bean.time = System.currentTimeMillis();
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... params) {
                            return DaoManager.getInstance().insertFavorite(bean) != 0L;
                        }

                        @Override
                        protected void onPostExecute(Boolean suc) {
                            if (suc) {
                                Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                }
                break;
            case R.id.action_copy_link:
                url = tabManager.getCurrentTabGroup().getCurrentTab().getUrl();
                String title = tabManager.getCurrentTabGroup().getCurrentTab().getTitle();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(title, url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_download:
                Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.action_setting:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_exit:
                exit();
                break;
            case R.id.action_bookmark:
                startActivity(new Intent(MainActivity.this, BookMarkActivity.class));
                break;
            case R.id.action_goforward:
                tabManager.getCurrentTabGroup().goForward();
                break;
            case R.id.action_refresh:
                tabManager.getCurrentTabGroup().getCurrentTab().reload();
                break;
            case R.id.action_auto_fullscreen:
                if (item.isChecked()) {//auto
                    SettingsManager.getInstance().saveAutoFullScreenSp(false);
                } else {
                    SettingsManager.getInstance().saveAutoFullScreenSp(true);
                }
                tabManager.getCurrentTabGroup().getCurrentTab().getView().requestLayout();
                break;
            case R.id.action_desktop:
                WebViewTab tab = (WebViewTab) tabManager.getCurrentTabGroup().getCurrentTab();
                if (tab.isDeskTopUA()) {
                    tab.setUA(Constants.MOBILE_USER_AGENT);
                } else {
                    tab.setUA(Constants.DESKTOP_USER_AGENT);
                }
                tab.reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void touchSearch() {
        String content = tabManager.getCurrentTabGroup().getCurrentTab().getSearchWord();
        int pageNo = tabManager.getCurrentTabGroup().getCurrentTab().getPageNo();
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        Bean bean = new Bean(content);
        bean.pageNo = pageNo;
        intent.putExtra(NAME_BEAN, bean);
        startActivity(intent);
    }

    public void updateMultiwindow() {
        if (multiWindowAdapter != null) {
            multiWindowAdapter.update(MainActivity.this);
        }
    }

    private void installLocalTabRounter() {
        router.put(Tab.URL_HOME, HomeTab.class);
    }

    public Class getRounterClass(String url) {
        return router.get(url);
    }

    public void setCurrentView(View view) {
        webViewContainer.setCurrentView(view);
        progressBar.setVisibility(View.INVISIBLE);
        showTopbar();
    }

    public View setCurrentView(int viewId) {
        View view = webViewContainer.setCurrentView(viewId);
        progressBar.setVisibility(View.INVISIBLE);
        showTopbar();
        return view;
    }

    public void showTopbar() {
        if (isTopbarHide() && SettingsManager.getInstance().isAutoFullScreen()) {
            ObjectAnimator.ofFloat(toolbar, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
            ObjectAnimator.ofFloat(webViewContainer, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
        }
    }

    public void hideTopbar() {
        if (isTopbarShow() && SettingsManager.getInstance().isAutoFullScreen()) {
            ObjectAnimator.ofFloat(toolbar, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
            ObjectAnimator.ofFloat(webViewContainer, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
        }
    }

    private boolean isTopbarHide() {
        return toolbar.getTranslationY() == -Constants.getActionBarH(this);
    }

    private boolean isTopbarShow() {
        return toolbar.getTranslationY() == 0;
    }


    public TabManager getTabManager() {
        return tabManager;
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
                Bean bean = intent.getParcelableExtra(NAME_BEAN);
                if (!TextUtils.isEmpty(bean.url)) {
                    tabManager.loadUrl(bean, false);
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    tabManager.loadUrl(new Bean("", url), true);
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                tabManager.loadUrl(new Bean(searchWord, url), true);
            } else if (Intent.ACTION_MAIN.equals(action)) {
                if (tabManager.getTabGroupCount() == 0) {
                    loadHome(true);
                }
            } else if (Intent.ACTION_ASSIST.equals(action)) {
                if (tabManager.getTabGroupCount() == 0) {
                    loadHome(true);
                } else if (!TextUtils.isEmpty(tabManager.getCurrentTabGroup().getCurrentTab().getSearchWord())) {
                    loadHome(true);
                }
                touchSearch();
            }
        }
    }

    public void loadHome(boolean newTab) {
        tabManager.loadUrl(new Bean("", Tab.URL_HOME), newTab);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawers();
                return true;
            }

            TabGroup tabGroup = tabManager.getCurrentTabGroup();
            if (tabGroup.canGoBack()) {
                tabGroup.goBack();
                return true;
            } else {
                TabGroup parentTabGroup = tabGroup.getParent();
                if (parentTabGroup != null) {
                    tabManager.removeIndex(tabGroup);
                    tabManager.switchTabGroup(parentTabGroup);
                    return true;
                }
            }

            if (!realBack) {
                realBack = true;
                Toast.makeText(MainActivity.this, R.string.exit_hint, Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        realBack = false;
                    }
                }, 1000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onResume() {
        super.onResume();
        tabManager.resumeTabGroupExclude(null);
        updateMultiwindow();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tabManager.pauseTabGroupExclude(null);
        MobclickAgent.onPause(this);
        saveTabs();
    }

    public void refreshTitle() {
        String host = tabManager.getCurrentTabGroup().getCurrentTab().getHost();
        refreshTopText(host);
        multiWindowDrawable.setText(tabManager.getTabGroupCount());
    }

    public void saveTabs() {
        Bundle outState = new Bundle(ClassLoader.getSystemClassLoader());
        List<TabGroup> tabGroupList = tabManager.getList();
        int groupSize = tabGroupList.size();
        outState.putString(BUNDLE_KEY_GROUP_SIZE, groupSize + "");
        int currentGroupIndex = tabManager.getList().indexOf(tabManager.getCurrentTabGroup());
        int currentTabIndex = tabManager.getCurrentTabGroup().getTabs().indexOf(tabManager.getCurrentTabGroup().getCurrentTab());

        outState.putInt(BUNDLE_KEY_CURRENT_GROUP, currentGroupIndex);
        outState.putInt(BUNDLE_KEY_CURRENT_TAB, currentTabIndex);

        for (int g = 0; g < groupSize; g++) {
            TabGroup tabGroup = tabGroupList.get(g);
            ArrayList<SearcherTab> tabs = tabGroup.getTabs();
            outState.putInt(BUNDLE_KEY_TAB_SIZE + g, tabs.size());
            for (int t = 0; t < tabs.size(); t++) {
                SearcherTab tab = tabs.get(t);
                if (!TextUtils.isEmpty(tab.getUrl())) {
                    Bundle state = new Bundle(ClassLoader.getSystemClassLoader());
                    final String key = g + BUNDLE_KEY_SIGN + t;
                    state.putString(URL_KEY, tab.getUrl());
                    if (tab instanceof WebViewTab) {
                        WebViewTab webViewTab = (WebViewTab) tab;
                        webViewTab.getView().saveState(state);
                        outState.putBundle(key, state);
                        state.putString(BUNDLE_KEY_SEARCH_WORD, webViewTab.getSearchWord());
                    } else {
                        outState.putBundle(key, state);
                    }
                }
            }
        }
        FileUtils.writeBundleToStorage(getApplication(), outState, BUNDLE_STORAGE);
    }

    public void restoreLostTabs() {
        Bundle savedState = FileUtils.readBundleFromStorage(getApplication(), BUNDLE_STORAGE);
        if (savedState != null) {
            int groupSize = Integer.valueOf(savedState.getString(BUNDLE_KEY_GROUP_SIZE));
            int currentGroupIndex = savedState.getInt(BUNDLE_KEY_CURRENT_GROUP);
            int currentTabIndex = savedState.getInt(BUNDLE_KEY_CURRENT_TAB);
            for (int g = 0; g < groupSize; g++) {
                int tabSize = savedState.getInt(BUNDLE_KEY_TAB_SIZE + g);
                for (int t = 0; t < tabSize; t++) {
                    final String key = g + BUNDLE_KEY_SIGN + t;
                    Bundle state = savedState.getBundle(key);
                    if (state != null) {
                        String url = state.getString(URL_KEY);
                        if (t == 0) {//first tab
                            Log.i("url ", url);
                            tabManager.loadUrl(new Bean("", url), true);
                        } else {// webView
                            String searchWord = state.getString(BUNDLE_KEY_SEARCH_WORD);
                            Bean bean = DaoManager.getInstance().queryBean(searchWord, url);
                            tabManager.loadUrl(bean, false);
                            Log.i("state ", state.toString());

                            SearcherTab searcherTab = tabManager.getCurrentTabGroup().getCurrentTab();
                            if (searcherTab instanceof WebViewTab) {
                                WebViewTab webViewTab = (WebViewTab) searcherTab;
                                Log.i("webView ", webViewTab.getView().toString());
                                webViewTab.getView().restoreState(state);
                                webViewTab.getView().stopLoading();
                            }
                        }
                    }
                }
            }
            tabManager.restoreTabPos(currentGroupIndex, currentTabIndex);
        }
        FileUtils.deleteBundleInStorage(getApplication(), BUNDLE_STORAGE);
    }

    public void refreshTopText(String text) {
        EditText top = (EditText) findViewById(R.id.top_txt);
        if (TextUtils.isEmpty(text)) {
            top.setText("");
            top.setHint(R.string.search_hint);
        } else {
            top.setText(text);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("tabs", Context.MODE_PRIVATE);
        sp.edit().putString("tabs", tabManager.getSaveState()).apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_tab:
                loadHome(true);
                drawerLayout.closeDrawers();
                break;
            case R.id.close_tab:
                if (tabManager.getTabGroupCount() == 1) {
                    exit();
                } else {
                    TabGroup tabGroup = (TabGroup) v.getTag();
                    tabManager.removeTabGroup(tabGroup);
                    updateMultiwindow();
                }
                break;
            case R.id.multi_window_item:
                TabGroup tabGroup = (TabGroup) v.getTag(R.id.multi_window_item_tag);
                tabManager.switchTabGroup(tabGroup);
                drawerLayout.closeDrawers();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tabs", tabManager.getSaveState());
    }

    public void refreshProgress(WebViewTab webViewTab, int progress) {
        if (tabManager.getCurrentTabGroup().getCurrentTab() == webViewTab) {
            progressBar.setProgress(progress);
            if (progress == 100) {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

}
