package peter.util.searcher.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.umeng.analytics.MobclickAgent;

import peter.util.searcher.R;
import peter.util.searcher.VoiceRecognizer;
import peter.util.searcher.fragment.BaseFragment;
import peter.util.searcher.fragment.EngineViewPagerFragment;
import peter.util.searcher.fragment.OperateUrlFragment2;
import peter.util.searcher.fragment.RecentSearchFragment;
import peter.util.searcher.tab.Tab;
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
    private BaseFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    private void checkData(Intent intent) {
        String content = intent.getStringExtra(NAME_WORD);
        if (!TextUtils.isEmpty(content) && !content.contains(Tab.LOCAL_SCHEMA)) {
            setSearchWord(content);
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
                    } else {
                        setEngineFragment(ENGINE_LIST);
                    }
                }
                currentFragment.refresh();
            }
        });
        setEngineFragment(RECENT_SEARCH);

        Intent intent = getIntent();
        checkData(intent);
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
            BaseFragment fragment = null;
            if (tag.equals(RECENT_SEARCH)) {
                fragment = new RecentSearchFragment();
            } else if (tag.equals(ENGINE_LIST)) {
                fragment = new EngineViewPagerFragment();
            } else if (tag.equals(OPERATE_URL)) {
                fragment = new OperateUrlFragment2();
            }
            currentFragment = fragment;
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
                        VoiceRecognizer.instance().showVoiceDialog(SearchActivity.this, mRecognizerDialogListener);
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

    /**
     * Dialog监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(com.iflytek.cloud.RecognizerResult recognizerResult, boolean isLast) {

            if (recognizerResult != null) {
                String json = recognizerResult.getResultString();
                if (!TextUtils.isEmpty(json)) {
                    String result = VoiceRecognizer.instance().parseIatResult(json);
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


}
