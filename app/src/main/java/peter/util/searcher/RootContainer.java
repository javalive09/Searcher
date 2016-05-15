package peter.util.searcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by peter on 16/5/15.
 */
public class RootContainer extends RelativeLayout {

    Rect engineRect = new Rect();
    Rect inputRect = new Rect();
    View engine, input;

    public RootContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RootContainer(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (engine == null) {
            engine = findViewById(R.id.engine);
        }
        if(input == null) {
            input = findViewById(R.id.input);
        }

        Log.i("peter", ev.toString());

        if (engine.getVisibility() == View.VISIBLE) {
            if(ev.getAction() == MotionEvent.ACTION_DOWN) {
                engine.getHitRect(engineRect);
                input.getHitRect(inputRect);
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                if (!engineRect.contains(x, y)
                        && !inputRect.contains(x, y)) {
                    engine.setVisibility(View.INVISIBLE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
