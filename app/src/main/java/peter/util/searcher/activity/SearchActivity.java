package peter.util.searcher.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import peter.util.searcher.R;
import peter.util.searcher.bean.TabBean;
import peter.util.searcher.fragment.EngineInfoViewPagerFragment;
import peter.util.searcher.fragment.OperateUrlFragment;
import peter.util.searcher.fragment.RecentSearchFragment;
import peter.util.searcher.utils.UrlUtils;

/**
 * 搜索页activity
 * Created by peter on 16/5/19.
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.top_txt)
    EditText search;
    @BindView(R.id.clearAll)
    ImageView clearAll;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    public static final String OPERATE_URL = "operate_url";
    private String currentFragmentTag = "";
    private TabBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(SearchActivity.this);
        init();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    private void checkData(Intent intent) {
        this.bean = intent.getParcelableExtra(NAME_BEAN);
        if (!TextUtils.isEmpty(bean.name) && !bean.name.contains(peter.util.searcher.tab.Tab.LOCAL_SCHEMA)) {
            setSearchWord(bean.name);
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
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // this sets the button to the back icon
            actionBar.setHomeButtonEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            closeIME();
            finish();
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
                    clearAll.setVisibility(View.GONE);
                } else {
                    clearAll.setVisibility(View.VISIBLE);
                    if (UrlUtils.guessUrl(content)) {
                        setEngineFragment(OPERATE_URL);
                    } else {
                        setEngineFragment(ENGINE_LIST);
                    }
                }
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
            Fragment fragment = null;
            switch (tag) {
                case RECENT_SEARCH:
                    fragment = new RecentSearchFragment();
                    break;
                case ENGINE_LIST:
                    fragment = new EngineInfoViewPagerFragment();
                    break;
                case OPERATE_URL:
                    fragment = new OperateUrlFragment();
                    break;
            }

            if (fragment != null) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(NAME_BEAN, bean);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.content_frame, fragment, tag);
                ft.commitAllowingStateLoss();
            }
        }
    }

    @OnClick(R.id.clearAll)
    public void onClick(View v) {
        openIME();
        search.requestFocus();
        search.setText("");
    }


}
