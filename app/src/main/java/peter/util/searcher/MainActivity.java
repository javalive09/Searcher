package peter.util.searcher;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.Set;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int API = Build.VERSION.SDK_INT;
    private View bottomBar;
    private int mainColor = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        bottomBar = findViewById(R.id.bottom_bar);
        Intent intent = getIntent();
        if (intent != null) {
            Set<String> category = intent.getCategories();
            if (category != null && category.contains(Intent.CATEGORY_LAUNCHER)) {//launcher invoke
                startSearch(true);
                finish();
            } else {
                checkIntentData(intent);
            }
        }
    }

    public void setBottomBarColor(int animColor) {
        bottomBar.setBackgroundColor(animColor);
    }

    public void setMainColor(int color) {
        mainColor = color;
    }

    public int getMainColor() {
        if(mainColor == -1) {
            mainColor = getResources().getColor(R.color.colorPrimary);
        }
        return mainColor;
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
                String url = (String) intent.getSerializableExtra(NAME_URL);
                if (!TextUtils.isEmpty(url)) {
                    String searchWord = intent.getStringExtra(NAME_WORD);
                    loadUrl(url, searchWord, isNewTab(intent));
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    loadUrl(url, "", true);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            SearcherWebView searcherWebView = SearcherWebViewManager.instance().getCurrentWebView();
            if (searcherWebView != null && searcherWebView.canGoBack()) {
                searcherWebView.goBack();
            }else{
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void loadUrl(String url, String searchWord, boolean isNewTab) {
        SearcherWebView view;
        if (isNewTab) {
            view = SearcherWebViewManager.instance().newWebview(this);
            view.loadUrl(url, searchWord);
        } else {
            view = SearcherWebViewManager.instance().containUrlView(url);
            if(view != null) {//切换
                view.setStatusMainColor();
            }else {//搜索
                view = SearcherWebViewManager.instance().getCurrentWebView();
                view.loadUrl(url, searchWord);
            }
        }

        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (contentFrame != null && view != null) {
            contentFrame.removeAllViews();
            contentFrame.addView(view.getRootView());
        }
    }

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        SearcherWebViewManager.instance().resumeAll();
        refreshMultiWindow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        SearcherWebViewManager.instance().pauseAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if(contentFrame != null) {
            contentFrame.removeAllViews();
        }
    }

    public void refreshMultiWindow() {
        TextView countView = (TextView) findViewById(R.id.multi_window);
        int count = SearcherWebViewManager.instance().getWebViewCount();
        if (countView != null) {
            String countStr = count + "";
            if (count > 9) {
                countStr = "*";
            }
            countView.setText(countStr);
        }
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
            case R.id.search:
                startSearch(false);
                break;

            case R.id.multi_window:
                startActivity(new Intent(MainActivity.this, MultiWindowActivity.class));
                break;
            case R.id.menu:
                popupMenu(v);
                break;
        }
    }

    private void popupMenu(View menu) {
        PopupMenu popup = new PopupMenu(this, menu);
        popup.getMenuInflater().inflate(R.menu.web, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_refresh:
                        SearcherWebViewManager.instance().getCurrentWebView().refresh();
                        break;
                    case R.id.action_share:
                        String url = SearcherWebViewManager.instance().getCurrentWebView().getUrl();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)));
                        break;
                    case R.id.action_setting:
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_collect:
                        SearcherWebView webView = SearcherWebViewManager.instance().getCurrentWebView();
                        if (!TextUtils.isEmpty(webView.getUrl())) {
                            Bean bean = new Bean();
                            bean.name = webView.getFavName();
                            bean.url = webView.getUrl();
                            bean.time = System.currentTimeMillis();
                            SqliteHelper.instance(MainActivity.this).insertFav(bean);
                            Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.action_collection:
                        startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                        break;
                    case R.id.action_history:
                        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                        break;
                    case R.id.action_exit:
                        exit();
                        break;
                }
                return true;
            }
        });
        popup.show();
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
