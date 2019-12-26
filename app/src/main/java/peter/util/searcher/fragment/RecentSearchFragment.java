package peter.util.searcher.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.databinding.FragmentRecentSearchBinding;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.utils.UrlUtils;

/**
 * 最近搜索fragment
 * Created by peter on 16/5/9.
 */
public class RecentSearchFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    CharSequence word;
    Disposable queryRecent;
    FragmentRecentSearchBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent_search, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
        ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        if(cmb.getPrimaryClip() != null) {
            if (cmb.getPrimaryClip().getItemCount() > 0) {
                word = cmb.getPrimaryClip().getItemAt(0).getText();
                if (!TextUtils.isEmpty(word)) {
                    if (UrlUtils.guessUrl(word.toString())) {
                        binding.pasteEnter.setVisibility(View.VISIBLE);
                        binding.pasteEnter.setOnClickListener(RecentSearchFragment.this);
                    } else {
                        binding.paste.setVisibility(View.VISIBLE);
                        binding.paste.setOnClickListener(RecentSearchFragment.this);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        TabData bean = (TabData) v.getTag();
        SearchActivity searchActivity = (SearchActivity) getActivity();
        switch (v.getId()) {
            case R.id.item:
                if (bean != null) {
                    searchActivity.setSearchWord(bean.getTitle());
                }
                break;
            case R.id.paste:
                searchActivity.setSearchWord(word.toString());
                break;
            case R.id.paste_enter:
                searchActivity.closeIME();
                searchActivity.finish();
                searchActivity.overridePendingTransition(0, 0);
                TabData tabData = new TabData();
                tabData.setUrl(word.toString());
                searchActivity.startBrowser(tabData);
                break;
        }
    }

    private void refreshData() {
        queryRecent = DaoManager.getInstance().queryRecentData(9).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                subscribe(beans -> {
                    binding.loading.setVisibility(View.GONE);
                    if (beans != null) {
                        if (beans.size() > 0) {
                            binding.recentSearch.setAdapter(new RecentSearchAdapter(beans));
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        dismissPopupMenu();
        if (queryRecent != null) {
            if (!queryRecent.isDisposed()) {
                queryRecent.dispose();
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.item:
                popupMenu(v);
                return true;
        }
        return false;
    }

    private class RecentSearchAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private final List<TabData> list;

        RecentSearchAdapter(List<TabData> list) {
            factory = LayoutInflater.from(getActivity());
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public TabData getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = factory.inflate(R.layout.item_list_recentsearch, parent, false);
            }
            TextView content = (TextView) convertView;
            TabData search = getItem(position);
            content.setText(search.getTitle());
            content.setOnClickListener(RecentSearchFragment.this);
            content.setOnLongClickListener(RecentSearchFragment.this);
            content.setTag(search);
            return convertView;
        }
    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    TabData bean = (TabData) view.getTag();
                    DaoManager.getInstance().deleteHistory(bean);
                    refreshData();
                    break;
            }
            return true;
        });
        popup.show();
    }

    private void dismissPopupMenu() {
        if (popup != null) {
            popup.dismiss();
        }
    }

}
