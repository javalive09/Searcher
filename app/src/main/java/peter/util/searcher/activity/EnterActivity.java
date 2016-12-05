package peter.util.searcher.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.fragment.BaseFragment;
import peter.util.searcher.fragment.EnterFragment;
import peter.util.searcher.fragment.WebViewFragment;
import peter.util.searcher.update.AsynWindowHandler;
import peter.util.searcher.fragment.EngineViewPagerFragment;
import peter.util.searcher.R;
import peter.util.searcher.fragment.RecentSearchFragment;
import peter.util.searcher.update.UpdateController;
import peter.util.searcher.utils.UrlUtils;

/**
 * Created by peter on 16/5/19.
 */
public class EnterActivity extends BaseActivity implements DrawerLayoutAdapter.OnItemClickListener, View.OnClickListener {

    private EditText search;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public static final String WEBVIEW = "webview";
    public static final String ENTER = "enter";
    private String currentFragmentTag = "";
    private static final String WEATHER_URL = "http://e.weather.com.cn/d/index/101010100.shtml";
    private static final String HISTORY_TODAY_URL = "http://wap.lssdjt.com/";
    private static final String NEWS_URL = "http://3g.163.com/touch/news";
    private static final String NAV_URL = "http://3g.hao123.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        init();
        checkIntentData(getIntent());
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntentData(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentFragmentTag.equals(WEBVIEW)) {
            FragmentManager fragmentManager = getFragmentManager();
            WebViewFragment fragment = (WebViewFragment) fragmentManager.findFragmentByTag(WEBVIEW);
            String url = fragment.getUrl();
            String searchWord = getSearchWord();
            outState.putString(NAME_URL, url);
            if (!TextUtils.isEmpty(searchWord)) {
                outState.putString(NAME_WORD, searchWord);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String url = savedInstanceState.getString(NAME_URL);
        if (!TextUtils.isEmpty(url)) {
            String searchWord = savedInstanceState.getString(NAME_WORD);
            setSearchWord(searchWord);
            startFragment(WEBVIEW, savedInstanceState);
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void setSearchWord(String word) {
        search.setText(word);
    }

    @Override
    public String getSearchWord() {
        return search.getText().toString().trim();
    }

    private void init() {
        search = (EditText) findViewById(R.id.search);
        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(EnterActivity.this, SearchActivity.class);
                    intent.putExtra(NAME_WORD, getSearchWord());
                    startActivity(intent);
                }
                return true;
            }
        });
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        RecyclerView mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        mDrawerList.setAdapter(new DrawerLayoutAdapter(getData(), this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                EnterActivity.this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                findViewById(R.id.title).setVisibility(View.GONE);
                search.setVisibility(View.VISIBLE);
                search.requestFocus();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }

            public void onDrawerOpened(View drawerView) {
                findViewById(R.id.title).setVisibility(View.VISIBLE);
                search.setVisibility(View.GONE);
                hideBoard();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(EnterActivity.this));
            }
        }, 200);

    }

    private void switchWebViewFrag(String url, String searchWord) {
        Bundle bundle = new Bundle();
        bundle.putString(BaseActivity.NAME_URL, url);
        bundle.putString(BaseActivity.NAME_WORD, searchWord);
        startFragment(WEBVIEW, bundle);
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_INNER_BROWSE.equals(action)) { // inner invoke
                String url = (String) intent.getSerializableExtra(NAME_URL);
                if (!TextUtils.isEmpty(url)) {
                    String searchWord = intent.getStringExtra(NAME_WORD);
                    switchWebViewFrag(url, searchWord);
                    setSearchWord(searchWord);
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    switchWebViewFrag(url, "");
                }
            } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                switchWebViewFrag(url, searchWord);
                setSearchWord(searchWord);
            } else {
                startFragment(ENTER, null);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentFragmentTag.equals(WEBVIEW)) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fragmentManager = getFragmentManager();
                    BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(WEBVIEW);
                    if (fragment.canGoBack()) {
                        fragment.GoBack();
                        return true;
                    } else if (currentFragmentTag.equals(WEBVIEW)) {
                        startFragment(ENTER, null);
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void startFragment(String tag, Bundle args) {
        currentFragmentTag = tag;
        Fragment fragment = null;
        if (tag.equals(WEBVIEW)) {
            fragment = new WebViewFragment();
        } else if (tag.equals(ENTER)) {
            fragment = new EnterFragment();
        }
        if (fragment != null) {
            if (args != null) {
                fragment.setArguments(args);
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<DrawerLayoutAdapter.TypeBean> getData() {
        ArrayList<DrawerLayoutAdapter.TypeBean> list = new ArrayList<>();

        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.VERSION));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_history_today, getString(R.string.history_today_title), HISTORY_TODAY_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.news_163, getString(R.string.news_163), NEWS_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_id_week_weather, getString(R.string.weeks_weather), WEATHER_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.nav, getString(R.string.web_guide), NAV_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_favorite, getString(R.string.action_collection), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_history, getString(R.string.action_history), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_url_history, getString(R.string.action_url_history), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_setting, getString(R.string.setting_title), ""));
        return list;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view, DrawerLayoutAdapter.TypeBean bean) {
        switch (bean.type) {
            case DrawerLayoutAdapter.CUSTOM:
                switch (bean.id) {
                    case R.id.hot_list_favorite:
                        startActivity(new Intent(EnterActivity.this, FavoriteActivity.class));
                        break;
                    case R.id.hot_list_history:
                        startActivity(new Intent(EnterActivity.this, HistoryActivity.class));
                        break;
                    case R.id.hot_list_url_history:
                        startActivity(new Intent(EnterActivity.this, HistoryURLActivity.class));
                        break;
                    case R.id.hot_list_setting:
                        startActivity(new Intent(EnterActivity.this, SettingActivity.class));
                        break;
                }
                break;
            case DrawerLayoutAdapter.HOT_LIST:
                switch (bean.id) {
                    case R.id.news_163:
                        startBrowser(bean.url, "");
                        break;
                    case R.id.hot_list_history_today:
                        startBrowser(bean.url, "");
                        break;
                    case R.id.hot_list_id_week_weather:
                        startBrowser(bean.url, "");
                        break;
                    case R.id.nav:
                        startBrowser(bean.url, "");
                        break;
                }
                break;
            case DrawerLayoutAdapter.VERSION:
                break;
        }
    }

//    private void openBoard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
//    }

    private void hideBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0); //强制隐藏键盘
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.opt:

//                switch (opt.getDrawable().getLevel()) {
//                    case 0:
//
//                        break;
//                    case 1:
//                        search.requestFocus();
//                        search.setText("");
//                        openBoard();
//                        break;
//                }
                break;
            case R.id.opt_multi:
                break;
        }
    }

}
