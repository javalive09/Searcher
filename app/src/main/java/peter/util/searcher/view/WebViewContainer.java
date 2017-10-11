package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
        removeViewAt(0);
        addView(view, 0);
    }

    public View setCurrentView(int viewId) {
        removeViewAt(0);
        LayoutInflater factory = LayoutInflater.from(getContext());
        View mView = factory.inflate(viewId, this, false);
        addView(mView, 0);
        return mView;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + Constants.getActionBarH(getContext()));
    }

}