package peter.util.searcher.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.fragment.BookmarkFragment;
import peter.util.searcher.fragment.FavoriteFragment;
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
    final BookmarkFragment[] bookmarkFragments = new BookmarkFragment[]{new FavoriteFragment(), new HistorySearchFragment()};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // this sets the button to the back icon
            actionBar.setHomeButtonEnabled(true);
        }
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
     * hide bookmark_favorite
     *
     * @param menu bookmark_favorite
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
                return bookmarkFragments.length;
            }

            @Override
            public BookmarkFragment getItem(int position) {
                return bookmarkFragments[position];
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mSlidingTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        if (!bookmarkFragments[viewPager.getCurrentItem()].needCloseSearchView()) {
            super.onBackPressed();
        }
    }


}
