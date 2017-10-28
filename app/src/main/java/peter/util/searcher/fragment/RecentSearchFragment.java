package peter.util.searcher.fragment;

import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.bean.TabBean;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.utils.UrlUtils;

/**
 * 最近搜索fragment
 * Created by peter on 16/5/9.
 */
public class RecentSearchFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    CharSequence word;
    @BindView(R.id.paste_enter)
    View pasteEnter;
    @BindView(R.id.paste)
    View paste;
    @BindView(R.id.loading)
    View loading;
    @BindView(R.id.recent_search)
    ListView recentSearch;
    Disposable queryRecent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recent_search, container, false);
        ButterKnife.bind(RecentSearchFragment.this, rootView);
        return rootView;
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
                        pasteEnter.setVisibility(View.VISIBLE);
                        pasteEnter.setOnClickListener(RecentSearchFragment.this);
                    } else {
                        paste.setVisibility(View.VISIBLE);
                        paste.setOnClickListener(RecentSearchFragment.this);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        TabBean bean = (TabBean) v.getTag();
        SearchActivity searchActivity = (SearchActivity) getActivity();
        switch (v.getId()) {
            case R.id.item:
                if (bean != null) {
                    searchActivity.setSearchWord(bean.name);
                }
                break;
            case R.id.paste:
                searchActivity.setSearchWord(word.toString());
                break;
            case R.id.paste_enter:
                searchActivity.closeIME();
                searchActivity.finish();
                searchActivity.overridePendingTransition(0, 0);
                searchActivity.startBrowser(new TabBean("", word.toString()));
                break;
        }
    }

    private void refreshData() {
        queryRecent = DaoManager.getInstance().queryRecentData(9).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                subscribe(beans -> {
                    loading.setVisibility(View.GONE);
                    if (beans != null) {
                        if (beans.size() > 0) {
                            recentSearch.setAdapter(new RecentSearchAdapter(beans));
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
        private final List<TabBean> list;

        RecentSearchAdapter(List<TabBean> list) {
            factory = LayoutInflater.from(getActivity());
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public TabBean getItem(int position) {
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
            TabBean search = getItem(position);
            content.setText(search.name);
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
                    TabBean bean = (TabBean) view.getTag();
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
