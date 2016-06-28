package peter.util.searcher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebViewFragment;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by peter on 16/5/19.
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private EditText search;
    private ImageView clear;
    private static final String RECENT_SEARCH = "recent_search";
    public static final String ENGINE_LIST = "engine_list";
    private static final String WEB_SITES = "web_sites";
    private static final String WEB_HINT = "web_hint";
    private String currentFragmentTag = "";
    private Fragment currentFragment;

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

    public void setSearchWord(String word) {
        search.setText(word);
        if (!TextUtils.isEmpty(word)) {
            int position = word.length();
            search.setSelection(position);
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void openBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
    }

    public String getSearchWord() {
        return search.getText().toString().trim();
    }

    private void init() {
        clear = (ImageView) findViewById(R.id.clear);
        search = (EditText) findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchWord = getSearchWord();
                    if (!TextUtils.isEmpty(searchWord)) {
                        if (!ENGINE_LIST.equals(currentFragmentTag)) {
                            setEngineFragment(ENGINE_LIST);
                        } else {
                            String engineUrl = getString(R.string.default_engine_url);
                            String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                            startBrowserFromSearch(SearchActivity.this, url, searchWord);
                        }
                    }
                    search.requestFocus();
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
                    setEngineFragment(RECENT_SEARCH);
                    clear.setVisibility(View.INVISIBLE);
                } else if (!content.equals(temp)) {
                    setEngineFragment(WEB_HINT);
                    WebHintFragment f = (WebHintFragment) currentFragment;
                    f.refreshData(content);
                    clear.setVisibility(View.VISIBLE);
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
            } else if (tag.equals(WEB_SITES)) {
                fragment = new CommonWebSiteFragment();
            } else if (tag.equals(WEB_HINT)) {
                fragment = new WebHintFragment();
            }
            currentFragment = fragment;
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment, tag);
            ft.commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                search.requestFocus();
                search.setText("");
                openBoard();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }
}