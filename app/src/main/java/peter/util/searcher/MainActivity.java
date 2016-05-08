package peter.util.searcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private String webHintUrl;
    private String[] webEngineUrls;
    private int currentWebEngine;


    private static final int STATUS_HIDE = 0;
    private static final int STATUS_CLEAR = 1;
    private static final int STATUS_LOADING = 2;

    private static final int STATUS_SEARCH = 0;
    private static final int STATUS_MENU = 1;

    private static final int HINT_ACTIVITY = 1;

    private WebView webview;
    private EditTextBackEvent search;
    private ImageView operate;
    private ImageView menus;
    private ImageView engine;
    private ListPopupWindow engineList;
    private EngineAdapter engineAdapter;

    private boolean showHint = true;
    private PullView frame;
    private AsynWindowHandler windowHandler;
    private int[] engineIcon = new int[]{
            R.drawable.baidu_,
            R.drawable.sougo_,
            R.drawable.haosou_,
            R.drawable.bing_,
            R.drawable.shenma_,
            R.drawable.ciba_
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {

            @Override
            public boolean queueIdle() {
                init();
                UpdateController.instance(getApplicationContext()).autoCheckVersion(windowHandler);
                return false;
            }
        });
    }

    private void init() {
        windowHandler = new AsynWindowHandler(this);
        webEngineUrls = getResources().getStringArray(R.array.engine_web_urls);
        webHintUrl = getString(R.string.web_hint_url);
        currentWebEngine = getSharedPreferences("setting", MODE_PRIVATE)
                .getInt("engine", getResources().getInteger(R.integer.default_engine));
        webview = (WebView) findViewById(R.id.wv);
        frame = (PullView) findViewById(R.id.frame);
        menus = (ImageView) findViewById(R.id.menus);
        engine = (ImageView) findViewById(R.id.engine);
        refreshEngineIcon();
        if (Build.VERSION.SDK_INT < 21) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        search = (EditTextBackEvent) findViewById(R.id.search);
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
        search.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
            @Override
            public void onImeBack(EditTextBackEvent ctrl, String text) {
                if (webview != null) {
                    if (TextUtils.isEmpty(webview.getUrl())) {
                        finish();
                    } else {
                        dismissHint();
                    }
                }

            }
        });
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.i("peter", "" + v + " ;hasFocus=" + hasFocus);
                if (hasFocus) {
                    if (!isFinishing()) {
                        showHintList();
                    }
                } else {
                    dismissHint();
                }
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
                    setOptLevel(STATUS_HIDE);
                    getDataFromDB();
                } else if (!content.equals(temp)) {
                    setOptLevel(STATUS_CLEAR);
                    setMenusLevel(STATUS_SEARCH);
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
        menus.setOnClickListener(this);
        engine.setOnClickListener(this);
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
                setMenusLevel(STATUS_LOADING);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setMenusLevel(STATUS_MENU);
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

        getDataFromDB();
    }

    boolean isFinishing;

    public void finish() {
        if (!isFinishing) {
            isFinishing = true;
            if (dismissHint() | dismissEngineList()) {
                search.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doFinish();
                    }
                }, 300);
            } else {
                doFinish();
            }
        }
    }

    private void doFinish() {
        super.finish();
    }

    private void setOptLevel(int level) {
        LevelListDrawable d = (LevelListDrawable) operate.getDrawable();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    private void setMenusLevel(int level) {
        LevelListDrawable d = (LevelListDrawable) menus.getDrawable();
        if (d.getLevel() != level) {
            d.setLevel(level);
        }
    }

    private void showEngineList() {
        if (engineList == null) {
            engineList = new ListPopupWindow(this);
            engineAdapter = new EngineAdapter(engineIcon, MainActivity.this);
            engineList.setAdapter(engineAdapter);
            engineList.setAnchorView(findViewById(R.id.engine_anchor));
            engineList.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            engineList.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            engineList.setModal(false);
        }
        if (!engineList.isShowing()) {
            engineList.show();
        }
    }

    private boolean dismissEngineList() {
        if(engineList!=null && engineList.isShowing()) {
            engineList.dismiss();
            return true;
        }
        return false;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (webview != null) {
            webview.onResume();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case HINT_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    frame.resetPlayExit();
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
            if (webview != null && webview.canGoBack()) {
                dismissHint();
                webview.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitHint() {
        if (!frame.played) {
            startActivityForResult(new Intent(MainActivity.this, GuideActivity.class), HINT_ACTIVITY);
            frame.startPlayExit();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("peter", "onDestroy");
        windowHandler.sendEmptyMessage(AsynWindowHandler.DESTROY);
        dismissEngineList();
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
                        updateHintList(searches);
                    } else {
                        dismissHint();
                    }
                }

            }
        }.execute();
    }

    private void updateHintList(List<Search> searches) {
        showHintList();
        Message msg = Message.obtain();
        msg.what = AsynWindowHandler.UPDATE_HINT_LIST_DATA;
        msg.obj = searches;
        windowHandler.sendMessage(msg);
    }

    private void showHintList() {
        Message msg = Message.obtain();
        msg.what = AsynWindowHandler.SHOW_HINT_LIST;
        msg.obj = search;
        windowHandler.sendMessage(msg);
    }

    private boolean dismissHint() {
        boolean suc = windowHandler.isHintListShowing();
        Message msg = Message.obtain();
        msg.what = AsynWindowHandler.DISMISS_HINT_LIST;
        windowHandler.sendMessage(msg);
        return suc;
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
                        updateHintList(searches);
                    } else {
                        dismissHint();
                    }
                }

            }
        }.execute();
    }

    private void doSearch() {
        String word = search.getText().toString().trim();
        if (!TextUtils.isEmpty(word)) {
            String url = getEngineUrl(word);
            if (!TextUtils.isEmpty(url)) {
                webview.loadUrl(url);
                //hide input
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(search.getWindowToken(), 0);

                //hide hintList
                dismissHint();
                saveData(word);
            }
        }
    }

    private String getEngineUrl(String word) {
        if (word.startsWith("http:")
                || word.startsWith("https:")) {
            return word;
        }
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
                search.setText("");
                setOptLevel(STATUS_HIDE);
                setMenusLevel(STATUS_SEARCH);
                search.requestFocus();
                openBoard();
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
                showHintList();
                break;
            case R.id.menus:
                LevelListDrawable d = (LevelListDrawable) menus.getDrawable();
                switch (d.getLevel()) {
                    case STATUS_SEARCH:
                        doSearch();
                        break;
                    case STATUS_MENU:
                        popupMenu(v);
                        break;
                }

                break;
            case R.id.engine:
                showEngineList();
                break;
            case R.id.engine_item:
                int position = (int) v.getTag();
                currentWebEngine = position;
                getSharedPreferences("setting", MODE_PRIVATE).edit().putInt("engine", currentWebEngine).commit();
                dismissEngineList();
                refreshEngineIcon();
                doSearch();
                break;
        }
    }

    private void refreshEngineIcon() {
        LevelListDrawable drawable = (LevelListDrawable) engine.getDrawable();
        drawable.setLevel(currentWebEngine);
    }

    private void popupMenu(View menu) {
        PopupMenu popup = new PopupMenu(this, menu);
        popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
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
                }

                return true;
            }
        });
        popup.show();
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
                updateHintList(new ArrayList<Search>(1));
                dismissHint();
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

    private static class EngineAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        MainActivity act;
        private int[] drawableRes;

        public EngineAdapter(int[] drawableRes, MainActivity act) {
            this.act = act;
            factory = LayoutInflater.from(act);
            this.drawableRes = drawableRes;
        }

        @Override
        public int getCount() {
            return drawableRes.length;
        }

        @Override
        public Integer getItem(int position) {
            return drawableRes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = factory.inflate(R.layout.engine_item, parent, false);
            }
            int resId = getItem(position);
            convertView.findViewById(R.id.icon).setBackgroundResource(resId);

            String name = act.getResources().getStringArray(R.array.engine_web_names)[position];
            TextView tv = (TextView) convertView.findViewById(R.id.name);
            tv.setText(name);
            convertView.setOnClickListener(act);
            convertView.setTag(position);
            return convertView;
        }

    }
}
