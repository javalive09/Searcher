package peter.util.searcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by peter on 16/5/18.
 */
public class MainFrameView extends FrameLayout {

    private Scroller mScroller;
    int mTouchSlop;

    public MainFrameView(Context context) {
        super(context);
        init();
    }

    public MainFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop() + 5;
        mScroller = new Scroller(getContext());
    }

    int deltaY;
    float startX, startY;
    private boolean lockBar = false;
    boolean autoScroll = false;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (lockBar) {
            return super.dispatchTouchEvent(ev);
        }

        final float x = ev.getX();
        final float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                autoScroll = false;
                break;

            case MotionEvent.ACTION_MOVE:
                deltaY = (int) (startY - y);

                Log.i("peter", "deltaY = " + deltaY);
                Log.i("peter", "titleH = " + titleH);
                Log.i("peter", "mTouchSlop = " + mTouchSlop);

                if (Math.abs(deltaY) > mTouchSlop) {//开始滑动
                    int scrollY = getScrollY();
                    Log.i("peter", "scrollY = " + scrollY);


                    if (deltaY < 0) {//finger down

                        autoScroll = false;
                        int realDeltaY = titleH + deltaY + mTouchSlop;

                        if (realDeltaY < 0) {
                            realDeltaY = 0;
                        }

                        if (getScrollY() == 0) {
                            return super.dispatchTouchEvent(ev);
                        }

                        scrollTo(0, realDeltaY);
                        return super.dispatchTouchEvent(ev);
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                final int scrollY = getScrollY();
                int deltaX = (int) (startX - x);
                int deltaY = (int) (startY - y);
                autoScroll = true;
                if(scrollY == 0  && (deltaY > titleH / 2) && (deltaY > Math.abs(deltaX))) {//finger up
                    startBounceAnim(getScrollY(), titleH, 600);
                }else {
                    if (scrollY < 0) {
                        scrollTo(0, 0);
                        break;
                    } else if (scrollY != 0 && scrollY != titleH) {
                        startBounceAnim(getScrollY(), -getScrollY(), 600);
                    }
                }

                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int mScrollerY = mScroller.getCurrY();
            scrollTo(0, mScrollerY);
            Log.i("~peter", "mScrollerY=" + mScrollerY);
            invalidate();
        }
    }

    public void showBar() {
        scrollTo(0, 0);
        lockBar = false;
    }

    public void hideBar() {
        scrollTo(0, titleH);
        lockBar = true;
    }

    public void startBounceAnim(int startY, int dy, int duration) {
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    int titleH;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View title = getChildAt(0);
        int width = title.getMeasuredWidth();
        titleH = title.getMeasuredHeight();
        title.layout(left, top, width, titleH);

        View webView = getChildAt(1);
        webView.layout(left, titleH, width, getMeasuredHeight());

        View status = getChildAt(2);
        status.layout(left, titleH, width, titleH + status.getMeasuredHeight());

    }

    OnFrameScrollListener listener;

    public void setOnFrameScrollListener(OnFrameScrollListener listener) {
        this.listener = listener;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (listener != null) {
            listener.onScrollChanged(l, t, oldl, oldt);
        }

        if(autoScroll) {
            View webView = getChildAt(1);
            webView.scrollBy(0, oldt - t);
            Log.i("peter", "autoScroll===" + (oldt - t));

            Log.i("peter", "l = " + l + "; t=" + t + "; oldl =" + oldl + "; oldt =" + oldt);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int titleH = getChildAt(0).getMeasuredHeight();
        int high = getMeasuredHeight();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(high + titleH, MeasureSpec.UNSPECIFIED);

        //container
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), heightMeasureSpec);

        //webView
        getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface OnFrameScrollListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

}
