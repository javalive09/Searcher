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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
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
public class EnterActivity extends BaseActivity implements DrawerLayoutAdapter.OnItemClickListener ,View.OnClickListener{

    private EditText search;
    private ImageView clear;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    private static final String WEB_SITES = "web_sites";
    private static final String WEB_HINT = "web_hint";
    private static final String COMMON_ENTER = "common_enter";
    private String currentFragmentTag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
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

    public void setSearchWord(String word) {
        search.setText(word);
        if (!TextUtils.isEmpty(word)) {
            int position = word.length();
            search.setSelection(position);
        }
    }

    public String getSearchWord() {
        return search.getText().toString().trim();
    }

    private void init() {
        clear = (ImageView) findViewById(R.id.clear);
        search = (EditText) findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchWord = getSearchWord();
                    if (!TextUtils.isEmpty(searchWord)) {
                        String engineUrl = getString(R.string.default_engine_url);
                        String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                        startBrowser(url, searchWord);
                    }
                    search.requestFocus();
                    return true;
                }
                return false;
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
        setEngineFragment(RECENT_SEARCH);
    }

    public void setEngineFragment(String tag) {
        if (!currentFragmentTag.equals(tag)) {
            currentFragmentTag = tag;
            Fragment fragment = null;
            if (tag.equals(RECENT_SEARCH)) {
                fragment = new RecentSearchFragment();
            } else if (tag.equals(ENGINE_LIST)) {
                fragment = new EngineViewPagerFragment();
            } else if (tag.equals(WEB_SITES)) {
                fragment = new CommonWebSiteFragment();
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commit();
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
        ArrayList<DrawerLayoutAdapter.TypeBean> list = new ArrayList();
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.VERSION));
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
                break;
            case DrawerLayoutAdapter.VERSION:
                break;
        }
    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0); //强制隐藏键盘
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                search.requestFocus();
                search.setText("");
                openBoard();
                break;
        }
    }

}
