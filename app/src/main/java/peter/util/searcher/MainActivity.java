package peter.util.searcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private String webHintUrl;

    private String[] webEngineUrls;

    private int currentWebEngine;

    //0-searcher; 1-operate_bg; 2-loading;
    private static final int SETTING = 0;
    private static final int CLEAR = 1;
    private static final int LOADING = 2;

    private static final int HINT_ACTIVITY = 1;
    private static final int SETTING_ACTIVITY = 2;

    private WebView webview;
    private EditText search;
    private ImageView operate;
    private HintAdapter adapter;

    private ListPopupWindow hintList;
    private boolean showHint = true;
    private PullView frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {

            @Override
            public boolean queueIdle() {
                init();
                return false;
            }
        });
    }

    private void init() {
        webEngineUrls = getResources().getStringArray(R.array.engine_web_urls);
        webHintUrl = getString(R.string.web_hint_url);
        currentWebEngine = getSharedPreferences("setting", MODE_PRIVATE)
                .getInt("engine", getResources().getInteger(R.integer.default_engine));
        webview = (WebView) findViewById(R.id.wv);
        frame = (PullView) findViewById(R.id.frame);
        if (Build.VERSION.SDK_INT < 21) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        search = (EditText) findViewById(R.id.search);
        search.setOnClickListener(this);
        operate = ((ImageView) findViewById(R.id.operate));
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                    return true;
                }
                return false;
            }
        });

        search.addTextChangedListener(new TextWatcher() {
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
                    setOperateLevel(SETTING);
                    getDataFromDB();
                } else if (!content.equals(temp)) {
                    setOperateLevel(CLEAR);
                    if (showHint) {
                        String path = String.format(webHintUrl, getEncodeString(content));
                        getDataFromWeb(path);
                    } else {
                        showHint = true;
                    }
                }
            }
        });
        operate.setOnClickListener(this);
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
                setOperateLevel(LOADING);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setOperateLevel(CLEAR);
                showExitHint();
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

        adapter = new HintAdapter(new ArrayList<Search>(1));
        hintList = new ListPopupWindow(this);
        hintList.setAdapter(adapter);
        hintList.setAnchorView(search);
        hintList.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        hintList.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        hintList.setModal(false);
        getDataFromDB();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if(webview != null) {
            webview.onResume();
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if(webview != null) {
            webview.onPause();
        }
    }

    private void showEngineDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.engine_title)
                .setSingleChoiceItems(R.array.engine_web_names, currentWebEngine, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(whichButton != currentWebEngine) {
                            currentWebEngine = whichButton;
                            getSharedPreferences("setting", MODE_PRIVATE).edit().putInt("engine", currentWebEngine).commit();
                            Intent intent = new Intent();
                            intent.putExtra("currentWebEngine", currentWebEngine);
                            setResult(RESULT_OK, intent);
                        }
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HINT_ACTIVITY:
                if(resultCode == RESULT_OK) {
                    frame.resetPlayExit();
                }
                break;
            case SETTING_ACTIVITY:
                if(resultCode == RESULT_OK) {
                    currentWebEngine = data.getIntExtra("currentWebEngine", 0);
                }
                break;
        }
    }

    private String getEncodeString(String content) {
        try {
            content = URLEncoder.encode(content, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return content;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(hintList!= null && hintList.isShowing()) {
                hintList.dismiss();
            }
            if (webview != null && webview.canGoBack()) {
                webview.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitHint() {
        if (!frame.played) {
            startActivityForResult(new Intent(MainActivity.this, HintActivity.class), HINT_ACTIVITY);
            frame.startPlayExit();
        }
    }

    @Override
    protected void onDestroy() {
        if (hintList != null && hintList.isShowing()) {
            hintList.dismiss();
        }
        ViewGroup root = (ViewGroup) findViewById(R.id.root);
        root.removeView(webview);
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

    private void getDataFromDB() {
        new AsyncTask<Void, Void, List<Search>>() {

            @Override
            protected List<Search> doInBackground(Void... params) {
                return SqliteHelper.instance(MainActivity.this).queryData();
            }

            @Override
            protected void onPostExecute(List<Search> searches) {
                super.onPostExecute(searches);
                if (searches != null) {
                    if (searches.size() > 0) {
                        Search delete = new Search();
                        delete.name = getString(R.string.clear_history);
                        searches.add(delete);
                        adapter.updateData(searches);
                        showHintList();
                    } else if(hintList != null){
                        if (hintList.isShowing()) {
                            hintList.dismiss();
                        }
                    }
                }

            }
        }.execute();
    }

    private void showHintList() {
        if (hintList != null && !hintList.isShowing()) {
            hintList.show();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 2];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    private List<Search> requestByGet(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setConnectTimeout(5 * 1000);
        urlConn.connect();

        if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            byte[] data = readStream(urlConn.getInputStream());
            String result = new String(data, "GBK");
            int start = result.indexOf("[") + 1;
            int end = result.indexOf("]");
            result = result.substring(start, end);
            String[] strs = result.split(",");
            int size = strs.length > 5 ? 5 : strs.length;
            List<Search> searches = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (!TextUtils.isEmpty(strs[i])) {
                    Search search = new Search();
                    search.name = strs[i].replaceAll("\"", "");
                    searches.add(search);
                }
            }
            return searches;
        }
        urlConn.disconnect();
        return null;
    }

    private void getDataFromWeb(final String path) {
        new AsyncTask<Void, Void, List<Search>>() {


            @Override
            protected List<Search> doInBackground(Void... params) {
                List<Search> searches = null;
                try {
                    searches = requestByGet(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return searches;
            }

            @Override
            protected void onPostExecute(List<Search> searches) {
                super.onPostExecute(searches);
                if (searches != null) {
                    if (searches.size() > 0) {
                        adapter.updateData(searches);
                        showHintList();
                    } else {
                        if (hintList.isShowing()) {
                            hintList.dismiss();
                        }
                    }
                }

            }
        }.execute();
    }

    private class HintAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private List<Search> list;

        public HintAdapter(List<Search> objects) {
            factory = LayoutInflater.from(MainActivity.this);
            list = objects;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Search getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void updateData(List<Search> objects) {
            list = objects;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;

            if(convertView == null) {
                view = (TextView) factory.inflate(R.layout.history_item, parent, false);
            }else {
                view = (TextView)convertView;
            }

            Search search = getItem(position);

            if (search.name.equals(getString(R.string.clear_history))) {
                Drawable drawable = getResources().getDrawable(R.drawable.search_clear);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                view.setCompoundDrawables(drawable, null, null, null);
            }else {
                Drawable drawable = getResources().getDrawable(R.drawable.search_small);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                view.setCompoundDrawables(drawable, null, null, null);
            }

            view.setText(search.name);
            view.setOnClickListener(MainActivity.this);
            view.setTag(search);
            return view;
        }
    }

    private void setOperateLevel(int level) {
        LevelListDrawable drawable = (LevelListDrawable) operate.getDrawable();
        drawable.setLevel(level);
    }

    private void doSearch() {
        String word = search.getText().toString().trim();
        if (!TextUtils.isEmpty(word)) {
            setOperateLevel(LOADING);
            String url = getEngineUrl(word);
            if (!TextUtils.isEmpty(url)) {
                webview.loadUrl(url);
                //hide input
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(search.getWindowToken(), 0);

                //hide hintList
                if (hintList.isShowing()) {
                    hintList.dismiss();
                }
                saveData(word);
            }
        }
    }

    private String getEngineUrl(String word) {
        return String.format(webEngineUrls[currentWebEngine], getEncodeString(word));
    }

    private void saveData(final String word) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Search search = new Search();
                search.name = word;
                search.time = System.currentTimeMillis();
                SqliteHelper.instance(MainActivity.this).insert(search);
                SqliteHelper.instance(MainActivity.this).trimData();
                return null;
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.operate:
                LevelListDrawable drawable = (LevelListDrawable) operate.getDrawable();
                switch (drawable.getLevel()) {
                    case SETTING:
                        closeBoard();
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        intent.putExtra("currentWebEngine", currentWebEngine);
                        startActivityForResult(intent, SETTING_ACTIVITY);
                        break;
                    case CLEAR:
                        search.setText("");
                        setOperateLevel(SETTING);
                        search.requestFocus();
                        openBoard();
                        break;
                    case LOADING:
                        break;
                }
                break;
            case R.id.hint_item:
                Search s = (Search) v.getTag();

                if (s != null) {
                    if (getString(R.string.clear_history).equals(s.name)) {
                        clearHistory();
                    } else {
                        showHint = false;
                        search.setText(s.name);
                        search.setSelection(s.name.length());
                        doSearch();
                    }
                }
                break;
            case R.id.search:
                break;
        }
    }

    private void clearHistory() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SqliteHelper.instance(MainActivity.this).deleteAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hintList.dismiss();
            }
        }.execute();
    }

    public void closeBoard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_FORCED);
    }
}
