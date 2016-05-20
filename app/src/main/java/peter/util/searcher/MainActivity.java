package peter.util.searcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by peter on 16/5/9.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    private WebView webview;
    private EditText input;
    private String[] webEngineUrls;
    private GridView engine;
    private int mCurrentWebEngine;
    private View mVideoCustomView;
    private WebChromeClient mWebchromeclient;
    private MainFrameView frame;
    private View status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(this));

        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        frame = (MainFrameView) findViewById(R.id.frame);
        status = findViewById(R.id.status);
        webEngineUrls = getResources().getStringArray(R.array.engine_web_urls);
        webview = (WebView)findViewById(R.id.wv);
        findViewById(R.id.menu).setOnClickListener(this);
        if (Build.VERSION.SDK_INT < 21) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        input = (EditText) findViewById(R.id.input);
        input.setOnClickListener(this);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch(getInputStr(), 1);
                    return true;
                }
                return false;
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            String temp;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                temp = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (TextUtils.isEmpty(content)) {
                    dismissEngine();
                } else if (!content.equals(temp)) {
                    popupEngine();
                }
            }
        });
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);

        final FrameLayout video_fullView = (FrameLayout) findViewById(R.id.video_fullView);
        mWebchromeclient = new WebChromeClient() {
            CustomViewCallback xCustomViewCallback;
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                Log.i("peter", "onShowCustomView");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                webview.setVisibility(View.INVISIBLE);
                // 如果一个视图已经存在，那么立刻终止并新建一个
                if (mVideoCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                video_fullView.addView(view);
                mVideoCustomView = view;
                xCustomViewCallback = callback;
                video_fullView.setVisibility(View.VISIBLE);
            }

            // 视频播放退出全屏会被调用的
            @Override
            public void onHideCustomView() {
                Log.i("peter", "onHideCustomView");
                if (mVideoCustomView == null) {// 不是全屏播放状态
                    return;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                mVideoCustomView.setVisibility(View.GONE);
                video_fullView.removeView(mVideoCustomView);
                mVideoCustomView = null;
                video_fullView.setVisibility(View.GONE);
                xCustomViewCallback.onCustomViewHidden();
                webview.setVisibility(View.VISIBLE);
            }


        };
        webview.setWebChromeClient(mWebchromeclient);
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                // Ignore SSL certificate errors
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setStatusLevel(1);
                Log.i("peter", "url=" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setStatusLevel(0);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        checkIntentData(getIntent());
    }

    private void setStatusLevel(int level) {
        LevelListDrawable d = (LevelListDrawable)status.getBackground();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntentData(intent);
    }

    private void checkIntentData(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra("url");
            String name = intent.getStringExtra("name");
            if (!TextUtils.isEmpty(url)) {
                loadUrl(name, url);
            }else {
                String word = intent.getStringExtra("keyWord");
                if(!TextUtils.isEmpty(word)) {
                    doSearch(word, 1);
                }
            }
        }
    }

    private void loadUrl(String word, String url) {
        if (!TextUtils.isEmpty(url)) {
            webview.loadUrl(url);
            closeBoard();
            dismissEngine();
            saveData(word, url);
        }
    }

    private void dismissEngine() {
        if(engine != null && engine.getVisibility() == View.VISIBLE) {
            engine.setVisibility(View.INVISIBLE);
        }
    }

    private void saveData(final String word, final String url) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bean search = new Bean();
                search.name = word;
                search.time = System.currentTimeMillis();
                search.url = url;
                SqliteHelper.instance(getApplicationContext()).insertHistory(search);
                return null;
            }
        }.execute();
    }

    public void closeBoard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    public void hideCustomView() {
        mWebchromeclient.onHideCustomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(webview != null && webview.canGoBack()) {
                if(mVideoCustomView != null) {//在全屏播放视频
                    hideCustomView();
                }else {
                    webview.goBack();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onResume() {
        super.onResume();
        webview.onResume();
        webview.resumeTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webview.onPause();
        webview.pauseTimers();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i("peter", "onConfigurationChanged ORIENTATION_LANDSCAPE");

                frame.hideBar();
                toggleFullscreen(true);

//                View title = findViewById(R.id.title);
//                if(title.getVisibility() == View.VISIBLE) {
//                    title.setVisibility(View.GONE);
//                    toggleFullscreen(true);
//                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i("peter", "onConfigurationChanged ORIENTATION_PORTRAIT");
//                title = findViewById(R.id.title);
//                if(title.getVisibility() != View.VISIBLE) {
//                    title.setVisibility(View.VISIBLE);
//                    toggleFullscreen(false);
//                }

                frame.showBar();
                toggleFullscreen(false);
                break;
        }
    }

    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    private void popupEngine() {
        if(engine == null) {
            engine = (GridView) findViewById(R.id.engine);
            String[] str = getResources().getStringArray(R.array.engine_web_names);
            engine.setAdapter(new MyAdapter(this, str));
        }
        engine.setVisibility(View.VISIBLE);
    }

    private String getEngineUrl(String word, int currentWebEngine) {
        if (word.startsWith("http:")
                || word.startsWith("https:")) {
            return word;
        }
        return String.format(webEngineUrls[currentWebEngine], getEncodeString(word));
    }

    public String getHistoryName(String word) {
        return word +  "  " + getEngineName();
    }

    private String getEngineName() {
        return getResources().getStringArray(R.array.engine_web_names)[mCurrentWebEngine];
    }

    private String getEncodeString(String content) {
        try {
            content = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }

    public String getInputStr() {
        return input.getText().toString().trim();
    }

    public String getFavName() {
        String title = webview.getTitle();
        String engineName = getEngineName();
        Log.i("peter", "title = " + title);
        Log.i("peter", "engineName = " + engineName);
        if(getString(R.string.url_title_mark_cb).equals(engineName)) {//词霸
            title = getInputStr() + " - " + engineName;
        }else if(getString(R.string.url_title_mark_yd).equals(engineName)) {//有道
            title = getInputStr() + " - " + title;
        }else if(getString(R.string.url_title_mark_jd).equals(engineName)) {//京东
            title = getInputStr() + " - " + engineName;
        }else if(getString(R.string.url_title_mark_tb).equals(engineName)) {//淘宝
            title = getInputStr() + " - " + engineName;
        }else if(getString(R.string.url_title_mark_tx).equals(engineName)) {//腾讯视频
            title = getInputStr() + " - " + engineName + " - " + title;
        }else if(getString(R.string.url_title_mark_sh).equals(engineName)) {//搜狐视频
            title = getInputStr() + " - " + engineName + " - " + title;
        }else if(getString(R.string.url_title_mark_aqy).equals(engineName)) {//爱奇艺
            title = getInputStr() + " - " + engineName + " - " + title;
        }else if(getString(R.string.url_title_mark_yyb).equals(engineName)) {//应用宝
            title = getInputStr() + " - " + engineName;
        }else if(getString(R.string.url_title_mark_360zs).equals(engineName)) {//360助手
            title = getInputStr() + " - " + engineName + " - " + title;
        }else if(getString(R.string.url_title_mark_bdzs).equals(engineName)) {//百度助手
            title = getInputStr() + " - " + engineName + " - " + title;
        }else if(getString(R.string.url_title_mark_xm).equals(engineName)) {//小米
            title = getInputStr() + " - " + engineName + " - " + title;
        }
        Log.i("peter", "title = " + title);
        return title;
    }

    private void doSearch(String word, int currentWebEngine) {
        if (!TextUtils.isEmpty(word)) {
            if(!word.equals(input.getText())) {
                input.setText(word);
            }
            webview.requestFocus();
            mCurrentWebEngine = currentWebEngine;
            String url = getEngineUrl(word, currentWebEngine);
            String name = getHistoryName(word);
            loadUrl(name, url);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.input:
                popupEngine();
                break;
            case R.id.engine_item:
                int position = (int) v.getTag();
                doSearch(getInputStr(), position);
                break;
            case R.id.menu:
                popupMenu(v);
                break;
        }
    }

    private void popupMenu(View menu) {
        PopupMenu popup = new PopupMenu(this, menu);
        if(TextUtils.isEmpty(webview.getUrl())) {
            popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.web, popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_share:
                        if (webview != null) {
                            String url = webview.getUrl();
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
                        }
                        break;
                    case R.id.action_setting:
                        closeBoard();
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_collect:
                        Bean bean = new Bean();
                        bean.name = webview.getTitle();
                        bean.url = webview.getUrl();
                        bean.time = System.currentTimeMillis();
                        SqliteHelper.instance(MainActivity.this).insertFav(bean);
                        Toast.makeText(MainActivity.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_collection:
                        closeBoard();
                        startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                        break;
                    case R.id.action_history:
                        closeBoard();
                        startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                        break;
                    case R.id.action_exit:
                        finish();
                        break;
//                    case R.id.action_feedback:
//                        sendMailByIntent();
//                        break;
//                    case R.id.action_about:
//                        showAlertDialog(getString(R.string.action_about), getString(R.string.setting_about));
//                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private static class MyAdapter extends BaseAdapter {

        MainActivity a;
        String[] s;
        LayoutInflater factory;

        public MyAdapter(MainActivity a, String[] s) {
            this.a = a;
            this.s = s;
            factory = a.getLayoutInflater();
        }


        @Override
        public int getCount() {
            return s.length;
        }

        @Override
        public String getItem(int position) {
            return s[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = factory.inflate(R.layout.engine_item, parent, false);
            }
            TextView tv = (TextView)convertView;
            String txt = getItem(position);
            if(!TextUtils.isEmpty(txt)) {
                tv.setText(txt);
                convertView.setTag(position);
                if(txt.contains(":")) {
                    convertView.setEnabled(false);
                }else {
                    convertView.setOnClickListener(a);
                }
            }

            return convertView;
        }
    }

}
