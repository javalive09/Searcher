package peter.util.searcher;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int API = Build.VERSION.SDK_INT;
    private View bottomBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        bottomBar = findViewById(R.id.bottom_bar);
        checkIntentData(getIntent());
    }

    public void setBottomBarColor(int animColor) {
        bottomBar.setBackgroundColor(animColor);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntentData(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusColor(int color) {
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(color);
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_INNER_BROWSE.equals(action)) { // inner invoke
                String url = (String) intent.getSerializableExtra("url");
                if (!TextUtils.isEmpty(url)) {
                    String searchWord = intent.getStringExtra("word");
                    loadUrl(url, searchWord);
                }
            }
        }
    }

    public void refreshStatusColor(SearcherWebView view) {
        setBottomBarColor(view.getMainColor());
        if (API >= 21) {
            setStatusColor(view.getMainColor());
        }
    }

    private void loadUrl(String url, String searchWord) {
        SearcherWebView view = SearcherWebViewManager.instance().newWebview(this, url, searchWord);
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (contentFrame != null && view != null) {
            contentFrame.removeAllViews();
            contentFrame.addView(view.getRootView());
        }
    }

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
    }

    @Override
    public void onClick(View v) {
        SearcherWebView webView = SearcherWebViewManager.instance().getCurrentWebView();
        switch (v.getId()) {
            case R.id.back:
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                break;
            case R.id.go:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
            case R.id.refresh:
                SearcherWebViewManager.instance().getCurrentWebView().refresh();
                break;

            case R.id.favorite:
                if (!TextUtils.isEmpty(webView.getUrl())) {
                    Bean bean = new Bean();
                    bean.name = webView.getFavName();
                    bean.url = webView.getUrl();
                    bean.time = System.currentTimeMillis();
                    SqliteHelper.instance(MainActivity.this).insertFav(bean);
                    Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.share:
                if (!TextUtils.isEmpty(webView.getUrl())) {
                    String url = webView.getUrl();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i("peter", "onConfigurationChanged ORIENTATION_LANDSCAPE");
                bottomBar.setVisibility(View.GONE);
                setFullscreen(true, true);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i("peter", "onConfigurationChanged ORIENTATION_PORTRAIT");
                bottomBar.setVisibility(View.VISIBLE);
                setFullscreen(false, false);
                break;
        }
    }

    /**
     * @param enabled   status bar
     * @param immersive
     */
    public void setFullscreen(boolean enabled, boolean immersive) {
        Window window = getWindow();
        View decor = window.getDecorView();
        if (enabled) {
            if (immersive) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }


}
