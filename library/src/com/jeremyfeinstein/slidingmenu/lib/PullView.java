package com.jeremyfeinstein.slidingmenu.lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

public class PullView extends ViewGroup {

    private Scroller mScroller;
    public static int mTouchSlop;
    private static final int STATE_IDLE = 0;//空闲状态
    private static final int STATE_DRAGGING = 1;//拖拽状态
    private static final int STATE_SETTLING = 2;//还原状态
    private int mTouchState = STATE_IDLE;
    private static final int mAnimTime = 300;
    private static final int VELOCITY_BORDER = 2000;
    private boolean mFinish;
    private int mStartX;
    private int mStartY;
    private int mDeltaX;
    private Rect validRct;
    private VelocityTracker mVelocityTracker;

    public PullView(Context context) {
        super(context);
        init();
    }

    public PullView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext(), new BakedBezierInterpolator());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        initHintExitPlay();
    }

    private void startBounceAnim(int startX, int startY, int dx, int dy, int duration) {
        mScroller.startScroll(startX, startY, dx, dy, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int mScrollerX = mScroller.getCurrX();
            int mScrollerY = mScroller.getCurrY();
            scrollTo(mScrollerX, mScrollerY);
            invalidate();
        } else if (mFinish) {
            Activity act = (Activity) getContext();
            act.finish();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        final int currentX = (int) ev.getX();
        final int currentY = (int) ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartX = currentX;
                mStartY = currentY;
                mTouchState = mScroller.isFinished() ? STATE_IDLE : STATE_SETTLING;
                if (validRct == null) {
                    DisplayMetrics dm = new DisplayMetrics();
                    WindowManager manager = (WindowManager) getContext()
                            .getSystemService(Context.WINDOW_SERVICE);
                    manager.getDefaultDisplay().getMetrics(dm);
                    int bottom = dm.heightPixels;
                    int right = getResources().getDimensionPixelOffset(R.dimen.menu_w);
                    int top = getResources().getDimensionPixelSize(R.dimen.title_h);
                    validRct = new Rect(0, top, right, bottom);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (validRct.contains(mStartX, mStartY)) {
                    final int deltaX = Math.abs(currentX - mStartX);
                    if(deltaX > mTouchSlop) {
                        final int deltaY = Math.abs(currentY - mStartY);
                        if(deltaY < mTouchSlop && deltaX > deltaY) {
                            mStartX = currentX;
                            mTouchState = STATE_DRAGGING;
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = STATE_IDLE;
                break;
        }
        return mTouchState != STATE_IDLE;
    }

    public boolean onTouchEvent(MotionEvent event) {

        Log.i("peter", "mTouchState =" + mTouchState);
        final int action = event.getAction();
        final int currentX = (int) event.getX();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mDeltaX = mStartX - currentX;
                Log.i("peter", "deltaX = " + mDeltaX);

                if(mDeltaX < -getWidth() / 4) {
                    setAlpha(0.8f);
                }else {
                    setAlpha(1.0f);
                }

                if(mDeltaX < 0) {
                    scrollTo(mDeltaX, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchState = STATE_IDLE;
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                Log.i("peter", "velocityX = " + velocityX);
                Log.i("peter", "mDeltaX = " + mDeltaX);

                if (velocityX > VELOCITY_BORDER || mDeltaX < -getWidth() / 4) {
                    mFinish = true;
                    startBounceAnim(getScrollX(), getScrollY(), getScrollX() - getWidth(), 0, mAnimTime);
                } else {
                    mFinish = false;
                    startBounceAnim(getScrollX(), getScrollY(), - getScrollX(), 0, mAnimTime);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }

        return false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(0).layout(l, t, r, b);
    }

    public boolean played = false;

    private void initHintExitPlay() {
        played = getContext().getSharedPreferences("hint_exit_played", Context.MODE_PRIVATE).getBoolean("played", false);
    }

    public void startPlayExit() {
        if (!played) {
            played = true;
            getContext().getSharedPreferences("hint_exit_played", Context.MODE_PRIVATE).edit().putBoolean("played", true).commit();
            startBounceAnim(0, 0, mTouchSlop * 4, 0, 300);
        }
    }

    public void resetPlayExit() {
        mScroller.forceFinished(true);
        setScrollX(0);
    }

}