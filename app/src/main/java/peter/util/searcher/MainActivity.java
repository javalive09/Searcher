package peter.util.searcher;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends SlidingFragmentActivity {

    private MainFragment mContent;
    protected MenuFragment mFrag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(this));

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);
        if (savedInstanceState == null) {
            FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
            mFrag = new MenuFragment();
            t.replace(R.id.menu_frame, mFrag);
            t.commit();
        } else {
            mFrag = (MenuFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }

        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        sm.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                if (mContent != null) {
                    mContent.closeBoard();
                }
            }
        });

        // set the Above View
        if (savedInstanceState != null)
            mContent = (MainFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        if (mContent == null) {
            mContent = new MainFragment();
        }

        // set the Above View
        setContentView(R.layout.content_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, mContent)
                .commit();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            String url = intent.getStringExtra("url");
            String name = intent.getStringExtra("name");
            if (!TextUtils.isEmpty(url)) {
                if (mContent != null) {
                    mContent.loadUrl(name, url);
                }
            }
        }
    }

    public MainFragment getMainFragment() {
        return mContent;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mContent != null && mContent.onKeyDown()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(mContent != null) {
            mContent.onConfigurationChanged(newConfig);
        }
    }

}
