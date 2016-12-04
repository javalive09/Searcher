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

    public MultiWindowListView(Context context) {
        super(context);
    }

    public MultiWindowListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiWindowListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE / 2 , MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
