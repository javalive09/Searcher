package peter.util.searcher.activity;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import peter.util.searcher.adapter.MenuWindowAdapter;
import peter.util.searcher.adapter.MultiWindowAdapter;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.tab.DownloadTab;
import peter.util.searcher.tab.FavoriteTab;
import peter.util.searcher.tab.HistorySearchTab;
import peter.util.searcher.tab.HistoryUrlTab;
import peter.util.searcher.tab.HomeTab;
import peter.util.searcher.tab.HomeTab2;
import peter.util.searcher.tab.SettingTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.view.CustomSwipeRefreshLayout;
import peter.util.searcher.view.DialogContainer;
import peter.util.searcher.view.MenuWindowGridView;
import peter.util.searcher.view.MultiWindowListView;
import peter.util.searcher.view.WebViewContainer;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private View bottomBar;
    private TabManager tabManager;
    private TextView multiWindowBtn;
    private HashMap<String, Class> router = new HashMap<>();

    private DialogContainer menu;
    private DialogContainer multiWindow;
    private MultiWindowAdapter multiWindowAdapter;
    private WebViewContainer webViewContainer;
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout drawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initTabs(savedInstanceState);
        checkIntentData(getIntent());
    }

    private void initTabs(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String saved = savedInstanceState.getString("tabs");
            if (TextUtils.isEmpty(saved)) {
                tabManager.restoreState(saved);
            }
        }
    }

    private void init() {
        bottomBar = findViewById(R.id.bottom_bar);
        multiWindowBtn = (TextView) findViewById(R.id.multi_btn_txt);
        webViewContainer = (WebViewContainer) findViewById(R.id.webview_container);
        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.progress_color, R.color.progress_color,
                R.color.progress_color, R.color.progress_color);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tabManager.getCurrentTabGroup().getCurrentTab().reload();
                mHandler.removeCallbacks(complete);
                mHandler.postDelayed(complete, 500);
            }
        });
        tabManager = new TabManager(MainActivity.this);
        installLocalTabRounter();
        initTopBar();
        initMenu();
        initMultiWindow();
        initMultiLayout();
    }

    public void initMultiLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView multiTabListView = (ListView) findViewById(R.id.tabs);
        multiWindowAdapter = new MultiWindowAdapter();
        multiTabListView.setAdapter(multiWindowAdapter);
        drawerLayout.findViewById(R.id.add_tab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                loadHome(true);
            }
        });
    }

    Runnable complete = new Runnable() {
        @Override
        public void run() {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    private void initTopBar() {
        findViewById(R.id.top_txt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchSearch();
                    return true;
                }
                return false;
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web, menu);
        return true;
    }

    private void touchSearch() {
        String content = tabManager.getCurrentTabGroup().getCurrentTab().getSearchWord();
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra(NAME_WORD, content);
        startActivity(intent);
        closeDialogFast();
    }

    private void initMenu() {
        menu = (DialogContainer) findViewById(R.id.menu_window_container);
        MenuWindowGridView menuList = (MenuWindowGridView) menu.findViewById(R.id.menu_window);
        ArrayList<MenuWindowAdapter.MenuItem> arrayList = new ArrayList<>();
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_folder_special, R.string.fast_enter_favorite));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_star_border, R.string.action_collect));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_share, R.string.action_share));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_file_download, R.string.action_download));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_url_history, R.string.action_url_history));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_search_history, R.string.action_search_history));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_settings, R.string.fast_enter_setting));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.ic_power_settings_new, R.string.action_exit));
        MenuWindowAdapter menuWindowAdapter = new MenuWindowAdapter(MainActivity.this, arrayList);
        menuList.setAdapter(menuWindowAdapter);
        menu.setOutSideTouchItemCallBack(new DialogContainer.OutSideTouchItemCallBack() {
            @Override
            public void outside() {
                menu.hide();
            }
        });
    }

    public void updateMultiwindow() {
        if (multiWindowAdapter != null) {
            multiWindowAdapter.update(MainActivity.this);
        }
    }

    private void initMultiWindow() {
        multiWindow = (DialogContainer) findViewById(R.id.multi_window_container);
        MultiWindowListView multiTabListView = (MultiWindowListView) multiWindow.findViewById(R.id.multi_window);
        multiWindowAdapter = new MultiWindowAdapter();
        multiTabListView.setAdapter(multiWindowAdapter);
        DialogContainer container = (DialogContainer) multiWindow.findViewById(R.id.multi_window_container);
        container.setOutSideTouchItemCallBack(new DialogContainer.OutSideTouchItemCallBack() {
            @Override
            public void outside() {
                multiWindow.hide();
            }
        });

        multiWindow.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiWindow.hide();
                loadHome(true);
            }
        });
    }

    private void installLocalTabRounter() {
        router.put(Tab.URL_HOME, HomeTab.class);
        router.put(Tab.URL_HOME2, HomeTab2.class);
        router.put(Tab.URL_SETTING, SettingTab.class);
        router.put(Tab.URL_FAVORITE, FavoriteTab.class);
        router.put(Tab.URL_HISTORY_SEARCH, HistorySearchTab.class);
        router.put(Tab.URL_HISTORY_URL, HistoryUrlTab.class);
        router.put(Tab.URL_DOWNLOAD, DownloadTab.class);
    }

    public Class getRounterClass(String url) {
        return router.get(url);
    }

    public void setCurrentView(View view) {
        webViewContainer.setCurrentView(view);
        setSwipeRefresh(view);
    }

    public View setCurrentView(int viewId) {
        View view = webViewContainer.setCurrentView(viewId);
        setSwipeRefresh(view);
        return view;
    }

    private void setSwipeRefresh(View view) {
        if (view instanceof WebView) {
            mSwipeRefreshLayout.setEnabled(true);
            final WebView webView = (WebView) view;
            mSwipeRefreshLayout.setCanChildScrollUpCallback(new CustomSwipeRefreshLayout.CanChildScrollUpCallback() {
                @Override
                public boolean canSwipeRefreshChildScrollUp() {
                    return webView.getScrollY() > 0;
                }
            });
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);
        }
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
                String url = (String) intent.getSerializableExtra(NAME_URL);
                if (!TextUtils.isEmpty(url)) {
                    String searchWord = intent.getStringExtra(NAME_WORD);
                    tabManager.loadUrl(url, searchWord, false);
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    tabManager.loadUrl(url, true);
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                tabManager.loadUrl(url, searchWord, true);
            } else if (Intent.ACTION_MAIN.equals(action)) {
                if (tabManager.getTabGroupCount() == 0) {
                    loadHome(true);
                }
            } else if (Intent.ACTION_ASSIST.equals(action)) {
                if (tabManager.getTabGroupCount() == 0) {
                    loadHome(true);
                }
                touchSearch();
            }
        }
    }

    public void loadHome(boolean newTab) {
        tabManager.loadUrl(Tab.URL_HOME2, newTab);
//        loadUrl(Tab.URL_HOME, newTab);
//        loadUrl("http://m.2345.com/websitesNavigation.htm", newTab);
//        loadUrl(getString(R.string.fast_enter_navigation_url), newTab);
//        loadUrl("http://top.baidu.com/m#buzz/1", newTab);
    }

    boolean realBack = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
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

    private Handler mHandler = new Handler(Looper.getMainLooper());

    protected void onResume() {
        super.onResume();
        tabManager.resumeTabGroupExclude(null);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tabManager.pauseTabGroupExclude(null);
        MobclickAgent.onPause(this);
    }

    public void refreshBottomBar() {
        //multi button
        int count = tabManager.getTabGroupCount();
        multiWindowBtn.setText(count + "");

        //go back
        if (tabManager.getCurrentTabGroup().canGoBack()) {
            findViewById(R.id.go_back).setEnabled(true);
        } else {
            findViewById(R.id.go_back).setEnabled(false);
        }

        refreshGoForward(false);
        String host = tabManager.getCurrentTabGroup().getCurrentTab().getHost();
        refreshTopText(host);
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

    public void refreshProgress(int progress) {
        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        int color = getResources().getColor(R.color.progress_color);
        bar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        bar.setProgress(progress);
        if (progress == 100) {
            bar.post(new Runnable() {
                @Override
                public void run() {
                    bar.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            bar.setVisibility(View.VISIBLE);
        }
    }

    public void refreshGoForward(boolean isActivate) {
        if (isActivate) {
            findViewById(R.id.go_forward).setActivated(true);
            findViewById(R.id.go_forward).setEnabled(true);
        } else {
            findViewById(R.id.go_forward).setActivated(false);
            if (tabManager.getCurrentTabGroup().canGoForward()) {
                findViewById(R.id.go_forward).setEnabled(true);
            } else {
                findViewById(R.id.go_forward).setEnabled(false);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("tabs", Context.MODE_PRIVATE);
        sp.edit().putString("tabs", tabManager.getSaveState()).apply();
    }

    public void exit() {
        super.exit();
        menu.hide();
        multiWindow.hide();
    }

    private void closeDialog() {
        if (multiWindow != null) {
            multiWindow.hide();
        }
        if (menu != null) {
            menu.hide();
        }
    }

    private void closeDialogFast() {
        if (multiWindow != null) {
            multiWindow.setVisibility(View.INVISIBLE);
        }
        if (menu != null) {
            menu.setVisibility(View.INVISIBLE);
        }
    }

    private boolean isDialogShow() {
        return multiWindow.getVisibility() == View.VISIBLE || menu.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.opt:
                break;
            case R.id.go_back:
                Tab tab = tabManager.getCurrentTabGroup();
                if (tab.canGoBack()) {
                    tab.goBack();
                    closeDialog();
                }
                break;
            case R.id.go_forward:
                if (v.isActivated()) {
                    View view = tabManager.getCurrentTabGroup().getCurrentTab().getView();
                    if (view instanceof WebView) {
                        ((WebView) view).stopLoading();
                        closeDialog();
                    }
                    refreshGoForward(false);
                } else if (v.isEnabled()) {
                    tab = tabManager.getCurrentTabGroup();
                    if (tab.canGoForward()) {
                        tab.goForward();
                        closeDialog();
                    }
                }
                break;
            case R.id.home:
                loadHome(false);
                closeDialog();
                break;
            case R.id.multi_btn:

                if (multiWindow != null) {
                    if (multiWindow.getVisibility() == View.INVISIBLE) {
                        drawerLayout.openDrawer(Gravity.RIGHT);
                        updateMultiwindow();
                    } else {
                        multiWindow.hide();
                    }
                }


                break;
//            case R.id.menu:
//                if (multiWindow != null) {
//                    multiWindow.hide();
//                }
//
//                if (menu != null) {
//                    if (menu.getVisibility() == View.INVISIBLE) {
//                        menu.show();
//                    } else {
//                        menu.hide();
//                    }
//                }
//                break;
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
                if (multiWindow != null) {
                    multiWindow.hide();
                }
                break;
            case R.id.menu_window_item:
                MenuWindowAdapter.MenuItem itemRes = (MenuWindowAdapter.MenuItem) v.getTag(R.id.menu_window_item_tag);
                switch (itemRes.titleRes) {
                    case R.string.action_exit:
                        exit();
                        return;
                    case R.string.action_copy_link:
                        String url = tabManager.getCurrentTabGroup().getUrl();
                        String title = tabManager.getCurrentTabGroup().getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                        break;
                    case R.string.action_collect:
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
                                    return SqliteHelper.instance(MainActivity.this).insertFav(bean);
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
                    case R.string.action_share:
                        url = tabManager.getCurrentTabGroup().getUrl();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                        break;
                    case R.string.action_refresh:
                        tabManager.getCurrentTabGroup().getCurrentTab().reload();
                        break;
                    case R.string.action_download:
                        tabManager.loadUrl(Tab.URL_DOWNLOAD, false);
                        break;
                    case R.string.fast_enter_favorite:
                        tabManager.loadUrl(Tab.URL_FAVORITE, false);
                        break;
                    case R.string.action_search_history:
                        tabManager.loadUrl(Tab.URL_HISTORY_SEARCH, false);
                        break;
                    case R.string.fast_enter_setting:
                        tabManager.loadUrl(Tab.URL_SETTING, false);
                        break;
                    case R.string.action_url_history:
                        tabManager.loadUrl(Tab.URL_HISTORY_URL, false);
                        break;
                }
                if (menu != null) {
                    menu.hide();
                }
                break;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tabs", tabManager.getSaveState());
    }

}
