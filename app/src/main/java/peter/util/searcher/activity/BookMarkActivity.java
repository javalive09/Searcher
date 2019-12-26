package peter.util.searcher.activity;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.umeng.analytics.MobclickAgent;
import peter.util.searcher.R;
import peter.util.searcher.databinding.ActivityBookmarkBinding;
import peter.util.searcher.fragment.BookmarkFragment;
import peter.util.searcher.fragment.FavoriteFragment;
import peter.util.searcher.fragment.HistorySearchFragment;

/**
 * 书签activity
 * Created by peter on 16/5/9.
 */
public class BookMarkActivity extends BaseActivity {

    ActivityBookmarkBinding binding;

    final BookmarkFragment[] bookmarkFragments = new BookmarkFragment[]{new FavoriteFragment(), new HistorySearchFragment()};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
        binding.viewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return bookmarkFragments[position];
            }

            @Override
            public int getCount() {
                return bookmarkFragments.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        binding.slidingTabs.setupWithViewPager(binding.viewpager);
    }

    @Override
    public void onBackPressed() {
        if (!bookmarkFragments[binding.viewpager.getCurrentItem()].needCloseSearchView()) {
            super.onBackPressed();
        }
    }


}
