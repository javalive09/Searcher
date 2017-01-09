package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 *
 * Created by peter on 2017/1/6.
 */

public class WebViewContainer extends FrameLayout{

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
        removeAllViews();
        addView(view);
    }

    public View setCurrentView(int viewId) {
        removeAllViews();
        LayoutInflater factory = LayoutInflater.from(getContext());
        View mView = factory.inflate(viewId, this, false);
        addView(mView);
        return mView;
    }

}
