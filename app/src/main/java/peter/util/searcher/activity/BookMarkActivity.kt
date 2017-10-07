package peter.util.searcher.activity

import android.os.Bundle
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.Menu

import com.umeng.analytics.MobclickAgent

import peter.util.searcher.R
import peter.util.searcher.fragment.BookmarkFragment
import peter.util.searcher.fragment.FavoriteFragment
import peter.util.searcher.fragment.HistorySearchFragment
import kotlinx.android.synthetic.main.activity_bookmark.*


/**
 * 书签activity
 * Created by peter on 16/5/9.
 */
class BookMarkActivity : BaseActivity() {

    var bookmarkFragments = arrayOf<BookmarkFragment>(FavoriteFragment(), HistorySearchFragment())

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        setSupportActionBar(toolbar)
        toolbar!!.setNavigationOnClickListener { _ -> onBackPressed() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // this sets the button to the back icon
        supportActionBar?.setHomeButtonEnabled(true)
        initFragment()
    }

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    /**
     * hide bookmark menu
     *
     * @param menu bookmark menu
     * @return result
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean = false

    private fun initFragment() {
        viewpager!!.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getCount(): Int = bookmarkFragments.size

            override fun getItem(position: Int): BookmarkFragment = bookmarkFragments[position]

            override fun getPageTitle(position: Int): CharSequence = resources.getStringArray(R.array.bookmarks)[position]
        }
        sliding_tabs!!.setupWithViewPager(viewpager)
    }

    override fun onBackPressed() {
        if (!bookmarkFragments[viewpager!!.currentItem].needCloseSearchView()) {
            super.onBackPressed()
        }
    }


}
