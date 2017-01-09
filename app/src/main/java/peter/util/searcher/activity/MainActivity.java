package peter.util.searcher.activity;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import peter.util.searcher.adapter.MenuWindowAdapter;
import peter.util.searcher.adapter.MultiWindowAdapter;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.tab.FavoriteTab;
import peter.util.searcher.tab.HistoryTab;
import peter.util.searcher.tab.HomeTab;
import peter.util.searcher.tab.HomeTab2;
import peter.util.searcher.tab.SettingTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.utils.UrlUtils;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_home2);
        Intent intent = getIntent();
        if (intent != null) {
            setContentView(R.layout.activity_main);
            init();
            checkIntentData(intent);
        }
    }

    private void init() {
        bottomBar = findViewById(R.id.bottom_bar);
        multiWindowBtn = (TextView) findViewById(R.id.multi_btn_txt);
        webViewContainer = (WebViewContainer) findViewById(R.id.webview_container);
        tabManager = new TabManager(MainActivity.this);
        installLocalTabRounter();
        initMenu();
        initMultiWindow();
    }

    private void initMenu() {
        menu = (DialogContainer) findViewById(R.id.menu_window_container);
        MenuWindowGridView menuList = (MenuWindowGridView) menu.findViewById(R.id.menu_window);
        ArrayList<MenuWindowAdapter.MenuItem> arrayList = new ArrayList<>();
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_favorite_plus, R.string.action_collect));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_share, R.string.action_share));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_copy, R.string.action_copy_link));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_refresh, R.string.action_refresh));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_favorite_folder, R.string.fast_enter_favorite));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_history, R.string.fast_enter_history));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_setting, R.string.fast_enter_setting));
        arrayList.add(new MenuWindowAdapter.MenuItem(R.drawable.menu_power, R.string.action_exit));
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
                loadHome();
            }
        });
    }

    private void installLocalTabRounter() {
        router.put(Tab.URL_HOME, HomeTab.class);
        router.put(Tab.URL_HOME2, HomeTab2.class);
        router.put(Tab.URL_SETTING, SettingTab.class);
        router.put(Tab.URL_FAVORITE, FavoriteTab.class);
        router.put(Tab.URL_HISTORY, HistoryTab.class);
    }

    public Class getRounterClass(String url) {
        return router.get(url);
    }


    public void setCurrentView(View view) {
        webViewContainer.setCurrentView(view);
    }

    public View setCurrentView(int viewId) {
        return webViewContainer.setCurrentView(viewId);
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
                    loadHome();
                }
            }
        }
    }

    public void loadHome() {
        tabManager.loadUrl(Tab.URL_HOME2, true);
//        loadUrl(Tab.URL_HOME, true);
//        loadUrl("http://m.2345.com/websitesNavigation.htm", true);
//        loadUrl(getString(R.string.fast_enter_navigation_url), true);
//        loadUrl("http://top.baidu.com/m#buzz/1", true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(isDialogShow()) {
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
        }
        return super.onKeyDown(keyCode, event);
    }

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
        TextView top = (TextView) findViewById(R.id.top_txt);
        if (TextUtils.isEmpty(text)) {
            top.setText("...");
        } else {
            top.setText(text);
        }
    }

    public void refreshProgress(int progress) {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        bar.setProgress(progress);
        if (progress < 100) {
            if (bar.getVisibility() == View.INVISIBLE) {
                bar.setVisibility(View.VISIBLE);
            }
        } else {
            bar.setVisibility(View.INVISIBLE);
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
            case R.id.top_bar:
                String content = tabManager.getCurrentTabGroup().getCurrentTab().getUrl();
                startSearcheActivity(content);
                closeDialogFast();
                break;
            case R.id.search:
                content = tabManager.getCurrentTabGroup().getCurrentTab().getSearchWord();
                startSearcheActivity(content);
                closeDialogFast();
                break;
            case R.id.multi_btn:
                if (menu != null) {
                    menu.hide();
                }

                if (multiWindow != null) {
                    if (multiWindow.getVisibility() == View.INVISIBLE) {
                        multiWindow.show();
                        updateMultiwindow();
                    } else {
                        multiWindow.hide();
                    }
                }

                break;
            case R.id.menu:
                if (multiWindow != null) {
                    multiWindow.hide();
                }

                if (menu != null) {
                    if (menu.getVisibility() == View.INVISIBLE) {
                        menu.show();
                    } else {
                        menu.hide();
                    }
                }
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
                        if (!TextUtils.isEmpty(tabManager.getCurrentTabGroup().getUrl())) {
                            Bean bean = new Bean();
                            bean.name = tabManager.getCurrentTabGroup().getTitle();
                            if (TextUtils.isEmpty(bean.name)) {
                                bean.name = tabManager.getCurrentTabGroup().getUrl();
                            }
                            bean.url = tabManager.getCurrentTabGroup().getUrl();
                            bean.time = System.currentTimeMillis();
                            SqliteHelper.instance(MainActivity.this).insertFav(bean);
                            Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
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
                    case R.string.fast_enter_favorite:
                        tabManager.loadUrl(Tab.URL_FAVORITE, false);
                        break;
                    case R.string.fast_enter_history:
                        tabManager.loadUrl(Tab.URL_HISTORY, false);
                        break;
                    case R.string.fast_enter_setting:
                        tabManager.loadUrl(Tab.URL_SETTING, false);
                        break;
                }
                if (menu != null) {
                    menu.hide();
                }
                break;
        }
    }

    public void startSearcheActivity(String url) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra(NAME_URL, url);
        startActivity(intent);
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
