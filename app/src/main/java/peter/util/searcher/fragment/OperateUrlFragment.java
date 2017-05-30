package peter.util.searcher.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.utils.UrlUtils;


/**
 * Created by peter on 2016/11/24.
 */

public class OperateUrlFragment extends Fragment implements View.OnClickListener {

    View rootView;
    CharSequence word;
    @BindView(R.id.paste)
    View paste;
    @BindView(R.id.paste_txt)
    View pasteTxt;
    @BindView(R.id.enter)
    View enter;
    @BindView(R.id.enter_txt)
    View enterTxt;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_operate_url, container, false);
        ButterKnife.bind(this, rootView);
        paste.setOnClickListener(OperateUrlFragment.this);
        enter.setOnClickListener(OperateUrlFragment.this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        word = cmb.getText();
        if (TextUtils.isEmpty(word)) {
            paste.setEnabled(false);
            pasteTxt.setEnabled(false);
        } else {
            paste.setEnabled(true);
            pasteTxt.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        SearchActivity searchActivity = (SearchActivity) getActivity();
        String url = UrlUtils.getGuessUrl(searchActivity.getSearchWord());
        switch (v.getId()) {
            case R.id.paste:
                searchActivity.setSearchWord(word.toString());
                break;
            case R.id.enter:
                searchActivity.closeIME();
                searchActivity.finish();
                searchActivity.overridePendingTransition(0, 0);
                searchActivity.startBrowser(new Bean("", url));
                break;
        }
    }

}
