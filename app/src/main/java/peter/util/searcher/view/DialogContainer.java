package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by peter on 2016/12/4.
 */

public class DialogContainer extends RelativeLayout {

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

}
