package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 *
 * Created by peter on 2016/12/2.
 */

public class WebViewContainer extends FrameLayout {

    private int mStartX;
    private int mDeltaX;
    private int mTouchSlop = 0;
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_LEFT = 1;
    private static final int TOUCH_STATE_RIGHT = 2;
    private int mTouchState = TOUCH_STATE_REST;
    private View Left,right;

    public WebViewContainer(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public WebViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int currentX = (int) ev.getX();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                mStartX = currentX;
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = mStartX - currentX;
                if(deltaX > mTouchSlop && mTouchState != TOUCH_STATE_RIGHT) {//向左滑动
                    mTouchState = TOUCH_STATE_LEFT;
                }

                if(deltaX < -mTouchSlop && mTouchState != TOUCH_STATE_LEFT) {//向右滑动
                    mTouchState = TOUCH_STATE_RIGHT;
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        boolean result = mTouchState != TOUCH_STATE_REST;
        return result;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int currentX = (int) event.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartX = currentX;
                break;

            case MotionEvent.ACTION_MOVE:
                mDeltaX = mStartX - currentX;
                switch (mTouchState) {
                    case TOUCH_STATE_LEFT:
                        setTranslationX(mDeltaX);
                        break;
                    case TOUCH_STATE_RIGHT:
                        break;
                }

                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        return false;
    }

}
