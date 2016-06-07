package peter.util.searcher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by peter on 16/5/19.
 */
public class SearchActivity extends AppCompatActivity implements DrawerLayoutAdapter.OnItemClickListener {

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private EditText search;
    private ImageView clear;
    private ActionBarDrawerToggle mDrawerToggle;
    static final String WEATHER_URL = "http://e.weather.com.cn/d/index/101010100.shtml";
    static final String HISTORY_TODAY_URL = "http://wap.lssdjt.com/";
    static final String NEWS_163 = "http://3g.163.com/touch/all?version=v_standard";
    static final String VIDEO_HUYA = "http://m.huya.com/index.php";
    static final String WEIBO = "http://m.weibo.cn/";
    static final String EAT_LIST = "http://m.xiachufang.com";
    static final String NAV_ENTER = "http://h5.mse.360.cn/navi.html";
    private static final int RECENT_SEARCH = 1;
    private static final int ENGINE_LIST = 2;
    private int currentFragment = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
    }

    public String getSearchWord() {
        return search.getText().toString().trim();
    }

    public void closeBoard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    private void init() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        clear = (ImageView) findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.requestFocus();
                search.setText("");
                openBoard();
            }
        });
        search = (EditText) findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchWord = getSearchWord();
                    if(!TextUtils.isEmpty(searchWord)) {
                        String engineUrl = getString(R.string.default_engine_url);
                        String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                        Utils.startSearchAct(SearchActivity.this, url, searchWord);
                        return true;
                    }
                }
                return false;
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            String temp;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (TextUtils.isEmpty(content)) {
                    setEngineFragment(RECENT_SEARCH);
                    clear.setVisibility(View.INVISIBLE);
                } else if (!content.equals(temp)) {
                    setEngineFragment(ENGINE_LIST);
                    clear.setVisibility(View.VISIBLE);
                }
            }
        });
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        mDrawerList.setAdapter(new DrawerLayoutAdapter(getData(), this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        final View title = toolbar.findViewById(R.id.title);
        mDrawerToggle = new ActionBarDrawerToggle(
                SearchActivity.this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                search.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }

            public void onDrawerOpened(View drawerView) {
                search.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                closeBoard();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setEngineFragment(RECENT_SEARCH);
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(SearchActivity.this));
            }
        },200);
    }

    private void setEngineFragment(int f) {
        if (currentFragment != f) {
            currentFragment = f;
            Fragment fragment = null;
            switch (f) {
                case RECENT_SEARCH:
                    fragment = new RecentSearchFragment();
                    break;
                case ENGINE_LIST:
                    fragment = new EngineViewPagerFragment();
                    break;
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private ArrayList<DrawerLayoutAdapter.TypeBean> getData() {
        ArrayList<DrawerLayoutAdapter.TypeBean> list = new ArrayList();
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.VERSION));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.news_163), NEWS_163));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.weibo), WEIBO));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.eat_list), EAT_LIST));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.video_huya), VIDEO_HUYA));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.weeks_weather), WEATHER_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.history_today_title), HISTORY_TODAY_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, 0, getString(R.string.web_guide), NAV_ENTER));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_favorite, getString(R.string.action_collection), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_history, getString(R.string.action_history), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_setting, getString(R.string.setting_title), ""));
        return list;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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
                        startActivity(new Intent(SearchActivity.this, FavoriteActivity.class));
                        break;
                    case R.id.hot_list_history:
                        startActivity(new Intent(SearchActivity.this, HistoryActivity.class));
                        break;
                    case R.id.hot_list_setting:
                        startActivity(new Intent(SearchActivity.this, SettingActivity.class));
                        break;
                }
                break;
            case DrawerLayoutAdapter.HOT_LIST:
                String url = UrlUtils.smartUrlFilter(bean.url, true, bean.url);
                Utils.startSearchAct(SearchActivity.this, url, bean.content);
                break;
            case DrawerLayoutAdapter.VERSION:
                break;
        }
    }
}
