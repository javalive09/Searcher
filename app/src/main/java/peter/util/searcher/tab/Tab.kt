package peter.util.searcher.tab


import peter.util.searcher.bean.Bean

/**
 *
 * Created by peter on 2016/11/17.
 */

interface Tab {

    companion object {
        val LOCAL_SCHEMA = "local://"
        val URL_HOME = LOCAL_SCHEMA + "home"
        val ACTION_NEW_WINDOW = "new_window"
    }

    fun getUrl(): String?

    fun getSearchWord():String?

    fun getPageNo(): Int?

    fun getTitle():String?

    fun getHost():String?

    fun loadUrl(bean: Bean)

    fun reload()

    fun canGoBack(): Boolean

    fun goBack()

    fun canGoForward(): Boolean

    fun goForward()

    fun onCreate()

    fun onPause()

    fun onResume()

    fun onDestroy()

}
