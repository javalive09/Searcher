package peter.util.searcher;

import android.view.View;

public class MainActivity_ extends MainActivity implements View.OnClickListener {

//    private String webHintUrl;
//    private String[] webEngineUrls;
//    private int currentWebEngine;
//
//    private static final int STATUS_SEARCH = 0;
//    private static final int STATUS_CLEAR = 1;
//    private static final int STATUS_LOADING = 2;
//
//    private static final int HINT_ACTIVITY = 1;
//
//    private WebView webview;
//    private EditTextBackEvent input;
//    private ImageView operate;
//    private ImageView menu;
//    private ImageView engine;
//    private ListPopupWindow engineList;
//    private EngineAdapter engineAdapter;
//
//    private boolean showHint = true;
//    private PullView frame;
//    private AsynWindowHandler windowHandler;
//    private int[] engineIcon = new int[]{
//            R.drawable.baidu_,
//            R.drawable.sougo_,
//            R.drawable.haosou_,
//            R.drawable.bing_,
//            R.drawable.shenma_,
//            R.drawable.ciba_
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
//
//            @Override
//            public boolean queueIdle() {
//                init();
//                UpdateController.instance(getApplicationContext()).autoCheckVersion(windowHandler);
//                return false;
//            }
//        });
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if(intent != null) {
//            String url = intent.getStringExtra("url");
//            String name = intent.getStringExtra("name");
//            if(!TextUtils.isEmpty(url)) {
//                doSearch(name, false);
//            }
//        }
//    }
//
//    private void init() {
//        windowHandler = new AsynWindowHandler(this);
//        webEngineUrls = getResources().getStringArray(R.array.engine_web_urls);
//        webHintUrl = getString(R.string.web_hint_url);
//        currentWebEngine = getSharedPreferences("setting", MODE_PRIVATE)
//                .getInt("engine", getResources().getInteger(R.integer.default_engine));
//        webview = (WebView) findViewById(R.id.wv);
//        webview.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
//        frame = (PullView) findViewById(R.id.frame);
//        menu = (ImageView) findViewById(R.id.menu);
//        engine = (ImageView) findViewById(R.id.engine);
//        refreshEngineIcon();
//        if (Build.VERSION.SDK_INT < 21) {
//            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
//        input = (EditTextBackEvent) findViewById(R.id.input);
//        input.setOnClickListener(this);
//        input.requestFocus();
//        operate = ((ImageView) findViewById(R.id.operate));
//        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    doSearch(input.getText().toString().trim(), false);
//                    return true;
//                }
//                return false;
//            }
//        });
//        input.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
//            @Override
//            public void onImeBack(EditTextBackEvent ctrl, String text) {
//                if (webview != null) {
//                    if (TextUtils.isEmpty(webview.getUrl())) {
//                        finish();
//                    } else {
//                        dismissHint();
//                    }
//                }
//
//            }
//        });
//        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                Log.i("peter", "" + v + " ;hasFocus=" + hasFocus);
//                if (hasFocus) {
//                    if (!isFinishing()) {
//                        showHintList();
//                    }
//                } else {
//                    dismissHint();
//                }
//            }
//        });
//        input.addTextChangedListener(new TextWatcher() {
//            String temp;
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                temp = s.toString();
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String content = s.toString();
//                if (TextUtils.isEmpty(content)) {
//                    setOptLevel(STATUS_SEARCH);
//                    getDataFromDB();
//                } else if (!content.equals(temp)) {
//                    setOptLevel(STATUS_CLEAR);
//                    if (showHint) {
//                        String path = String.format(webHintUrl, getEncodeString(content));
//                        getDataFromWeb(path);
//                    } else {
//                        showHint = true;
//                    }
//                }
//            }
//        });
//        operate.setOnClickListener(this);
//        menu.setOnClickListener(this);
//        findViewById(R.id.search).setOnClickListener(this);
//        engine.setOnClickListener(this);
//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.getSettings().setDomStorageEnabled(true);
//        webview.setWebChromeClient(new WebChromeClient() {
//        });
//        webview.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//                // Ignore SSL certificate errors
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                setOptLevel(STATUS_LOADING);
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                setOptLevel(STATUS_CLEAR);
//                showExitHint();
//            }
//
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return false;
//            }
//        });
//        webview.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//            }
//        });
//
//        getDataFromDB();
//    }
//
//    boolean isFinishing;
//
//    public void finish() {
//        if (!isFinishing) {
//            isFinishing = true;
//            if (dismissHint() | dismissEngineList()) {
//                input.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        doFinish();
//                    }
//                }, 300);
//            } else {
//                doFinish();
//            }
//        }
//    }
//
//    private void doFinish() {
//        super.finish();
//    }
//
//    private void setOptLevel(int level) {
//        LevelListDrawable d = (LevelListDrawable) operate.getDrawable();
//        if (d.getLevel() != level) {
//            d.setLevel(level);
//        }
//    }
//
//    private void showEngineList() {
//        if (engineList == null) {
//            engineList = new ListPopupWindow(this);
//            engineAdapter = new EngineAdapter(engineIcon, MainActivity_.this);
//            engineList.setAdapter(engineAdapter);
//            engineList.setAnchorView(findViewById(R.id.engine_anchor));
//            engineList.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
//            engineList.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//            engineList.setModal(false);
//        }
//        if (!engineList.isShowing()) {
//            engineList.show();
//        }
//    }
//
//    private boolean dismissEngineList() {
//        if(engineList!=null && engineList.isShowing()) {
//            engineList.dismiss();
//            return true;
//        }
//        return false;
//    }
//
//    public void onResume() {
//        super.onResume();
//        MobclickAgent.onResume(this);
//        if (webview != null) {
//            webview.onResume();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case HINT_ACTIVITY:
//                if (resultCode == RESULT_OK) {
//                    frame.resetPlayExit();
//                }
//                break;
//        }
//    }
//
//    private String getEncodeString(String content) {
//        try {
//            content = URLEncoder.encode(content, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return content;
//    }
//
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
//
//    private void showExitHint() {
//        if (!frame.played) {
//            startActivityForResult(new Intent(MainActivity_.this, GuideActivity.class), HINT_ACTIVITY);
//            frame.startPlayExit();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        Log.i("peter", "onDestroy");
//        windowHandler.sendEmptyMessage(AsynWindowHandler.DESTROY);
//        dismissEngineList();
//        ViewGroup root = (ViewGroup) findViewById(R.id.root);
//        root.removeView(webview);
//        webview.removeAllViews();
//        webview.stopLoading();
//        webview.clearCache(true);
//        webview.clearHistory();
//        webview.destroy();
//        webview.clearDisappearingChildren();
//        webview.clearFocus();
//        webview.clearFormData();
//        webview.clearMatches();
//        super.onDestroy();
//    }
//
//    private void getDataFromDB() {
//        new AsyncTask<Void, Void, List<Bean>>() {
//
//            @Override
//            protected List<Bean> doInBackground(Void... params) {
//                return SqliteHelper.instance(MainActivity_.this).queryRecentHistory();
//            }
//
//            @Override
//            protected void onPostExecute(List<Bean> searches) {
//                super.onPostExecute(searches);
//                if (searches != null) {
//                    if(!isActivityDestroyed()) {
//                        if (searches.size() > 0) {
//                            Bean delete = new Bean();
//                            delete.name = getString(R.string.clear_history);
//                            searches.add(delete);
//                            updateHintList(searches);
//                        } else {
//                            dismissHint();
//                        }
//                    }
//                }
//
//            }
//        }.execute();
//    }
//
//    private void updateHintList(List<Bean> searches) {
//        showHintList();
//        Message msg = Message.obtain();
//        msg.what = AsynWindowHandler.UPDATE_HINT_LIST_DATA;
//        msg.obj = searches;
//        windowHandler.sendMessage(msg);
//    }
//
//    private void showHintList() {
//        Message msg = Message.obtain();
//        msg.what = AsynWindowHandler.SHOW_HINT_LIST;
//        msg.obj = input;
//        windowHandler.sendMessage(msg);
//    }
//
//    private boolean dismissHint() {
//        boolean suc = windowHandler.isHintListShowing();
//        Message msg = Message.obtain();
//        msg.what = AsynWindowHandler.DISMISS_HINT_LIST;
//        windowHandler.sendMessage(msg);
//        return suc;
//    }
//
//    private byte[] readStream(InputStream inStream) throws Exception {
//        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024 * 2];
//        int len;
//        while ((len = inStream.read(buffer)) != -1) {
//            outStream.write(buffer, 0, len);
//        }
//        inStream.close();
//        return outStream.toByteArray();
//    }
//
//    private List<Bean> requestByGet(String path) throws Exception {
//        URL url = new URL(path);
//        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
//        urlConn.setConnectTimeout(5 * 1000);
//        urlConn.connect();
//
//        if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//            byte[] data = readStream(urlConn.getInputStream());
//            String result = new String(data, "GBK");
//            int start = result.indexOf("[") + 1;
//            int end = result.indexOf("]");
//            result = result.substring(start, end);
//            String[] strs = result.split(",");
//            int size = strs.length > 5 ? 5 : strs.length;
//            List<Bean> searches = new ArrayList<>();
//            for (int i = 0; i < size; i++) {
//                if (!TextUtils.isEmpty(strs[i])) {
//                    Bean search = new Bean();
//                    search.name = strs[i].replaceAll("\"", "");
//                    searches.add(search);
//                }
//            }
//            return searches;
//        }
//        urlConn.disconnect();
//        return null;
//    }
//
//    private void getDataFromWeb(final String path) {
//        new AsyncTask<Void, Void, List<Bean>>() {
//
//
//            @Override
//            protected List<Bean> doInBackground(Void... params) {
//                List<Bean> searches = null;
//                try {
//                    searches = requestByGet(path);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return searches;
//            }
//
//            @Override
//            protected void onPostExecute(List<Bean> searches) {
//                super.onPostExecute(searches);
//                if (searches != null) {
//                    if(!isActivityDestroyed()) {
//                        if (searches.size() > 0) {
//                            updateHintList(searches);
//                        } else {
//                            dismissHint();
//                        }
//                    }
//                }
//
//            }
//        }.execute();
//    }
//
//    private void doSearch(String word, boolean needShowHint) {
//        showHint = needShowHint;
//        input.setText(word);
//        input.setSelection(word.length());
//        if (!TextUtils.isEmpty(word)) {
//            String url = getEngineUrl(word);
//            if (!TextUtils.isEmpty(url)) {
//                webview.setOnTouchListener(null);
//                webview.loadUrl(url);
//                //hide input
//                InputMethodManager inputMethodManager =
//                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
//
//                //hide hintList
//                dismissHint();
//                saveData(word, url);
//            }
//        }
//    }
//
//    private String getEngineUrl(String word) {
//        if (word.startsWith("http:")
//                || word.startsWith("https:")) {
//            return word;
//        }
//        return String.format(webEngineUrls[currentWebEngine], getEncodeString(word));
//    }
//
//    private void saveData(final String word, final String url) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                Bean search = new Bean();
//                search.name = word;
//                search.time = System.currentTimeMillis();
//                search.url = url;
//                SqliteHelper.instance(MainActivity_.this).insertHistory(search);
//                return null;
//            }
//        }.execute();
//    }
//
    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.operate:
//                input.setText("");
//                setOptLevel(STATUS_SEARCH);
//                input.requestFocus();
//                openBoard();
//                break;
//            case R.id.hint_item:
//                Bean s = (Bean) v.getTag();
//
//                if (s != null) {
//                    if (getString(R.string.clear_history).equals(s.name)) {
//                        clearHistory();
//                    } else {
//                        doSearch(s.name ,false);
//                    }
//                }
//                break;
//            case R.id.input:
//                showHintList();
//                break;
//            case R.id.search:
//                doSearch(input.getText().toString().trim(), false);
//                break;
//            case R.id.menu:
//                popupMenu(v);
//                break;
//            case R.id.engine:
//                showEngineList();
//                break;
//            case R.id.engine_item:
//                int position = (int) v.getTag();
//                currentWebEngine = position;
//                getSharedPreferences("setting", MODE_PRIVATE).edit().putInt("engine", currentWebEngine).commit();
//                dismissEngineList();
//                refreshEngineIcon();
//                doSearch(input.getText().toString().trim(),false);
//                break;
//        }
    }
//
//    private void refreshEngineIcon() {
//        LevelListDrawable drawable = (LevelListDrawable) engine.getDrawable();
//        drawable.setLevel(currentWebEngine);
//    }
//
//    private void popupMenu(View menu) {
//        PopupMenu popup = new PopupMenu(this, menu);
//        if(TextUtils.isEmpty(webview.getUrl())) {
//            popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
//        }else {
//            popup.getMenuInflater().inflate(R.menu.web, popup.getMenu());
//        }
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.action_share:
//                        if (webview != null) {
//                            String url = webview.getUrl();
//                            Intent sendIntent = new Intent();
//                            sendIntent.setAction(Intent.ACTION_SEND);
//                            sendIntent.putExtra(Intent.EXTRA_TEXT, url);
//                            sendIntent.setType("text/plain");
//                            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
//                        }
//                        break;
//                    case R.id.action_setting:
//                        closeBoard();
//                        Intent intent = new Intent(MainActivity_.this, SettingActivity.class);
//                        startActivity(intent);
//                        break;
//                    case R.id.action_collect:
//                        Bean bean = new Bean();
//                        bean.name = webview.getTitle();
//                        bean.url = webview.getUrl();
//                        bean.time = System.currentTimeMillis();
//                        SqliteHelper.instance(MainActivity_.this).insertFav(bean);
//                        Toast.makeText(MainActivity_.this, R.string.favorite_txt, Toast.LENGTH_SHORT).show();
//                        break;
//                    case R.id.action_collection:
//                        closeBoard();
//                        startActivity(new Intent(MainActivity_.this, FavoriteActivity.class));
//                        break;
//                    case R.id.action_history:
//                        closeBoard();
//                        startActivity(new Intent(MainActivity_.this, HistoryActivity.class));
//                        break;
////                    case R.id.action_feedback:
////                        sendMailByIntent();
////                        break;
////                    case R.id.action_about:
////                        showAlertDialog(getString(R.string.action_about), getString(R.string.setting_about));
////                        break;
//                }
//                return true;
//            }
//        });
//        popup.show();
//    }
//
//    private void clearHistory() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                SqliteHelper.instance(MainActivity_.this).clearRecentHistory();
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                if(!isActivityDestroyed()) {
//                    updateHintList(new ArrayList<Bean>(1));
//                    dismissHint();
//                }
//            }
//        }.execute();
//    }
//
//    public void closeBoard() {
//        InputMethodManager imm = (InputMethodManager)
//                getSystemService(INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
//    }
//
//    private void openBoard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
//    }
//
//    private static class EngineAdapter extends BaseAdapter {
//
//        private final LayoutInflater factory;
//        MainActivity_ act;
//        private int[] drawableRes;
//
//        public EngineAdapter(int[] drawableRes, MainActivity_ act) {
//            this.act = act;
//            factory = LayoutInflater.from(act);
//            this.drawableRes = drawableRes;
//        }
//
//        @Override
//        public int getCount() {
//            return drawableRes.length;
//        }
//
//        @Override
//        public Integer getItem(int position) {
//            return drawableRes[position];
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            if (convertView == null) {
//                convertView = factory.inflate(R.layout.engine_item, parent, false);
//            }
//            int resId = getItem(position);
//            convertView.findViewById(R.id.icon).setBackgroundResource(resId);
//
//            String name = act.getResources().getStringArray(R.array.engine_web_names)[position];
//            TextView tv = (TextView) convertView.findViewById(R.id.name);
//            tv.setText(name);
//            convertView.setOnClickListener(act);
//            convertView.setTag(position);
//            return convertView;
//        }
//
//    }
}
