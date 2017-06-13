package peter.util.searcher.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.fragment.FavoriteFragment;
import peter.util.searcher.fragment.HistoryDownloadFragment;
import peter.util.searcher.fragment.HistorySearchFragment;

/**
 * 书签activity
 * Created by peter on 16/5/9.
 */
public class BookMarkActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mSlidingTabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        ButterKnife.bind(this);
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
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    /**
     * hide menu
     *
     * @param menu menu
     * @return result
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void initFragment() {
        final String[] title = getResources().getStringArray(R.array.bookmarks);
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new FavoriteFragment();
                    case 1:
                        return new HistorySearchFragment();
                    case 2:
                        return new HistoryDownloadFragment();
                }
                return null;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mSlidingTabLayout.setupWithViewPager(viewPager);

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

}
