package peter.util.searcher.activity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.SettingTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.view.MultiWindowListView;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private View bottomBar;
    private TabManager manager;
    private TextView multiWindow;
    private FrameLayout mContainer;
    private HashMap<String, Class> router = new HashMap<>();
    private int[] searchDrawableRes = {R.drawable.abc_ic_search_api_mtrl_alpha, R.drawable.abc_ic_clear_mtrl_alpha};
    private int searchDrawableLevel;

    private MenuWindowAdapter menuWindowAdapter;
    private Dialog menuDialog;

    private MultiWindowAdapter multiWindowAdapter;
    private Dialog multiWindowdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            setContentView(R.layout.activity_main);
            init();
            checkIntentData(intent);
        }
    }

    private void init() {
        bottomBar = findViewById(R.id.bottom_bar);
        mContainer = (FrameLayout) findViewById(R.id.container);
        multiWindow = (TextView) findViewById(R.id.multi_btn_txt);
        manager = new TabManager(MainActivity.this);
        installLocalTabRounter();
        initMenuDialog();
        initMultiWindowDialog();
    }

    private void initMenuDialog() {
        menuDialog = new Dialog(MainActivity.this, R.style.multiwindow_Dialog);
        menuDialog.setContentView(R.layout.layout_menu_window);
        MultiWindowListView menuList = (MultiWindowListView) menuDialog.findViewById(R.id.menu_window);
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(R.string.action_refresh);
        arrayList.add(R.string.action_share);
        arrayList.add(R.string.action_collect);
        arrayList.add(R.string.action_copy_link);
        arrayList.add(R.string.action_exit);
        menuWindowAdapter = new MenuWindowAdapter(MainActivity.this, arrayList);
        menuList.setAdapter(menuWindowAdapter);
        menuDialog.getWindow().getAttributes().windowAnimations = R.style.multiwindow_anim;
        menuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        menuList.setOutSideTouchItemCallBack(new MultiWindowListView.OutSideTouchItemCallBack() {
            @Override
            public void outside() {
                menuDialog.dismiss();
            }
        });
    }

    public void updateMultiwindow() {
        if (multiWindowAdapter != null) {
            multiWindowAdapter.update(MainActivity.this);
        }
    }

    private void initMultiWindowDialog() {
        multiWindowdialog = new Dialog(MainActivity.this, R.style.multiwindow_Dialog);
        multiWindowdialog.setContentView(R.layout.layout_multi_window);
        MultiWindowListView multiTabListView = (MultiWindowListView) multiWindowdialog.findViewById(R.id.multi_window);
        multiWindowAdapter = new MultiWindowAdapter();
        multiTabListView.setAdapter(multiWindowAdapter);
        multiWindowdialog.getWindow().getAttributes().windowAnimations = R.style.multiwindow_anim;
        multiWindowdialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        multiTabListView.setOutSideTouchItemCallBack(new MultiWindowListView.OutSideTouchItemCallBack() {
            @Override
            public void outside() {
                multiWindowdialog.dismiss();
            }
        });

        multiWindowdialog.findViewById(R.id.add).setActivated(true);
        multiWindowdialog.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiWindowdialog.dismiss();
                loadUrl(Tab.URL_HOME, true);
            }
        });
    }

    private void installLocalTabRounter() {
        router.put(Tab.URL_HOME, HomeTab.class);
        router.put(Tab.URL_SETTING, SettingTab.class);
        router.put(Tab.URL_FAVORITE, FavoriteTab.class);
        router.put(Tab.URL_HISTORY, HistoryTab.class);
    }

    public Class getRounterClass(String url) {
        return router.get(url);
    }

    public void setStatusLevel(int level) {
        ImageView search = (ImageView) findViewById(R.id.search);
        search.setImageResource(searchDrawableRes[level]);
        searchDrawableLevel = level;
    }

    public void setCurrentView(View view) {
        mContainer.removeAllViews();
        mContainer.addView(view);
    }

    public View setCurrentView(int viewId) {
        mContainer.removeAllViews();
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        View mView = factory.inflate(viewId, mContainer, false);
        mContainer.addView(mView);
        return mView;
    }

    public TabManager getManager() {
        return manager;
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
                    loadUrl(url, searchWord, false);
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    loadUrl(url, true);
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                loadUrl(url, searchWord, true);
            } else if (Intent.ACTION_MAIN.equals(action)) {
                if (manager.getTabGroupCount() == 0) {
                    loadUrl(Tab.URL_HOME, true);
//                    loadUrl("http://m.2345.com/websitesNavigation.htm", true);
                }
            }
        }
    }

    public void loadUrl(String url, String searchWord, boolean newTab) {
        manager.loadUrl(url, searchWord, newTab);
    }

    public void loadUrl(String url, boolean newTab) {
        manager.loadUrl(url, newTab);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (manager.canGoBack()) {
                manager.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public SearcherTab getCurrentTab() {
        return manager.getCurrentTabGroup().getCurrentTab();
    }

    public void removeTabGroup(SearcherTab tab) {
        TabGroup tabGroup = manager.getTabGroup(tab);
        if (tabGroup != null) {
            manager.removeTabGroup(tabGroup);
        }
    }

    protected void onResume() {
        super.onResume();
        manager.resumeTabGroupExclude(null);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.pauseTabGroupExclude(null);
        MobclickAgent.onPause(this);
    }

    public void refreshBottomBar() {
        //multi button
        int count = manager.getTabGroupCount();
        multiWindow.setText(count + "");

        setStatusLevel(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                Tab tab = manager.getCurrentTabGroup();
                if (tab.canGoBack()) {
                    tab.goBack();
                }
                break;
//            case R.id.go_forward:
//                tab = manager.getCurrentTabGroup();
//                if (tab.canGoForward()) {
//                    tab.goForward();
//                }
//                break;
            case R.id.search:
                switch (searchDrawableLevel) {
                    case 0:
                        String content = manager.getCurrentTabGroup().getCurrentTab().getSearchWord();
                        startSearcheActivity(content);
                        break;
                    case 1:
                        View view = manager.getCurrentTabGroup().getCurrentTab().getView();
                        if (view instanceof WebView) {
                            ((WebView) view).stopLoading();
                            setStatusLevel(0);
                        }
                        break;
                }

                break;
            case R.id.multi_btn:
                multiWindowdialog.show();
                updateMultiwindow();
                break;
            case R.id.menu:
//                popupMenu(v);
                menuDialog.show();
                break;
            case R.id.close_tab:
                TabGroup tabGroup = (TabGroup) v.getTag();
                manager.removeTabGroup(tabGroup);
                updateMultiwindow();
                break;
            case R.id.multi_window_item:
                tabGroup = (TabGroup) v.getTag(R.id.multi_window_item_tag);
                manager.switchTabGroup(tabGroup);
                multiWindowdialog.dismiss();
                break;
            case R.id.menu_window_item:
                Integer itemRes = (Integer) v.getTag(R.id.menu_window_item_tag);
                switch (itemRes) {
                    case R.string.action_exit:
                        exit();
                        break;
                    case R.string.action_copy_link:
                        String url = getManager().getCurrentTabGroup().getUrl();
                        String title = getManager().getCurrentTabGroup().getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                        break;
                    case R.string.action_collect:
                        if (!TextUtils.isEmpty(getManager().getCurrentTabGroup().getUrl())) {
                            Bean bean = new Bean();
                            bean.name = getManager().getCurrentTabGroup().getTitle();
                            if (TextUtils.isEmpty(bean.name)) {
                                bean.name = getManager().getCurrentTabGroup().getUrl();
                            }
                            bean.url = getManager().getCurrentTabGroup().getUrl();
                            bean.time = System.currentTimeMillis();
                            SqliteHelper.instance(MainActivity.this).insertFav(bean);
                            Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.string.action_share:
                        url = getManager().getCurrentTabGroup().getUrl();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                        break;
                    case R.string.action_refresh:
                        getManager().getCurrentTabGroup().reload();
                        break;
                }
                menuDialog.dismiss();
                break;
        }
    }

    public void startSearcheActivity(String word) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra(NAME_WORD, word);
        startActivity(intent);
    }

    private void popupMenu(View menu) {
        PopupMenu popup = new PopupMenu(this, menu);
        popup.getMenuInflater().inflate(R.menu.web, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
//                    case R.id.action_browser:
//                        String url = manager.getCurrentTabGroup().getUrl();
//                        Intent intent = new Intent();
//                        intent.setAction("android.intent.action.VIEW");
//                        Uri content_url = Uri.parse(url);
//                        intent.setData(content_url);
//                        startActivity(intent);
//                        break;
                    case R.id.action_share:
                        String url = manager.getCurrentTabGroup().getUrl();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                        break;
                    case R.id.action_collect:
                        if (!TextUtils.isEmpty(manager.getCurrentTabGroup().getUrl())) {
                            Bean bean = new Bean();
                            bean.name = manager.getCurrentTabGroup().getTitle();
                            if (TextUtils.isEmpty(bean.name)) {
                                bean.name = manager.getCurrentTabGroup().getUrl();
                            }
                            bean.url = manager.getCurrentTabGroup().getUrl();
                            bean.time = System.currentTimeMillis();
                            SqliteHelper.instance(MainActivity.this).insertFav(bean);
                            Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.action_copy_link:
                        url = manager.getCurrentTabGroup().getUrl();
                        String title = manager.getCurrentTabGroup().getTitle();
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
