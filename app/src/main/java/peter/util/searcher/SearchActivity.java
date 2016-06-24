package peter.util.searcher;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
public class SearchActivity extends BaseActivity {

    private EditText search;
    private ImageView clear;
    private static final int RECENT_SEARCH = 1;
    private static final int ENGINE_LIST = 2;
    private static final int WEB_SITES = 3;
    private int currentFragment = -1;

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
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.requestFocus();
                search.setText("");
                openBoard();
            }
        });
        search = (EditText) findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchWord = getSearchWord();
                    if(!TextUtils.isEmpty(searchWord)) {
                        String engineUrl = getString(R.string.default_engine_url);
                        String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
                        startBrowser(SearchActivity.this, url, searchWord);
                        finish();
                        return true;
                    }
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
                    setEngineFragment(WEB_SITES);
                    clear.setVisibility(View.INVISIBLE);
                } else if (!content.equals(temp)) {
                    setEngineFragment(ENGINE_LIST);
                    clear.setVisibility(View.VISIBLE);
                }
            }
        });
        setEngineFragment(WEB_SITES);
    }

    private void setEngineFragment(int f) {
        if (currentFragment != f) {
            currentFragment = f;
            Fragment fragment = null;
            switch (f) {
                case RECENT_SEARCH:
                    fragment = new RecentSearchFragment();
                    break;
                case ENGINE_LIST:
                    fragment = new EngineViewPagerFragment();
                    break;
                case WEB_SITES:
                    fragment = new CommonWebSiteFragment();
                    break;
            }
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }

}
