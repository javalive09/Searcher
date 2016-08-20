package peter.util.searcher.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import peter.util.searcher.download.DownloadHandler;
import peter.util.searcher.update.AsynWindowHandler;
import peter.util.searcher.engine.EngineViewPagerFragment;
import peter.util.searcher.R;
import peter.util.searcher.engine.RecentSearchFragment;
import peter.util.searcher.update.UpdateController;

/**
 * Created by peter on 16/5/19.
 */
public class EnterActivity extends BaseActivity implements DrawerLayoutAdapter.OnItemClickListener, View.OnClickListener {

    private EditText search;
    private ImageView opt;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    private String currentFragmentTag = "";
    private static final String WEATHER_URL = "http://e.weather.com.cn/d/index/101010100.shtml";
    private static final String HISTORY_TODAY_URL = "http://wap.lssdjt.com/";
    private static final String NEWS_URL = "http://3g.163.com/touch/news";
    private static final String NAV_URL = "http://3g.hao123.com/";

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        init();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String searchWord = getSearchWord();
        if(!TextUtils.isEmpty(searchWord)) {
            outState.putString("searchWord", searchWord);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String searchWord = savedInstanceState.getString("searchWord");
        if(!TextUtils.isEmpty(searchWord)) {
            setSearchWord(searchWord);
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void setSearchWord(String word) {
        search.setText(word);
        if (!TextUtils.isEmpty(word)) {
            int position = word.length();
            search.setSelection(position);
        }
    }

    public String getSearchWord() {
        return search.getText().toString().trim();
    }

    private void init() {
        mIat = SpeechRecognizer.createRecognizer(EnterActivity.this, mInitListener);
        mIatDialog = new RecognizerDialog(EnterActivity.this, mInitListener);
        mIatDialog.setListener(mRecognizerDialogListener);
        setParam();

        opt = (ImageView) findViewById(R.id.opt);
        search = (EditText) findViewById(R.id.search);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        RecyclerView mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        mDrawerList.setAdapter(new DrawerLayoutAdapter(getData(), this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                EnterActivity.this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                findViewById(R.id.title).setVisibility(View.GONE);
                search.setVisibility(View.VISIBLE);
                search.requestFocus();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }

            public void onDrawerOpened(View drawerView) {
                findViewById(R.id.title).setVisibility(View.VISIBLE);
                search.setVisibility(View.GONE);
                hideBoard();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(EnterActivity.this));
            }
        }, 200);
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
                    setEngineFragment(RECENT_SEARCH);
                    opt.getDrawable().setLevel(0);
                } else if (!content.equals(temp)) {
                    setEngineFragment(ENGINE_LIST);
                    opt.getDrawable().setLevel(1);
                }
            }
        });
        setEngineFragment(RECENT_SEARCH);
    }

    public void setEngineFragment(String tag) {
        if (!currentFragmentTag.equals(tag)) {
            currentFragmentTag = tag;
            Fragment fragment = null;
            if (tag.equals(RECENT_SEARCH)) {
                fragment = new RecentSearchFragment();
            } else if (tag.equals(ENGINE_LIST)) {
                fragment = new EngineViewPagerFragment();
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private ArrayList<DrawerLayoutAdapter.TypeBean> getData() {
        ArrayList<DrawerLayoutAdapter.TypeBean> list = new ArrayList<>();

        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.VERSION));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_history_today, getString(R.string.history_today_title), HISTORY_TODAY_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.news_163, getString(R.string.news_163), NEWS_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.hot_list_id_week_weather, getString(R.string.weeks_weather), WEATHER_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.HOT_LIST, R.id.nav, getString(R.string.web_guide), NAV_URL));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_favorite, getString(R.string.action_collection), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_history, getString(R.string.action_history), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_url_history, getString(R.string.action_url_history), ""));
        list.add(new DrawerLayoutAdapter.TypeBean(DrawerLayoutAdapter.CUSTOM, R.id.hot_list_setting, getString(R.string.setting_title), ""));
        return list;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view, DrawerLayoutAdapter.TypeBean bean) {
        switch (bean.type) {
            case DrawerLayoutAdapter.CUSTOM:
                switch (bean.id) {
                    case R.id.hot_list_favorite:
                        startActivity(new Intent(EnterActivity.this, FavoriteActivity.class));
                        break;
                    case R.id.hot_list_history:
                        startActivity(new Intent(EnterActivity.this, HistoryActivity.class));
                        break;
                    case R.id.hot_list_url_history:
                        startActivity(new Intent(EnterActivity.this, HistoryURLActivity.class));
                        break;
                    case R.id.hot_list_setting:
                        startActivity(new Intent(EnterActivity.this, SettingActivity.class));
                        break;
                }
                break;
            case DrawerLayoutAdapter.HOT_LIST:
                switch (bean.id) {
                    case R.id.news_163:
                        startBrowser(bean.url, "");
                        break;
                    case R.id.hot_list_history_today:
                        startBrowser(bean.url, "");
                        break;
                    case R.id.hot_list_id_week_weather:
                        startBrowser(bean.url, "");
                        break;
                    case R.id.nav:
                        startBrowser(bean.url, "");
                        break;
                }
                break;
            case DrawerLayoutAdapter.VERSION:
                break;
        }
    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0); //强制隐藏键盘
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.opt:

                switch (opt.getDrawable().getLevel()) {
                    case 0:
                        showVoiceDialog();
                        break;
                    case 1:
                        search.requestFocus();
                        search.setText("");
                        openBoard();
                        break;
                }
                break;
        }
    }

    private void showVoiceDialog() {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(EnterActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        mIatDialog.show();
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(EnterActivity.this, R.string.record_audio_permission, Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Listener for dialog
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(EnterActivity.this, "error code " + code, Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void setParam() {
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

    }

    /**
     * Dialog监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean isLast) {

            if (recognizerResult != null) {
                String json = recognizerResult.getResultString();
                if (!TextUtils.isEmpty(json)) {
                    String result = parseIatResult(json);
                    if (!TextUtils.isEmpty(result)) {
                        setSearchWord(result);
                    }
                }
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };

    private String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }
}
