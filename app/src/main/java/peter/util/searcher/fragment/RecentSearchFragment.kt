package peter.util.searcher.fragment

import android.app.Fragment
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import peter.util.searcher.R
import peter.util.searcher.activity.SearchActivity
import peter.util.searcher.bean.Bean
import peter.util.searcher.utils.UrlUtils
import kotlinx.android.synthetic.main.fragment_recent_search.*
import peter.util.searcher.db.DaoManager


/**
 * 最近搜索fragment
 * Created by peter on 16/5/9.
 */
class RecentSearchFragment : Fragment(), View.OnClickListener, View.OnLongClickListener {

    private var popup: PopupMenu? = null
    private var word: CharSequence? = null
    private var queryRecent: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_recent_search, container, false)

    override fun onResume() {
        super.onResume()
        refreshData()
        val cmb = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (cmb.primaryClip != null) {
            if (cmb.primaryClip.itemCount > 0) {
                word = cmb.primaryClip.getItemAt(0).text
                if (!TextUtils.isEmpty(word)) {
                    if (UrlUtils.guessUrl(word.toString())) {
                        paste_enter!!.visibility = View.VISIBLE
                        paste_enter!!.setOnClickListener(this@RecentSearchFragment)
                    } else {
                        paste!!.visibility = View.VISIBLE
                        paste!!.setOnClickListener(this@RecentSearchFragment)
                    }
                }
            }
        }
    }

    override fun onClick(v: View) {
        val searchActivity = activity as SearchActivity
        when (v.id) {
            R.id.item -> searchActivity.setSearchWord((v.tag as Bean).name!!)
            R.id.paste -> searchActivity.setSearchWord(word.toString())
            R.id.paste_enter -> {
                searchActivity.closeIME()
                searchActivity.finish()
                searchActivity.overridePendingTransition(0, 0)
                searchActivity.startBrowser(Bean("", word.toString()))
            }
        }
    }

    private fun refreshData() {
        queryRecent = DaoManager.getInstance().queryRecentData(9).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { beans ->
            loading!!.visibility = View.GONE
            if (beans != null) {
                if (beans.size > 0) {
                    recent_search!!.adapter = RecentSearchAdapter(beans)
                }
            }
        }
    }

    override fun onDestroy() {
        dismissPopupMenu()
        if (!queryRecent?.isDisposed!!) {
            queryRecent!!.dispose()
        }
        super.onDestroy()
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.item -> {
                popupMenu(v)
                return true
            }
        }
        return false
    }

    private inner class RecentSearchAdapter internal constructor(private val list: List<Bean>) : BaseAdapter() {

        override fun getCount(): Int = list.size

        override fun getItem(position: Int): Bean = list[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var content = convertView as TextView?
            if (content == null) {
                content = LayoutInflater.from(activity).inflate(R.layout.item_list_recentsearch, parent, false) as TextView?
            }
            val search = getItem(position)
            content!!.text = search.name
            content.setOnClickListener(this@RecentSearchFragment)
            content.setOnLongClickListener(this@RecentSearchFragment)
            content.tag = search
            return content
        }
    }

    private fun popupMenu(view: View) {
        dismissPopupMenu()
        popup = PopupMenu(activity, view)
        popup!!.menuInflater.inflate(R.menu.item, popup!!.menu)
        popup!!.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    val bean = view.tag as Bean
                    DaoManager.getInstance().deleteHistory(bean)
                    refreshData()
                }
            }
            true
        }
        popup!!.show()
    }

    private fun dismissPopupMenu() {
        if (popup != null) {
            popup!!.dismiss()
        }
    }

}
