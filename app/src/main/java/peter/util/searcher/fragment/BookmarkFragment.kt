package peter.util.searcher.fragment

import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView

/**
 * 书签fragment父类
 *
 *
 * Created by peter on 2017/9/28.
 */

abstract class BookmarkFragment : Fragment() {

    protected var mSearchView: SearchView? = null

    fun needCloseSearchView(): Boolean {
        if (mSearchView != null && !mSearchView!!.isIconified) {//open
            mSearchView!!.isIconified = true
            return true
        }
        return false
    }

}
