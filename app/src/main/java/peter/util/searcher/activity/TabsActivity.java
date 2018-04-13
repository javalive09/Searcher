package peter.util.searcher.activity;


import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.adapter.ItemTouchHelperCallback;
import peter.util.searcher.adapter.TabsAdapter;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;

/**
 * Tab list
 * <p>
 * Created by peter on 2017/6/14.
 */

public class TabsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.top)
    AppBarLayout top;
    @BindView(R.id.list)
    RecyclerView list;
    @BindView(R.id.swipe_refresh_layout_recycler_view)
    SwipeRefreshLayout swipeRefreshLayout;
    TabsAdapter adapter;
    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //        Debug.startMethodTracing("loading");
        setContentView(R.layout.activity_tabs);
        ButterKnife.bind(TabsActivity.this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // this sets the button to the back icon
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TabsActivity.this);
        list.setLayoutManager(linearLayoutManager);

        adapter = new TabsAdapter(TabsActivity.this);
        list.setAdapter(adapter);

        refreshAdapter();

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(list);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);
//            Debug.stopMethodTracing();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabs, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint(getString(R.string.action_bookmark_search_tab_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    refreshAdapter();
                } else {
                    adapter.updateItems(queryLike(s));
                }
                return true;
            }
        });
        SearchView.SearchAutoComplete mSearchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.search_text_size));
        mSearchAutoComplete.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                needCloseSearchView();
            }
        });
        return true;
    }

    private boolean needCloseSearchView() {
        if (mSearchView != null && !mSearchView.isIconified()) {//open
            mSearchView.setIconified(true);
            return true;
        }
        return false;
    }

    private void refreshAdapter() {
        ArrayList<TabGroup> tabGroups = TabGroupManager.getInstance().getList();
        adapter.setItems(tabGroups);
        list.scrollToPosition(TabGroupManager.getInstance().getCurrentTabIndex());
    }

    private ArrayList<TabGroup> queryLike(String title) {
        ArrayList<TabGroup> tabGroups = TabGroupManager.getInstance().getList();
        ArrayList<TabGroup> currentList = new ArrayList<>();
        for (TabGroup tabGroup : tabGroups) {
            String tabGroupTitle = tabGroup.getTitle();
            if (tabGroupTitle.startsWith(title)) {
                currentList.add(tabGroup);
            }
        }
        return currentList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addTab();
                break;
            case R.id.action_close_all:
                TabGroupManager.getInstance().clear();
                saveAndExit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveAndExit() {
        DaoManager.getInstance().saveTabs();
        exit();
    }

    public void addTab() {
        TabData tabData = new TabData();
        tabData.setUrl(Tab.URL_HOME);
        TabGroupManager.getInstance().load(tabData, true);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!needCloseSearchView()) {
            super.onBackPressed();
        }
    }

}
