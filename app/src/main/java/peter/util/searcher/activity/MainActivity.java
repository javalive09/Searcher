package peter.util.searcher.activity;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;

import peter.util.searcher.bean.Bean;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.tab.FavoriteTab;
import peter.util.searcher.tab.HistoryTab;
import peter.util.searcher.tab.HomeTab;
import peter.util.searcher.tab.SettingTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.utils.UrlUtils;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private View bottomBar;
    private TabManager manager;
    private TextView multiWindow;
    private View progressBar;
    private FrameLayout mContainer;
    private HashMap<String, Class> router = new HashMap<>();
    public static final String URL_INFO = "url_info";
    private PopupWindow multiTabPopupWindow;
    private MultiWindowAdapter multiWindowAdapter;

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
        progressBar = findViewById(R.id.status);
        bottomBar = findViewById(R.id.bottom_bar);
        mContainer = (FrameLayout) findViewById(R.id.container);
        multiWindow = (TextView) findViewById(R.id.multi_btn);
        findViewById(R.id.bottom_search_btn_container).setOnTouchListener(new View.OnTouchListener() {

            int mTouchSlop = 0;
            int startX = 0;
            boolean slide = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (!slide) {
                            onClick(v);
                        }

                        break;
                    case MotionEvent.ACTION_DOWN:
                        if (mTouchSlop == 0) {
                            ViewConfiguration configuration = android.view.ViewConfiguration.get(v.getContext());
                            mTouchSlop = configuration.getScaledTouchSlop();
                        }
                        startX = (int) event.getX();
                        slide = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (!slide) {
                            if (Math.abs(startX - (int) event.getX()) > mTouchSlop) {//scroll x
                                View child = ((FrameLayout) v).getChildAt(0);
                                if (child != null) {
                                    int childWidth = child.getWidth();
                                    if (v.getWidth() < childWidth) {//can scroll
                                        slide = true;
                                    }
                                }
                            }
                        }
                        break;
                }

                return false;
            }
        });
        manager = new TabManager(MainActivity.this);
        installLocalTabRounter();
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
        LevelListDrawable d = (LevelListDrawable) progressBar.getBackground();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
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
            Tab tab = manager.getCurrentTabGroup();
            if (tab.canGoBack()) {
                tab.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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

    public void refreshBottomBar() {
        //multi button
        int count = manager.getTabGroupCount();
        multiWindow.setText(count + "");

        //back button
        if (manager.getCurrentTabGroup().canGoBack()) {
            findViewById(R.id.back).setEnabled(true);
        } else {
            findViewById(R.id.back).setEnabled(false);
        }
        //go button
        if (manager.getCurrentTabGroup().canGoForward()) {
            findViewById(R.id.go).setEnabled(true);
        } else {
            findViewById(R.id.go).setEnabled(false);
        }

        //bottom search btn
        TextView bottomSearch = (TextView) findViewById(R.id.bottom_search_btn);
        String title = manager.getCurrentTabGroup().getTitle();
        bottomSearch.setHint(title);
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
            case R.id.go:
                tab = manager.getCurrentTabGroup();
                if (tab.canGoForward()) {
                    tab.goForward();
                }
                break;
            case R.id.bottom_search_btn_container:
                String content = manager.getCurrentTabGroup().getCurrentTab().getSearchWord();
                if (TextUtils.isEmpty(content)) {
                    content = manager.getCurrentTabGroup().getCurrentTab().getUrl();
                }
                startSearcheActivity(content);
                break;
            case R.id.multi_btn:
                showMultiTabWindow();
                break;
            case R.id.menu:
                popupMenu(v);
                break;
            case R.id.close_tab:


        }
    }

    public void startSearcheActivity(String url) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra(URL_INFO, url);
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

    private void initMultiWindow() {
        if (multiTabPopupWindow == null) {
            View root = getLayoutInflater().inflate(R.layout.layout_multi_window, null, false);
            multiTabPopupWindow = new PopupWindow(root, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            multiTabPopupWindow.setAnimationStyle(R.style.multiwindow_style);
            multiTabPopupWindow.setOutsideTouchable(true);
            multiTabPopupWindow.setFocusable(true);
            // Removes default background.
            multiTabPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ListView multiTabListView = (ListView) root.findViewById(R.id.multi_window);
            multiWindowAdapter = new MultiWindowAdapter();
            multiTabListView.setAdapter(multiWindowAdapter);
        }
    }

    public void updateMultiWindow() {
        multiWindowAdapter.update(MainActivity.this);
    }

    private void showMultiTabWindow() {
        initMultiWindow();
        updateMultiWindow();
        multiTabPopupWindow.showAtLocation(findViewById(R.id.root), Gravity.BOTTOM, 0, 0);
    }

    private static class MultiWindowAdapter extends BaseAdapter {

        private ArrayList<TabGroup> mList;

        private MainActivity mainActivity;

        public void update(MainActivity activity) {
            mainActivity = activity;
            mList = mainActivity.manager.getList();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (mList != null) {
                return mList.size();
            }
            return 0;
        }

        @Override
        public TabGroup getItem(int position) {
            if (mList != null) {
                return mList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (getCount() == 0) {
                return null;
            }

            Holder holder = null;
            if (convertView == null) {
                convertView = mainActivity.getLayoutInflater().inflate(R.layout.multiwindow_item, parent, false);
                holder = new Holder();
                holder.close = (ImageView) convertView.findViewById(R.id.close_tab);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            }

            if (holder == null) {
                holder = (Holder) convertView.getTag();
            }

            TabGroup tabGroup = getItem(position);
            if (tabGroup != null) {
                holder.title.setText(tabGroup.getCurrentTab().getTitle());
                Drawable icon = tabGroup.getCurrentTab().getIconDrawable();
                if (icon != null) {
                    holder.icon.setBackground(icon);
                }
                holder.close.setOnClickListener(mainActivity);
            }
            return convertView;
        }
    }

    private static class Holder {
        ImageView icon;
        TextView title;
        ImageView close;
    }

}
