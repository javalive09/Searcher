package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.GridView;
import android.widget.ListView;

/**
 * Created by peter on 2016/11/27.
 */

public class MenuWindowGridView extends GridView {

    private OutSideTouchItemCallBack outSideTouchItemCallBack;

    public MenuWindowGridView(Context context) {
        super(context);
    }

    public MenuWindowGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuWindowGridView(Context context, AttributeSet attrs, int defStyleAttr) {
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
