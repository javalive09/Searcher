package peter.util.searcher.adapter

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

import peter.util.searcher.R
import peter.util.searcher.activity.MainActivity
import peter.util.searcher.tab.TabGroup

class MultiWindowAdapter : BaseAdapter() {

    private var mList: ArrayList<TabGroup> = ArrayList()
    private var mainActivity: MainActivity? = null

    fun update(activity: MainActivity) {
        mainActivity = activity
        mList = mainActivity!!.tabManager!!.list
        notifyDataSetChanged()
    }

    override fun getCount(): Int =  mList.size

    override fun getItem(position: Int): TabGroup? = mList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var content = convertView
        if (content == null) {
            content = mainActivity!!.layoutInflater.inflate(R.layout.multiwindow_item, parent, false)
        }

        val tabGroup = getItem(position)

        content!!.isActivated = mainActivity!!.tabManager!!.currentTabGroup() === tabGroup

        val title: TextView = content.findViewOften(R.id.title)
        title.text = tabGroup?.currentTab()?.getTitle()
        val icon: ImageView = content.findViewOften(R.id.icon)
        if (tabGroup?.currentTab()?.iconDrawable != null) {
            icon.background = tabGroup.currentTab()!!.iconDrawable
        } else {
            icon.setBackgroundResource(R.drawable.ic_website)
        }
        val close: ImageView = content.findViewOften(R.id.close_tab)
        close.tag = tabGroup
        close.setOnClickListener(mainActivity)
        content.setTag(R.id.multi_window_item_tag, tabGroup)
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