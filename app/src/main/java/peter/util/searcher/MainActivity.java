package peter.util.searcher;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Set;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private View bottomBar;
    private long mExitTime = 0;
    public static final String HOME = "file:///android_asset/susou.html";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearcherWebViewManager.instance().setMainAct(this);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        bottomBar = findViewById(R.id.bottom_bar);
        Intent intent = getIntent();
        if (intent != null) {
            Set<String> category = intent.getCategories();
            if (category != null && category.contains(Intent.CATEGORY_LAUNCHER)) {//launcher invoke
//                startBrowser(this,  MainActivity.HOME, "", false);
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
                } else {// url null finish
                    startSearch(true);
                    finish();
                }
            } else if (Intent.ACTION_VIEW.equals(action)) { // outside invoke
                String url = intent.getDataString();
                if (!TextUtils.isEmpty(url)) {
                    loadUrl(url, "", true);
                } else {// url null finish
                    startSearch(true);
                    finish();
                }
            }else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
                String searchWord = intent.getStringExtra(SearchManager.QUERY);
                String engineUrl = getString(R.string.default_engine_url);
                String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                loadUrl(url, searchWord, true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            SearcherWebView searcherWebView = SearcherWebViewManager.instance().getCurrentWebView();
            if (searcherWebView != null && searcherWebView.canGoBack()) {
                searcherWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void loadUrl(String url, String searchWord, boolean isNewTab) {
        SearcherWebView view;
        if (isNewTab) {//new tab
            view = SearcherWebViewManager.instance().newWebview();
            view.getSetting().setCacheMode(WebSettings.LOAD_NO_CACHE);
            view.clearView();
            view.loadUrl(url, searchWord);
        } else {
            view = SearcherWebViewManager.instance().containUrlView(url);
            if (view != null) {//切换
                view.setStatusMainColor();
                SearcherWebViewManager.instance().setCurrentWebView(view);
            } else {//搜索, first
                view = SearcherWebViewManager.instance().getCurrentWebView();
                if (view == null) {
                    view = SearcherWebViewManager.instance().newWebview();
                }
                view.clearView();
                view.getSetting().setCacheMode(WebSettings.LOAD_DEFAULT);
                view.loadUrl(url, searchWord);
            }
        }
        clearFrameContentView();
        setFrameContentView(view.getRootView());
    }

    private void clearFrameContentView() {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (contentFrame != null) {
            contentFrame.removeAllViews();
        }
    }

    private void setFrameContentView(View contentView) {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        if (contentFrame != null) {
            contentFrame.addView(contentView);
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
        clearFrameContentView();
        SearcherWebViewManager.instance().setMainAct(null);
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
            case R.id.home:
                startBrowser(this,  MainActivity.HOME, "", false);
                break;
            case R.id.refresh:
                webView.refresh();
                break;
            case R.id.search:
                startSearch(false);
                break;
            case R.id.search_btn:
                startSearch(false);
                break;
            case R.id.multi_window:
//                popUpMultiWindow(v);
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
//                    case R.id.action_go:
//                        webView = SearcherWebViewManager.instance().getCurrentWebView();
//                        if (webView.canGoForward()) {
//                            webView.goForward();
//                        }
//                        break;
                    case R.id.action_copy_link:
                        url = SearcherWebViewManager.instance().getCurrentWebView().getUrl();
                        String title = SearcherWebViewManager.instance().getCurrentWebView().getTitle();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(MainActivity.this, R.string.copy_link_txt, Toast.LENGTH_SHORT).show();
                        break;
//                    case R.id.action_history_url:
//                        startActivity(new Intent(MainActivity.this, HistoryURLActivity.class));
//                        break;
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

//    private void popUpMultiWindow(View anchor) {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View view = inflater.inflate(R.layout.popup_window_list, null);
//        ListView listView = (ListView) view.findViewById(R.id.list);
//        listView.setAdapter(new PopupWindowAdapter(inflater,
//                SearcherWebViewManager.instance().getAllViews(),
//                new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                }));
//        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
//        popupWindow.update();
//        popupWindow.setTouchable(true);
//        popupWindow.setFocusable(true);
//        popupWindow.showAtLocation(findViewById(R.id.root), Gravity.LEFT | Gravity.TOP, 0, 0);
//
//    }

//    static class PopupWindowAdapter extends BaseAdapter {
//
//        private List<SearcherWebView> list;
//        private LayoutInflater inflater;
//        private View.OnClickListener listener;
//
//        public PopupWindowAdapter(LayoutInflater inflater, List<SearcherWebView> list, View.OnClickListener listener) {
//            this.list = list;
//            this.inflater = inflater;
//            this.listener = listener;
//        }
//
//        @Override
//        public int getCount() {
//            return list.size();
//        }
//
//        @Override
//        public SearcherWebView getItem(int position) {
//            return list.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if(convertView == null) {
//                convertView = inflater.inflate(R.layout.multi_window_list_item, parent, false);
//            }
//            TextView content = (TextView) convertView.findViewById(R.id.list_item);
//            ImageView close = (ImageView) convertView.findViewById(R.id.close);
//            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
//
//            SearcherWebView search = getItem(position);
//            icon.setImageBitmap(search.getFavIcon());
//            content.setText(search.getTitle());
//            content.setOnClickListener(listener);
//            content.setTag(search);
//            close.setTag(search);
//            close.setOnClickListener(listener);
//            return convertView;
//        }
//    }

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
