package peter.util.searcher.activity;

import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.TabManager;
import peter.util.searcher.adapter.MultiWindowAdapter;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.tab.FavoriteTab;
import peter.util.searcher.tab.HistorySearchTab;
import peter.util.searcher.tab.HistoryUrlTab;
import peter.util.searcher.tab.HomeTab2;
import peter.util.searcher.tab.SettingTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.utils.Constants;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.view.WebViewContainer;

/**
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
    @BindView(R.id.top)
    View top;

    private TabManager tabManager;
    private HashMap<String, Class> router = new HashMap<>();
    private MultiWindowAdapter multiWindowAdapter;
    private boolean realBack = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init(savedInstanceState);
    }

    private void initTabs(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String saved = savedInstanceState.getString("tabs");
            if (TextUtils.isEmpty(saved)) {
                tabManager.restoreState(saved);
            }
        }
    }

    private void init(Bundle savedInstanceState) {
        tabManager = new TabManager(MainActivity.this);
        installLocalTabRounter();
        initTopBar();
        initMultiLayout();
        initTabs(savedInstanceState);
        checkIntentData(getIntent());
    }

    public void initMultiLayout() {
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
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
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
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        drawerLayout.openDrawer(Gravity.LEFT);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String url = tabManager.getCurrentTabGroup().getCurrentTab().getUrl();
        if (url.startsWith(Tab.LOCAL_SCHEMA)) {
            menu.setGroupVisible(R.id.web_sites, false);
        } else {
            menu.setGroupVisible(R.id.web_sites, true);
        }

        if (tabManager.getCurrentTabGroup().canGoForward()) {
            menu.findItem(R.id.action_goforward).setVisible(true);
        } else {
            menu.findItem(R.id.action_goforward).setVisible(false);
        }

        menu.findItem(R.id.action_auto_fullscreen).setChecked(Constants.AUTO_FULLSCREEN);

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
            case R.id.action_copy_link:
                url = tabManager.getCurrentTabGroup().getUrl();
                String title = tabManager.getCurrentTabGroup().getTitle();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(title, url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_browser:
                url = tabManager.getCurrentTabGroup().getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                break;
            case R.id.action_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
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
                if(item.isChecked()) {//auto
                    saveAutoFullScreen(false);
                }else {
                    saveAutoFullScreen(true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAutoFullScreen(boolean show) {
        Constants.AUTO_FULLSCREEN = show;
        SharedPreferences sp = getSharedPreferences("fullscreen", MODE_PRIVATE);
        sp.edit().putBoolean("auto", show).apply();
        tabManager.getCurrentTabGroup().getCurrentTab().getView().requestLayout();

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
        router.put(Tab.URL_HOME2, HomeTab2.class);
        router.put(Tab.URL_SETTING, SettingTab.class);
        router.put(Tab.URL_FAVORITE, FavoriteTab.class);
        router.put(Tab.URL_HISTORY_SEARCH, HistorySearchTab.class);
        router.put(Tab.URL_HISTORY_URL, HistoryUrlTab.class);
    }

    public Class getRounterClass(String url) {
        return router.get(url);
    }

    public void setCurrentView(View view) {
        webViewContainer.setCurrentView(view);
        showTopbar();
    }

    public View setCurrentView(int viewId) {
        View view = webViewContainer.setCurrentView(viewId);
        showTopbar();
        return view;
    }

    public void showTopbar() {
        if (isTopbarHide() && Constants.AUTO_FULLSCREEN) {
            ObjectAnimator.ofFloat(top, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
            ObjectAnimator.ofFloat(progressBar, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
            ObjectAnimator.ofFloat(webViewContainer, "translationY", -Constants.getActionBarH(this), 0).setDuration(300).start();
        }
    }

    public void hideTopbar() {
        if (isTopbarShow() && Constants.AUTO_FULLSCREEN) {
            ObjectAnimator.ofFloat(top, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
            ObjectAnimator.ofFloat(progressBar, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
            ObjectAnimator.ofFloat(webViewContainer, "translationY", 0, -Constants.getActionBarH(this)).setDuration(300).start();
        }
    }

    private boolean isTopbarHide() {
        return top.getTranslationY() == -Constants.getActionBarH(this);
    }

    private boolean isTopbarShow() {
        return top.getTranslationY() == 0;
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
                    tabManager.loadUrl(new Bean(url), true);
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                tabManager.loadUrl(new Bean(url, searchWord), true);
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
        tabManager.loadUrl(new Bean("", Tab.URL_HOME2), newTab);
//        loadUrl(Tab.URL_HOME, newTab);
//        loadUrl("http://m.2345.com/websitesNavigation.htm", newTab);
//        loadUrl(getString(R.string.fast_enter_navigation_url), newTab);
//        loadUrl("http://top.baidu.com/m#buzz/1", newTab);
//        tabManager.loadUrl("https://github.com/trending", newTab);
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
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tabManager.pauseTabGroupExclude(null);
        MobclickAgent.onPause(this);
    }

    public void refreshTitle() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = getSharedPreferences("tabs", Context.MODE_PRIVATE);
        sp.edit().putString("tabs", tabManager.getSaveState()).apply();
    }

    public void exit() {
        super.exit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_tab:
                drawerLayout.closeDrawers();
                loadHome(true);
                break;
            case R.id.close_tabs:
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i("peter", "onConfigurationChanged ORIENTATION_LANDSCAPE");
                setFullscreen(true, true);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i("peter", "onConfigurationChanged ORIENTATION_PORTRAIT");
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
