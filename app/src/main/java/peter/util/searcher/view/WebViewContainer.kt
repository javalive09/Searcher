package peter.util.searcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout

import peter.util.searcher.utils.Constants

/**
 * webView 容器类
 * Created by peter on 2017/1/6.
 */

class WebViewContainer : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setCurrentView(view: View) {
        removeViewAt(0)
        addView(view, 0)
    }

    fun setCurrentView(viewId: Int): View {
        removeViewAt(0)
        val factory = LayoutInflater.from(context)
        val mView = factory.inflate(viewId, this, false)
        addView(mView, 0)
        return mView
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight + Constants.getActionBarH(context))
    }

}
