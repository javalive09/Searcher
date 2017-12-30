package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import peter.util.searcher.utils.Constants;

/**
 * webView 容器类
 * Created by peter on 2017/1/6.
 */

public class WebViewContainer extends FrameLayout {

    public WebViewContainer(Context context) {
        super(context);
    }

    public WebViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCurrentView(View view) {
        if (getChildAt(0) != view) {
            removeViewAt(0);
            ViewParent parent = view.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeAllViews();
            }
            addView(view, 0);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + Constants.getActionBarH(getContext()));
    }

}
