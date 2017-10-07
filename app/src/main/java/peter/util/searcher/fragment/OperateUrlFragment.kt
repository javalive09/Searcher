package peter.util.searcher.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import peter.util.searcher.R
import peter.util.searcher.activity.SearchActivity
import peter.util.searcher.bean.Bean
import peter.util.searcher.utils.UrlUtils
import kotlinx.android.synthetic.main.fragment_operate_url.*


/**
 * url操作fragment
 * Created by peter on 2016/11/24.
 */

class OperateUrlFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_operate_url, container, false)
        enter!!.setOnClickListener({
            val searchActivity = activity as SearchActivity
            searchActivity.closeIME()
            searchActivity.finish()
            searchActivity.overridePendingTransition(0, 0)
            searchActivity.startBrowser(Bean("", UrlUtils.getGuessUrl(searchActivity.getSearchWord())))

        })
        return rootView
    }

}
