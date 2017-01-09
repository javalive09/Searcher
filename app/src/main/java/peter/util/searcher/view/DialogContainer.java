package peter.util.searcher.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by peter on 2016/12/4.
 */

public class DialogContainer extends RelativeLayout {

    private static final int DURATION = 300;

    private OutSideTouchItemCallBack outSideTouchItemCallBack;

    public DialogContainer(Context context) {
        super(context);
    }

    public DialogContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DialogContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (outSideTouchItemCallBack != null) {
                outSideTouchItemCallBack.outside();
                Log.i("peter", "--");
            }
        }
        return super.onTouchEvent(event);
    }

    public void setOutSideTouchItemCallBack(OutSideTouchItemCallBack callBack) {
        outSideTouchItemCallBack = callBack;
    }

    public interface OutSideTouchItemCallBack {
        void outside();
    }

    private ObjectAnimator animatorBg;
    private ObjectAnimator animatorContent;

    public void show() {
        if(getVisibility() == INVISIBLE) {
            setVisibility(VISIBLE);
            View bg = getChildAt(0);
            if (bg != null) {
                if(animatorBg != null) {
                    animatorBg.cancel();
                }
                animatorBg = ObjectAnimator.ofFloat(bg, "alpha", 0, 1).setDuration(DURATION);
                animatorBg.start();
            }

            View content = getChildAt(1);
            if (content != null) {
                if(animatorContent != null) {
                    animatorContent.cancel();
                }
                animatorContent = ObjectAnimator.ofFloat(content, "translationY", getHeight(), 0).setDuration(DURATION);
                animatorContent.start();
            }
        }
    }

    public void hide() {
        if (getVisibility() == VISIBLE) {
            View bg = getChildAt(0);
            if (bg != null) {
                if(animatorBg != null) {
                    animatorBg.cancel();
                }
                animatorBg = ObjectAnimator.ofFloat(bg, "alpha", 1, 0).setDuration(DURATION);
                animatorBg.start();
            }

            View content = getChildAt(1);
            if (content != null) {
                if(animatorContent != null) {
                    animatorContent.cancel();
                }
                animatorContent = ObjectAnimator.ofFloat(content, "translationY", 0, getHeight()).setDuration(DURATION);
                animatorContent.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setVisibility(INVISIBLE);
                    }
                });
                animatorContent.start();
            }
        }
    }

}
