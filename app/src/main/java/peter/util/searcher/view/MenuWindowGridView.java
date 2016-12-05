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

    public MenuWindowGridView(Context context) {
        super(context);
    }

    public MenuWindowGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MenuWindowGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE / 2 , MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
