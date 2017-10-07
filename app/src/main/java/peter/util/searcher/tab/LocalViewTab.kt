package peter.util.searcher.tab

import android.text.TextUtils
import android.view.View

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.bean.Bean

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

abstract class LocalViewTab internal constructor(activity: MainActivity) : SearcherTab(activity) {

    var localView: View? = null

    override fun loadUrl(bean: Bean) {
        if (!TextUtils.isEmpty(bean.url)) {
            if (localView == null) {
                val viewResId = onCreateViewResId()
                localView = mainActivity.setCurrentView(viewResId)
                onCreate()
            } else {
                mainActivity.setCurrentView(localView!!)
            }
        }
    }

    override fun getView(): View? = localView

}
