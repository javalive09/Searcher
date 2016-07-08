package peter.util.searcher.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import peter.util.searcher.update.AsynWindowHandler;
import peter.util.searcher.engine.EngineViewPagerFragment;
import peter.util.searcher.R;
import peter.util.searcher.engine.RecentSearchFragment;
import peter.util.searcher.update.UpdateController;
import peter.util.searcher.utils.UrlUtils;

/**
 * Created by peter on 16/5/19.
 */
public class EnterActivity extends BaseActivity implements DrawerLayoutAdapter.OnItemClickListener, View.OnClickListener {

    private EditText search;
    private ImageView opt;
    private int level = 0; // 0-voice 1-clear
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    private String currentFragmentTag = "";
    private RecognizerDialog iatDialog;

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
        iatDialog = new RecognizerDialog(EnterActivity.this, mInitListener);
        opt = (ImageView) findViewById(R.id.opt);
        search = (EditText) findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchWord = getSearchWord();
                    if (!TextUtils.isEmpty(searchWord)) {
                        String engineUrl = getString(R.string.default_engine_url);
                        String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                        startBrowser(url, searchWord);
                    }
                    search.requestFocus();
                    return true;
                }
                return false;
            }
        });
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
                    level = 0;
                    opt.setImageResource(R.drawable.abc_ic_voice_search_api_mtrl_alpha);
                } else if (!content.equals(temp)) {
                    setEngineFragment(ENGINE_LIST);
                    level = 1;
                    opt.setImageResource(R.drawable.abc_ic_clear_mtrl_alpha);
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
            ft.commit();
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
                switch (level) {
                    case 1://clear
                        search.requestFocus();
                        search.setText("");
                        openBoard();
                        break;
                    case 0://voice
                        SpeechUtility.createUtility(EnterActivity.this, SpeechConstant.APPID + "=5775fcb4");
                        //语音初始化和设置组件
                        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(EnterActivity.this, null);
                        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
                        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
                        iatDialog.setListener(recognizerDialogListener);
                        iatDialog.show();
                        break;
                }


                break;
        }
    }

    //Listener for dialog
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("peter123", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(EnterActivity.this, code + "  error !!!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Dialog监听器
     */
    private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean b) {

            if (recognizerResult != null) {
                String json = recognizerResult.getResultString();
                if (!TextUtils.isEmpty(json)) {
                    String result = parseIatResult(json);
                    setSearchWord(result);
                    iatDialog.dismiss();
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
