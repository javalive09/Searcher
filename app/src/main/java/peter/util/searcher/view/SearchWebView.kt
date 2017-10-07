package peter.util.searcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.MenuItem
import android.view.MotionEvent
import android.webkit.WebView
import android.widget.PopupMenu

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.utils.Constants

/**
 * 自定义的webView
 * Created by peter on 2017/6/8.
 */

class SearchWebView : WebView {

    private var startY: Int = 0
    private var touchX: Int = 0
    private var touchY: Int = 0
    private var popMenu: Boolean = false
    private var slop: Int = 0

    constructor(context: Context) : super(context) {
        slop = Constants.getActionBarH(context) * 2
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        slop = Constants.getActionBarH(context) * 2
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        slop = Constants.getActionBarH(context) * 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight + Constants.getActionBarH(context))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = y
                touchX = x
                touchY = y
                popMenu = false
            }
            MotionEvent.ACTION_UP -> if (!popMenu) {
                val deltaY = y - startY
                if (deltaY > slop) { //show
                    (context as MainActivity).showTopBar()
                } else if (deltaY < -slop) {//hide
                    (context as MainActivity).hideTopBar()
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo {
        popMenu = true
        val info = ContextMenuInfo(hitTestResult, touchX, touchY)
        info.searchWebView = this
        return info
    }


    class ContextMenuInfo internal constructor(val result: WebView.HitTestResult, val x: Int, val y: Int) : ContextMenu.ContextMenuInfo {

        var searchWebView: SearchWebView? = null

    }

    open class OnMenuItemClickListener : PopupMenu.OnMenuItemClickListener {

        var searchWebView: SearchWebView? = null

        var info: WebView.HitTestResult? = null


        override fun onMenuItemClick(item: MenuItem): Boolean = false
    }

}
