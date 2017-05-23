package peter.util.searcher.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import peter.util.searcher.R;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.fragment.FavoriteFragment;
import peter.util.searcher.fragment.HistoryDownloadFragment;
import peter.util.searcher.fragment.HistorySearchFragment;
import peter.util.searcher.fragment.SlidingTabLayout;

/**
 * Created by peter on 16/5/9.
 */
public class BookMarkActivity extends BaseActivity {

    private List<Fragment> fragmentContainter;
    private ViewPager viewPager;
    private SlidingTabLayout mSlidingTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // this sets the button to the back icon
        actionBar.setHomeButtonEnabled(true);
        initFragment();
        installFavUrl();
    }

    private void installFavUrl() {
        String[] urls = getResources().getStringArray(R.array.favorite_urls);
        String[] names = getResources().getStringArray(R.array.favorite_urls_names);
        final ArrayList<Bean> list = new ArrayList<>(urls.length);
        for (int i = 0; i < urls.length; i++) {
            Bean bean = new Bean();
            bean.name = names[i];
            bean.url = urls[i];
            bean.time = -1;
            list.add(bean);
        }
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SqliteHelper.instance(BookMarkActivity.this).insertFav(list);
                return null;
            }
        }.execute();

    }

    /**
     * hide menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void initFragment() {
        FavoriteFragment f1 = new FavoriteFragment();
        HistorySearchFragment f2 = new HistorySearchFragment();
        HistoryDownloadFragment f3 = new HistoryDownloadFragment();
        fragmentContainter = new ArrayList<>(3);
        fragmentContainter.add(f1);
        fragmentContainter.add(f2);
        fragmentContainter.add(f3);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        final String[] title = getResources().getStringArray(R.array.bookmarks);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.colorPrimary));
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return fragmentContainter.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return fragmentContainter.get(arg0);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mSlidingTabLayout.setViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
