package peter.util.searcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
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
    private EditTextBackEvent input;
    private ViewGroup root;
    private String[] webEngineUrls;

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
        if (Build.VERSION.SDK_INT < 21) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        input = (EditTextBackEvent) root.findViewById(R.id.input);
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
        input.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
            @Override
            public void onImeBack(EditTextBackEvent ctrl, String text) {
                if (webview != null) {
                    if (TextUtils.isEmpty(webview.getUrl())) {

                    } else {

                    }
                }

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
//                setOptLevel(STATUS_LOADING);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                setOptLevel(STATUS_CLEAR);
//                showExitHint();
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

//        getDataFromDB();
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

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            if (webview != null && webview.canGoBack()) {
//                dismissHint();
//                webview.goBack();
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onDestroy() {
        Log.i("peter", "onDestroy");
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

//    private void getDataFromDB() {
//        new AsyncTask<Void, Void, List<Bean>>() {
//
//            @Override
//            protected List<Bean> doInBackground(Void... params) {
//                return SqliteHelper.instance(getActivity().getApplicationContext()).queryRecentHistory();
//            }
//
//            @Override
//            protected void onPostExecute(List<Bean> searches) {
//                super.onPostExecute(searches);
//                if (searches != null) {
//                    if(!isDetached()) {
//                        if (searches.size() > 0) {
//                            Bean delete = new Bean();
//                            delete.name = getString(R.string.clear_history);
//                            searches.add(delete);
////                            updateHintList(searches);
//                        } else {
////                            dismissHint();
//                        }
//                    }
//                }
//
//            }
//        }.execute();
//    }

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

    private List<Bean> requestByGet(String path) throws Exception {
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
            List<Bean> searches = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (!TextUtils.isEmpty(strs[i])) {
                    Bean search = new Bean();
                    search.name = strs[i].replaceAll("\"", "");
                    searches.add(search);
                }
            }
            return searches;
        }
        urlConn.disconnect();
        return null;
    }

    private void doSearch(String word, int currentWebEngine) {
        input.setText(word);
        input.setSelection(word.length());
        if (!TextUtils.isEmpty(word)) {
            String url = getEngineUrl(word, currentWebEngine);
            if (!TextUtils.isEmpty(url)) {
                webview.setOnTouchListener(null);
                webview.loadUrl(url);
                //hide input
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);

//                saveData(word, url);
            }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.operate:
                input.setText("");
                input.requestFocus();
                openBoard();
                break;
            case R.id.hint_item:
                Bean s = (Bean) v.getTag();

                if (s != null) {
                    if (getString(R.string.clear_history).equals(s.name)) {
//                        clearHistory();
                    } else {
                        doSearch(s.name , 1);
                    }
                }
                break;
            case R.id.input:
                popupEngine(v);
                break;
//            case R.id.search:
//                doSearch(input.getText().toString().trim(), false);
//                break;
//            case R.id.menu:
//                popupMenu(v);
//                break;
//            case R.id.engine:
//                break;
//            case R.id.engine_item:
//                int position = (int) v.getTag();
//                currentWebEngine = position;
//                getSharedPreferences("setting", MODE_PRIVATE).edit().putInt("engine", currentWebEngine).commit();
//                dismissEngineList();
//                doSearch(input.getText().toString().trim(),false);
//                break;
        }
    }

//    public void closeBoard() {
//        InputMethodManager imm = (InputMethodManager)getContext().
//                getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
//    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
    }

    private PopupWindow popupEngine(View anchor) {
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.layout_engine, null);
        final PopupWindow mPopupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.engine_item, getResources().getStringArray(R.array.engine_web_names));
        GridView gridView = (GridView) popupView.findViewById(R.id.engine);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity().getApplicationContext(),
                        ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
                doSearch(input.getText().toString().trim(), position);
                mPopupWindow.dismiss();

            }
        });
        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAsDropDown(anchor);
        return mPopupWindow;
    }

}
