package peter.util.searcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener {

    private WebView webview;
    private EditText input;
    private ViewGroup root;
    private String[] webEngineUrls;
    private ImageView operate;
    private GridView engine;

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
                    doSearch(input.getText().toString().trim(), 1);
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
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebChromeClient(new WebChromeClient() {
        });
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                // Ignore SSL certificate errors
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setOptLevel(STATUS_LOADING);
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

    public String getCurrentTitle() {
        if(webview != null) {
            return webview.getTitle();
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
            webview.goBack();
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
            String url = getEngineUrl(word, currentWebEngine);
            loadUrl(word, url);
        }
    }

    void loadUrl(String word, String url) {
        if(!input.getText().equals(word)) {
            input.setText(word);
        }
        input.setSelection(word.length());
        if (!TextUtils.isEmpty(url)) {
            webview.loadUrl(url);
            closeBoard();
            dismissEngine();
            saveData(word, url);
        }
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
                doSearch(input.getText().toString().trim(), position);
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

}
