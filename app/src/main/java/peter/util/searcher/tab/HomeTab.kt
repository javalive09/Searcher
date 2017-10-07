package peter.util.searcher.tab

import peter.util.searcher.R
import peter.util.searcher.activity.MainActivity

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

class HomeTab(activity: MainActivity) : LocalViewTab(activity) {

    override fun getUrl(): String? = Tab.URL_HOME

    override fun getSearchWord(): String? = ""

    override fun getPageNo(): Int? = 0

    override fun getTitle(): String? = mainActivity.getString(R.string.home)

    override fun getHost(): String? = ""

    override fun reload() {}

    override fun canGoBack(): Boolean = false

    override fun goBack() {}

    override fun canGoForward(): Boolean = false

    override fun goForward() {}

    override fun onCreate() {}

    override fun onPause() {}

    override fun onResume() {}

    override fun onDestroy() {}

    override fun onCreateViewResId(): Int = R.layout.tab_home

}
