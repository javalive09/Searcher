package peter.util.searcher.view;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import java.util.Map;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.utils.Constants;

/**
 * 自定义的webView
 * Created by peter on 2017/6/8.
 */

public class SearchWebView extends WebView {

    private int startY;
    private static int SLOP;

    public SearchWebView(Context context) {
        super(context);
        SLOP = Constants.getActionBarH(context) * 2;
    }

    public SearchWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SLOP = Constants.getActionBarH(context) * 2;
    }

    public SearchWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SLOP = Constants.getActionBarH(context) * 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + Constants.getActionBarH(getContext()));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = y;
                break;
            case MotionEvent.ACTION_UP:
                int deltaY = y - startY;
                if (deltaY > SLOP) { //show
                    ((MainActivity) getContext()).showTopbar();
                } else if (deltaY < -SLOP) {//hide
                    ((MainActivity) getContext()).hideTopbar();
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }
}
