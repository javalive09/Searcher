package peter.util.searcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
        mTouchSlop =  ViewConfiguration.get(getContext()).getScaledTouchSlop() + 5;
        mScroller = new Scroller(getContext());
    }

    float startY;
    float currentY;
    int deltaY;
    private boolean lockBar = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(lockBar) {
            return super.dispatchTouchEvent(ev);
        }

        final float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = y;
                currentY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                deltaY = (int) (startY - y);

                Log.i("peter", "deltaY = " + deltaY);
                Log.i("peter", "titleH = " + titleH);
                Log.i("peter", "mTouchSlop = " + mTouchSlop);

                if (Math.abs(deltaY) > mTouchSlop) {//开始滑动
                    int scrollY = getScrollY();
                    Log.i("peter", "scrollY = " + scrollY);


                    if(deltaY > 0) {//finger up

                        int realDelatY = deltaY - mTouchSlop;
                        if(realDelatY > titleH) {
                            realDelatY = titleH;
                        }

                        if(getScrollY() == titleH) {
                            return super.dispatchTouchEvent(ev);
                        }

                        scrollTo(0, realDelatY);
                        return super.dispatchTouchEvent(ev);
                    }else if(deltaY < 0) {//finger down

                        int realDeltaY = titleH + deltaY + mTouchSlop;

                        if(realDeltaY < 0) {
                            realDeltaY = 0;
                        }

                        if(getScrollY() == 0) {
                            return super.dispatchTouchEvent(ev);
                        }

                        scrollTo(0, realDeltaY);
                        return super.dispatchTouchEvent(ev);
                    }

                }

                break;
            case MotionEvent.ACTION_UP:

                int scrollY = getScrollY();
                if(scrollY < 0) {
                    scrollTo(0, 0);
                    break;
                }else if(scrollY > titleH) {
                    scrollTo(0, titleH);
                    break;
                }else if(scrollY != 0 && scrollY != titleH){
                    if (deltaY > titleH / 2) {
                        startBounceAnim(getScrollY(), titleH - getScrollY(), 150);
                    } else {
                        startBounceAnim(getScrollY(), -getScrollY(), 150);
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

    public boolean isHideBar() {
        return getScrollY() == titleH;
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


}
