package peter.util.searcher;

import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * Created by peter on 16/5/9.
 */
public class BaseActivity extends SlidingFragmentActivity {

    private Fragment mContent;

    AsynWindowHandler windowHandler;
    private boolean isDestroyed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateController.instance(getApplicationContext()).autoCheckVersion(windowHandler);
        // get the Above View
        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        if (mContent == null) {
            mContent = new MainFragment();
        }

        // set the Above View
        setContentView(R.layout.content_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mContent)
                .commit();

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);

        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setMenu(R.layout.menu_frame);
        sm.setSecondaryMenu(R.layout.menu_frame_two);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_frame, new MenuFragment())
                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.menu_frame_two, new MenuFragment())
                .commit();

    }

    public MainFragment getMainFragment() {
        return (MainFragment) mContent;
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        if(windowHandler != null) {
            windowHandler.sendEmptyMessage(AsynWindowHandler.DESTROY);
        }
        super.onDestroy();
    }

    public boolean isActivityDestroyed() {
        return isDestroyed;
    }

    public AlertDialog showAlertDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(BaseActivity.this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.show();
        return dialog;
    }


}
