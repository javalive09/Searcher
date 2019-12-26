package peter.util.searcher.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.umeng.analytics.MobclickAgent;
import peter.util.searcher.R;
import peter.util.searcher.databinding.ActivitySearchBinding;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.fragment.EngineInfoViewPagerFragment;
import peter.util.searcher.fragment.OperateUrlFragment;
import peter.util.searcher.fragment.RecentSearchFragment;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.utils.UrlUtils;

/**
 * 搜索页activity
 * Created by peter on 16/5/19.
 */
public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding binding;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    public static final String OPERATE_URL = "operate_url";
    private String currentFragmentTag = "";
    private TabData tabData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        init();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    private void checkData(Intent intent) {
        this.tabData = (TabData) intent.getSerializableExtra(NAME_TAB_DATA);
        if (!TextUtils.isEmpty(tabData.getSearchWord()) && !tabData.getSearchWord().contains(Tab.LOCAL_SCHEMA)) {
            setSearchWord(tabData.getSearchWord());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String searchWord = getSearchWord();
        if (!TextUtils.isEmpty(searchWord)) {
            outState.putString(NAME_WORD, searchWord);
        }
    }

    @Override
    public String getSearchWord() {
        return binding.topTxt.getText().toString().trim();
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

    public TabData getTabData() {
        return tabData;
    }

    @Override
    public void setSearchWord(String word) {
        binding.topTxt.setText(word);
        if (!TextUtils.isEmpty(word)) {
            int position = word.length();
            binding.topTxt.setSelection(position);
        }
    }

    private void init() {
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // this sets the button to the back icon
            actionBar.setHomeButtonEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> {
            closeIME();
            finish();
        });

        binding.topTxt.addTextChangedListener(new TextWatcher() {

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
                    binding.clearAll.setVisibility(View.GONE);
                } else {
                    binding.clearAll.setVisibility(View.VISIBLE);
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

        binding.clearAll.setOnClickListener(v -> {
            openIME();
            binding.topTxt.requestFocus();
            binding.topTxt.setText("");
        });

        binding.topTxt.postDelayed(() -> {
            binding.topTxt.requestFocus();
            openIME();
        }, 500);
    }

    public void closeIME() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void openIME() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.showSoftInput(binding.topTxt, InputMethodManager.SHOW_IMPLICIT);
        }
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
                bundle.putSerializable(NAME_TAB_DATA, tabData);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.content_frame, fragment, tag);
                ft.commitAllowingStateLoss();
            }
        }
    }

}
