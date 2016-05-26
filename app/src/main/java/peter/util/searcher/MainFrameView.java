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
    private WebView webView;
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

    float startX, startY;
    private boolean lockBar = false;
    boolean lockScroll = false;
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
                break;

            case MotionEvent.ACTION_UP:
                lockScroll = false;
                final int scrollY = getScrollY();
                int deltaX = (int) (startX - x);
                int deltaY = (int) (startY - y);
                if (scrollY == 0 && (deltaY > titleH / 2) && (deltaY > Math.abs(deltaX))) {//finger up
                    startBounceAnim(getScrollY(), titleH, 600);
                    if((int)(webView.getContentHeight()*webView.getScale()) == (webView.getHeight() + webView.getScrollY())) {
                        lockScroll = true;
                    }
                } else if (scrollY == titleH && (deltaY < -titleH / 2) && (Math.abs(deltaY) > Math.abs(deltaX))) {//finger down
                    startBounceAnim(getScrollY(), -getScrollY(), 600);
                    if(webView.getScrollY() == 0){
                        lockScroll = true;
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

        webView = (WebView) getChildAt(1);
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

        if(!lockScroll) {
            webView.scrollBy(0, oldt - t);
            webView.setPadding(0, t, 0, 0);
        }

        Log.i("peter", "autoScroll===" + (oldt - t));
        Log.i("peter", "l = " + l + "; t=" + t + "; oldl =" + oldl + "; oldt =" + oldt);
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
