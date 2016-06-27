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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by peter on 16/5/19.
 */
public class EnterActivity extends BaseActivity implements DrawerLayoutAdapter.OnItemClickListener, View.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private long mExitTime = 0;

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

    private void init() {
        View searchInput = findViewById(R.id.search);
        if (searchInput != null) {
            searchInput.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        startSearch(isNewTab(getIntent()));
                        return true;
                    }
                    return false;
                }
            });
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
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
                findViewById(R.id.search).setVisibility(View.VISIBLE);
                findViewById(R.id.search).requestFocus();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }

            public void onDrawerOpened(View drawerView) {
                findViewById(R.id.title).setVisibility(View.VISIBLE);
                findViewById(R.id.search).setVisibility(View.GONE);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(EnterActivity.this));
            }
        }, 200);
        setWebSiteFragment();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isLaunchTab(getIntent())) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {//
                    Toast.makeText(this, R.string.exit_hint, Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();// 更新mExitTime
                } else {
                    SearcherWebViewManager.instance().shutdown();
                    finish();
                }
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

    private void setWebSiteFragment() {
        Fragment fragment = new CommonWebSiteFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
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
                        startFavAct();
                        break;
                    case R.id.hot_list_history:
                        startHistoryAct();
                        break;
                    case R.id.hot_list_url_history:
                        startHistorUrlyAct();
                        break;
                    case R.id.hot_list_setting:
                        startActivity(new Intent(EnterActivity.this, SettingActivity.class));
                        break;
                }
                break;
            case DrawerLayoutAdapter.HOT_LIST:
                String url = UrlUtils.smartUrlFilter(bean.url, true, bean.url);
                startBrowser(EnterActivity.this, url, bean.content);
                break;
            case DrawerLayoutAdapter.VERSION:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multi_window:
                int count = SearcherWebViewManager.instance().getWebViewCount();
                if (count > 0) {
                    startActivity(new Intent(EnterActivity.this, MultiWindowActivity.class));
                } else {
                    Toast.makeText(EnterActivity.this, R.string.action_about, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
