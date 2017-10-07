package peter.util.searcher.fragment

import android.app.Fragment
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.text.TextUtils
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import peter.util.searcher.R
import peter.util.searcher.activity.BaseActivity
import peter.util.searcher.bean.Bean
import peter.util.searcher.bean.EnginesItem
import peter.util.searcher.bean.ItemItem
import peter.util.searcher.net.CommonRetrofit
import peter.util.searcher.net.IEngineService
import peter.util.searcher.utils.UrlUtils
import kotlinx.android.synthetic.main.fragment_engine_viewpager.*


/**
 * 引擎列表fragment
 * Created by peter on 16/5/9.
 */
class EngineInfoViewPagerFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_engine_viewpager, container, false)
        init()
        return rootView
    }

    private fun init() {
        val iEngineService = CommonRetrofit.instance.getRetrofit()!!.create(IEngineService::class.java)
        val engineInfoObservable = iEngineService.info
        engineInfoObservable.subscribeOn(Schedulers.io())
                .retry(5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ engineInfo ->
                    loading!!.visibility = View.GONE
                    val adapter = EnginesAdapter(this@EngineInfoViewPagerFragment, engineInfo.engines!!)
                    viewpager!!.adapter = adapter
                    sliding_tabs!!.setupWithViewPager(viewpager)
                    viewpager!!.currentItem = arguments.getParcelable<Bean>(BaseActivity.NAME_BEAN).pageNo
                }) { throwable -> Toast.makeText(activity, throwable.message, Toast.LENGTH_LONG).show() }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.engine_item -> {
                val act = activity as BaseActivity
                val searchWord = act.getSearchWord()
                if (!TextUtils.isEmpty(searchWord)) {
                    val engine = v.getTag(R.id.grid_view_item) as ItemItem
                    val url = UrlUtils.smartUrlFilter(searchWord!!, true, engine.url)
                    act.finish()
                    act.overridePendingTransition(0, 0)
                    val bean = Bean(searchWord, url)
                    bean.pageNo = viewpager!!.currentItem
                    act.startBrowser(bean)
                }
            }
            else -> {
            }
        }
    }

    private class EnginesAdapter internal constructor(internal val f: EngineInfoViewPagerFragment, internal val list: List<EnginesItem>) : PagerAdapter() {

        override fun getPageTitle(position: Int): CharSequence {
            return list[position].title!!
        }


        override fun getCount(): Int {
            return list.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = f.activity.layoutInflater.inflate(R.layout.fragment_engine_grid,
                    container, false)
            val gv = view as GridView
            val engines = list[position]
            gv.adapter = EngineAdapter(f, position, engines.item!!)
            container.addView(view)
            return view
        }

        override fun isViewFromObject(view: View, o: Any): Boolean {
            return o === view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

    private class EngineAdapter internal constructor(internal val f: EngineInfoViewPagerFragment, internal val pageNo: Int, internal val list: List<ItemItem>) : BaseAdapter() {

        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): ItemItem {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var content = convertView
            if (content == null) {
                content = f.activity.layoutInflater.inflate(R.layout.fragment_engine_grid_item, parent, false)
            }
            val title: TextView = content!!.findViewOften(R.id.title)
            val icon: ImageView = content.findViewOften(R.id.icon)
            val engine = getItem(position)
            engine.pageNo = pageNo
            Glide.with(f)
                    .load(engine.icon)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(icon)
            title.text = engine.name
            content.setOnClickListener(f)
            content.setTag(R.id.grid_view_item, engine)
            return content
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T : View> View.findViewOften(viewId: Int): T {
            val viewHolder: SparseArray<View> = tag as? SparseArray<View> ?: SparseArray()
            tag = viewHolder
            var childView: View? = viewHolder.get(viewId)
            if (null == childView) {
                childView = findViewById(viewId)
                viewHolder.put(viewId, childView)
            }
            return childView as T
        }
    }


}
