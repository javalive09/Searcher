package peter.util.searcher.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by peter on 2016/11/22.
 */

public class ObservableEditText extends AppCompatEditText {

    BackPressCallBack mBackPress;

    public ObservableEditText(Context context) {
        super(context);
    }

    public ObservableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if(mBackPress != null) {
                mBackPress.backPress();
                return false;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setBackPressCallBack(BackPressCallBack backPressCallBack) {
        mBackPress = backPressCallBack;
    }

    public interface BackPressCallBack {
        void backPress();
    }
}
