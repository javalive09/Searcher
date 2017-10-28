package peter.util.searcher.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.bean.TabBean;
import peter.util.searcher.utils.UrlUtils;


/**
 * url操作fragment
 * Created by peter on 2016/11/24.
 */

public class OperateUrlFragment extends Fragment implements View.OnClickListener {

    View rootView;
    @BindView(R.id.enter)
    View pasteEnter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_operate_url, container, false);
        ButterKnife.bind(this, rootView);
        pasteEnter.setOnClickListener(OperateUrlFragment.this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        SearchActivity searchActivity = (SearchActivity) getActivity();
        String url = UrlUtils.getGuessUrl(searchActivity.getSearchWord());
        switch (v.getId()) {
            case R.id.enter:
                searchActivity.closeIME();
                searchActivity.finish();
                searchActivity.overridePendingTransition(0, 0);
                searchActivity.startBrowser(new TabBean("", url));
                break;
        }
    }

}
