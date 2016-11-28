package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Created by peter on 2016/11/27.
 */

public class MultiWindowListView extends ListView {

    private OutSideTouchItemCallBack outSideTouchItemCallBack;

    public MultiWindowListView(Context context) {
        super(context);
    }

    public MultiWindowListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiWindowListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOutSideTouchItemCallBack(OutSideTouchItemCallBack callBack) {
        outSideTouchItemCallBack = callBack;
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

    public interface OutSideTouchItemCallBack {
        void outside();
    }
}
