package peter.util.searcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.PopupMenu;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.utils.Constants;

/**
 * 自定义的webView
 * Created by peter on 2017/6/8.
 */

public class SearchWebView extends WebView {

    private int startY, touchX, touchY;
    private static int SLOP;
    private boolean popMenu;

    public SearchWebView(Context context) {
        super(context);
        SLOP = Constants.getActionBarH(context) * 2;
    }

    public SearchWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SLOP = Constants.getActionBarH(context) * 2;
    }

    public SearchWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SLOP = Constants.getActionBarH(context) * 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + Constants.getActionBarH(getContext()));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = y;
                touchX = x;
                touchY = y;
                popMenu = false;
                break;
            case MotionEvent.ACTION_UP:
                if (!popMenu) {
                    int deltaY = y - startY;
                    if (deltaY > SLOP) { //show
                        ((MainActivity) getContext()).showTopbar();
                    } else if (deltaY < -SLOP) {//hide
                        ((MainActivity) getContext()).hideTopbar();
                    }
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        popMenu = true;
        return new ContextMenuInfo(getHitTestResult(), touchX, touchY);
    }


    public static class ContextMenuInfo implements ContextMenu.ContextMenuInfo {

        final HitTestResult result;
        final int x, y;

        ContextMenuInfo(HitTestResult result, int x, int y) {
            this.result = result;
            this.x = x;
            this.y = y;
        }

        public HitTestResult getResult() {
            return result;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

    public static class OnMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {


        SearchWebView.ContextMenuInfo info;

        String url;

        public ContextMenuInfo getInfo() {
            return info;
        }

        public OnMenuItemClickListener setInfo(ContextMenuInfo info) {
            this.info = info;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public OnMenuItemClickListener setUrl(String url) {
            this.url = url;
            return this;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return false;
        }
    }

}
