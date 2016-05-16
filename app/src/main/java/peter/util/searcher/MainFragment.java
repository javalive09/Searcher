package peter.util.searcher;

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
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainFragment extends Fragment implements View.OnClickListener {

    private WebView webview;
    private EditText input;
    private ViewGroup root;
    private String[] webEngineUrls;
    private ImageView operate;
    private GridView engine;
    private int mCurrentWebEngine;
    private View mVideoCustomView;
    private WebChromeClient mWebchromeclient;

    private static final int STATUS_SEARCH = 0;
    private static final int STATUS_CLEAR = 1;
    private static final int STATUS_LOADING = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = (ViewGroup) inflater.inflate(R.layout.activity_main, container,false);
        initView();
        return root;
    }

    private void initView() {
        webEngineUrls = getResources().getStringArray(R.array.engine_web_urls);
        webview = (WebView) root.findViewById(R.id.wv);
        operate = (ImageView) root.findViewById(R.id.operate);
        operate.setOnClickListener(this);
        root.findViewById(R.id.menu).setOnClickListener(this);
        if (Build.VERSION.SDK_INT < 21) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        input = (EditText) root.findViewById(R.id.input);
        input.clearFocus();
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
                    setOptLevel(STATUS_SEARCH);
                    dismissEngine();
                } else if (!content.equals(temp)) {
                    setOptLevel(STATUS_CLEAR);
                    popupEngine();
                }
            }
        });
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);

        final FrameLayout video_fullView = (FrameLayout) root.findViewById(R.id.video_fullView);
        mWebchromeclient = new WebChromeClient() {
            CustomViewCallback xCustomViewCallback;
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                Log.i("peter", "onShowCustomView");
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
                setOptLevel(STATUS_LOADING);
                Log.i("peter", "url=" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setOptLevel(STATUS_CLEAR);
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

    }

    public void hideCustomView() {
        mWebchromeclient.onHideCustomView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    private void dismissEngine() {
        if(engine != null && engine.getVisibility() == View.VISIBLE) {
            engine.setVisibility(View.INVISIBLE);
        }
    }

    public String getCurrentUrl() {
        if(webview != null){
            return webview.getUrl();
        }
        return "";
    }

    private String getEncodeString(String content) {
        try {
            content = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }

    public boolean onKeyDown() {
        if(webview != null && webview.canGoBack()) {
            if(mVideoCustomView != null) {//在全屏播放视频
                hideCustomView();
            }else {
                webview.goBack();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Log.i("peter", "onDestroy");
        root.removeView(webview);
        dismissEngine();
        webview.removeAllViews();
        webview.stopLoading();
        webview.clearCache(true);
        webview.clearHistory();
        webview.destroy();
        webview.clearDisappearingChildren();
        webview.clearFocus();
        webview.clearFormData();
        webview.clearMatches();
        super.onDestroy();
    }

    private void doSearch(String word, int currentWebEngine) {
        if (!TextUtils.isEmpty(word)) {
            mCurrentWebEngine = currentWebEngine;
            String url = getEngineUrl(word, currentWebEngine);
            String name = getHistoryName(word);
            loadUrl(name, url);
        }
    }

    void loadUrl(String word, String url) {
        if (!TextUtils.isEmpty(url)) {
            webview.loadUrl(url);
            closeBoard();
            dismissEngine();
            saveData(word, url);
        }
    }

    public String getInputStr() {
        return input.getText().toString().trim();
    }

    public String getHistoryName(String word) {
        return word +  "  " + getEngineName();
    }

    private String getEngineName() {
        return getResources().getStringArray(R.array.engine_web_names)[mCurrentWebEngine];
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

    private String getEngineUrl(String word, int currentWebEngine) {
        if (word.startsWith("http:")
                || word.startsWith("https:")) {
            return word;
        }
        return String.format(webEngineUrls[currentWebEngine], getEncodeString(word));
    }

    private void saveData(final String word, final String url) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bean search = new Bean();
                search.name = word;
                search.time = System.currentTimeMillis();
                search.url = url;
                SqliteHelper.instance(getActivity().getApplicationContext()).insertHistory(search);
                return null;
            }
        }.execute();
    }

    private void setOptLevel(int level) {
        LevelListDrawable d = (LevelListDrawable) operate.getDrawable();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.operate:
                setOptLevel(STATUS_SEARCH);
                input.setText("");
                input.requestFocus();
                openBoard();
                break;
            case R.id.input:
                popupEngine();
                break;
            case R.id.engine_item:
                int position = (int) v.getTag();
                doSearch(getInputStr(), position);
                break;
            case R.id.menu:
                ((MainActivity)getActivity()).getSlidingMenu().toggle();
                break;
        }
    }

    public void closeBoard() {
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
    }

    private void popupEngine() {

        if(engine == null) {
            engine = (GridView) root.findViewById(R.id.engine);
            String[] str = getResources().getStringArray(R.array.engine_web_names);
            engine.setAdapter(new MyAdapter(MainFragment.this, str));
        }
        engine.setVisibility(View.VISIBLE);
    }

    private static class MyAdapter extends BaseAdapter {

        MainFragment f;
        String[] s;
        LayoutInflater factory;

        public MyAdapter(MainFragment f, String[] s) {
            this.f = f;
            this.s = s;
            factory = f.getActivity().getLayoutInflater();
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
                    convertView.setOnClickListener(f);
                }
            }

            return convertView;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.i("peter", "onConfigurationChanged ORIENTATION_LANDSCAPE");
                View title = root.findViewById(R.id.title);
                if(title.getVisibility() == View.VISIBLE) {
                    title.setVisibility(View.GONE);
                    toggleFullscreen(true);
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.i("peter", "onConfigurationChanged ORIENTATION_PORTRAIT");
                title = root.findViewById(R.id.title);
                if(title.getVisibility() != View.VISIBLE) {
                    title.setVisibility(View.VISIBLE);
                    toggleFullscreen(false);
                }
                break;
        }

    }

    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getActivity().getWindow().setAttributes(attrs);
    }


}
