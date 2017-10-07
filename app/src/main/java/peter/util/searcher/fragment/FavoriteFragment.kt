package peter.util.searcher.fragment

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import peter.util.searcher.R
import peter.util.searcher.activity.BaseActivity
import peter.util.searcher.bean.Bean
import kotlinx.android.synthetic.main.fragment_favorite.*
import peter.util.searcher.db.DaoManager


/**
 * 收藏夹fragment
 * Created by peter on 16/5/9.
 */
class FavoriteFragment : BookmarkFragment(), View.OnClickListener, View.OnLongClickListener {

    private var popup: PopupMenu? = null
    var queryFavorite: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.bookmark_favorite, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        mSearchView = MenuItemCompat.getActionView(searchItem) as SearchView
        mSearchView!!.queryHint = getString(R.string.action_bookmark_search_favorite_hint)
        mSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean = false

            override fun onQueryTextChange(s: String): Boolean {
                if (TextUtils.isEmpty(s)) {
                    refreshAllListData()
                } else {
                    val listObservable = DaoManager.getInstance().queryFavoriteLike(s)
                    cancelQuery()
                    queryFavorite = listObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { list -> refreshListData(list) }
                }
                return true
            }
        })
        val mSearchAutoComplete = mSearchView!!.findViewById(R.id.search_src_text) as SearchView.SearchAutoComplete
        mSearchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelOffset(R.dimen.search_text_size).toFloat())
        mSearchAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                needCloseSearchView()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_favorite, container, false)
        refreshAllListData()
        return rootView
    }

    private fun refreshAllListData() {
        cancelQuery()
        queryFavorite = DaoManager.getInstance().queryAllFavorite().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { list ->
            val urls = resources.getStringArray(R.array.favorite_urls)
            val names = resources.getStringArray(R.array.favorite_urls_names)
            val beans: MutableList<Bean> = mutableListOf()
            for (i in urls.indices) {
                val bean = Bean()
                bean.name = names!![i]
                bean.url = urls[i]
                bean.time = -1
                beans.add(bean)
            }
            beans.addAll(list)
            refreshListData(beans)
        }
    }

    private fun refreshListData(beans: List<Bean>?) {
        if (beans != null) {
            if (beans.isEmpty()) {
                no_record!!.visibility = View.VISIBLE
            } else {
                no_record!!.visibility = View.GONE
            }
            if (favorite!!.adapter == null) {
                favorite!!.adapter = FavoriteAdapter(beans)
            } else {
                (favorite!!.adapter as FavoriteAdapter).updateData(beans)
            }
        }
        loading!!.visibility = View.GONE
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.item -> {
                val bean = v.tag as Bean
                (activity as BaseActivity).startBrowser(bean)
            }
        }
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


    override fun onDestroy() {
        dismissPopupMenu()
        cancelQuery()
        super.onDestroy()
    }

    private fun cancelQuery() {
        if (queryFavorite != null) {
            if (!queryFavorite!!.isDisposed) {
                queryFavorite!!.dispose()
            }
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
                    DaoManager.getInstance().deleteFav(bean)
                    refreshAllListData()
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

    private inner class FavoriteAdapter internal constructor(private var list: List<Bean>?) : BaseAdapter() {

        internal fun updateData(list: List<Bean>) {
            this.list = list
            notifyDataSetChanged()
        }

        override fun getCount(): Int = list!!.size

        override fun getItem(position: Int): Bean = list!![position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view: TextView? = convertView as? TextView
            if (view == null) view = LayoutInflater.from(activity).inflate(R.layout.item_list_website, parent, false) as TextView
            val bean = getItem(position)
            view.text = bean.name
            view.setOnClickListener(this@FavoriteFragment)
            view.tag = bean
            if (resources.getStringArray(R.array.favorite_urls).any { it == bean.url }) {
                view.setOnLongClickListener(null)
            } else {
                view.setOnLongClickListener(this@FavoriteFragment)
            }
            return view
        }
    }

}
