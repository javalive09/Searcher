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
import android.view.MotionEvent;
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

import peter.util.searcher.R;
import peter.util.searcher.fragment.EngineViewPagerFragment;
import peter.util.searcher.fragment.OperateUrlFragment;
import peter.util.searcher.fragment.RecentSearchFragment;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.update.AsynWindowHandler;
import peter.util.searcher.update.UpdateController;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.view.ObservableEditText;

/**
 * Created by peter on 16/5/19.
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private ObservableEditText search;
    private ImageView opt;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    public static final String OPERATE_URL = "operate_url";
    private String currentFragmentTag = "";

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    private void checkData(Intent intent) {
        String url = intent.getStringExtra(NAME_URL);
        if(!url.contains(Tab.LOCAL_SCHEMA)) {
            setSearchWord(url);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String searchWord = getSearchWord();
        if (!TextUtils.isEmpty(searchWord)) {
            outState.putString(NAME_WORD, searchWord);
        }
    }

    @Override
    public String getSearchWord() {
        return search.getText().toString().trim();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String searchWord = savedInstanceState.getString(NAME_WORD);
        if (!TextUtils.isEmpty(searchWord)) {
            setSearchWord(searchWord);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkData(intent);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void setSearchWord(String word) {
        search.setText(word);
        if (!TextUtils.isEmpty(word)) {
            int position = word.length();
            search.setSelection(position);
        }
    }

    private void init() {
        mIat = SpeechRecognizer.createRecognizer(SearchActivity.this, mInitListener);
        mIatDialog = new RecognizerDialog(SearchActivity.this, mInitListener);
        mIatDialog.setListener(mRecognizerDialogListener);
        setParam();

        opt = (ImageView) findViewById(R.id.opt);
        search = (ObservableEditText) findViewById(R.id.search);
        search.setBackPressCallBack(new ObservableEditText.BackPressCallBack() {
            @Override
            public void backPress() {
                closeIME();
                finish();
            }
        });
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
                } else {
                    opt.getDrawable().setLevel(1);
                    if (UrlUtils.guessUrl(content)) {
                        setEngineFragment(OPERATE_URL);
                    }else {
                        setEngineFragment(ENGINE_LIST);
                    }
                }
            }
        });
        setEngineFragment(RECENT_SEARCH);

        Intent intent = getIntent();
        if (intent != null) {
            String searchWord = intent.getStringExtra(NAME_WORD);
            if (!TextUtils.isEmpty(searchWord)) {
                setSearchWord(searchWord);
            } else {
                checkData(intent);
            }
        }
    }

    public void closeIME() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
    }

    public void setEngineFragment(String tag) {
        if (!currentFragmentTag.equals(tag)) {
            currentFragmentTag = tag;
            Fragment fragment = null;
            if (tag.equals(RECENT_SEARCH)) {
                fragment = new RecentSearchFragment();
            } else if (tag.equals(ENGINE_LIST)) {
                fragment = new EngineViewPagerFragment();
            } else if (tag.equals(OPERATE_URL)) {
                fragment = new OperateUrlFragment();
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(SearchActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        mIatDialog.show();
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(SearchActivity.this, R.string.record_audio_permission, Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Listener for dialog
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "error code " + code, Toast.LENGTH_SHORT).show();
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
