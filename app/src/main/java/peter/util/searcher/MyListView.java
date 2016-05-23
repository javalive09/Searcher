package peter.util.searcher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by peter on 16/5/23.
 */
public class MyListView extends ListView{
    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE / 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
