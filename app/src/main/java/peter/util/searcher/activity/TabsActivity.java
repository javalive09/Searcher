package peter.util.searcher.activity;


import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.adapter.ItemTouchHelperCallback;
import peter.util.searcher.adapter.RecyclerViewAdapter;
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
    RecyclerViewAdapter adapter;

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
        toolbar.setNavigationOnClickListener(v -> finish());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TabsActivity.this);
        list.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerViewAdapter(TabsActivity.this);
        list.setAdapter(adapter);

        refreshAdapter();

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(list);

//            Debug.stopMethodTracing();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabs, menu);
        return true;
    }

    private void refreshAdapter() {
//        DaoManager.getInstance().restoreAllTabs();
        ArrayList<TabGroup> tabGroups = TabGroupManager.getInstance().getList();
        Log.i("tabGroups", tabGroups.toString());
        adapter.setItems(tabGroups);
        list.scrollToPosition(TabGroupManager.getInstance().getCurrentTabIndex());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                TabData tabData = new TabData();
                tabData.setUrl(Tab.URL_HOME);
                TabGroupManager.getInstance().load(tabData, true);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
