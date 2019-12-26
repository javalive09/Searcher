package peter.util.searcher.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.databinding.FragmentOperateUrlBinding;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.utils.UrlUtils;


/**
 * url操作fragment
 * Created by peter on 2016/11/24.
 */

public class OperateUrlFragment extends Fragment implements View.OnClickListener {

    FragmentOperateUrlBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_operate_url, container, false);
        binding.enter.setOnClickListener(OperateUrlFragment.this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        SearchActivity searchActivity = (SearchActivity) getActivity();
        if(searchActivity != null) {
            String word = searchActivity.getSearchWord();
            if (!TextUtils.isEmpty(word)) {
                searchActivity.closeIME();
                searchActivity.finish();
                searchActivity.overridePendingTransition(0, 0);
                TabData tabData = new TabData();
                tabData.setUrl(UrlUtils.getGuessUrl(word));
                searchActivity.startBrowser(tabData);
            }
        }
    }

}
