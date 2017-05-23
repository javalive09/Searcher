package peter.util.searcher.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.utils.UrlUtils;


/**
 * Created by peter on 2016/11/24.
 */

public class OperateUrlFragment2 extends BaseFragment implements View.OnClickListener {

    View rootView;
    CharSequence word;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_operate_url2, container, false);
        rootView.findViewById(R.id.paste).setOnClickListener(OperateUrlFragment2.this);
        rootView.findViewById(R.id.enter).setOnClickListener(OperateUrlFragment2.this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ClipboardManager cmb = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        word = cmb.getText();
        if(TextUtils.isEmpty(word)) {
            rootView.findViewById(R.id.paste).setEnabled(false);
        }else {
            rootView.findViewById(R.id.paste).setEnabled(true);
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
                searchActivity.startBrowser(url, "");
                break;
        }
    }

}
