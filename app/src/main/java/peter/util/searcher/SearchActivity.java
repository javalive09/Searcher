package peter.util.searcher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by peter on 16/5/19.
 */
public class SearchActivity extends AppCompatActivity implements DrawerLayoutAdapter.OnItemClickListener{

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private EditText search;
    private ActionBarDrawerToggle mDrawerToggle;
    static final String WEATHER_URL = "http://e.weather.com.cn/d/index/101010100.shtml";
    static final String HISTORY_TODAY_URL = "http://wap.lssdjt.com/";
    static final String HOT_TOP_URL = "http://top.baidu.com/m/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    private void init() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        search = (EditText) findViewById(R.id.search);
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
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setFragment();
    }

    private void setFragment() {
        Fragment fragment = new EngineViewPagerFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    private ArrayList<DrawerLayoutAdapter.TypeBean> getData() {
        ArrayList<DrawerLayoutAdapter.TypeBean> list = new ArrayList();
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.VERSION));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_top, getString(R.string.hot_title), HOT_TOP_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_history_today, getString(R.string.history_today_title), HISTORY_TODAY_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_id_week_weather, getString(R.string.weeks_weather), WEATHER_URL));
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
    public void onClick(View view, int position) {

    }
}
