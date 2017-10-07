package peter.util.searcher.tab

import android.graphics.drawable.Drawable
import android.view.View

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.bean.Bean

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

abstract class SearcherTab(val mainActivity: MainActivity) : Tab {

    var iconDrawable: Drawable? = null

    abstract fun getView(): View?

    open fun onCreateViewResId(): Int = 0

}
