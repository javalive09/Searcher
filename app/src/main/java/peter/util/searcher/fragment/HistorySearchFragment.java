package peter.util.searcher.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.DaoManager;

/**
 * 搜索记录fragment
 * Created by peter on 16/5/9.
 */
public class HistorySearchFragment extends BookmarkFragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    @BindView(R.id.loading_history_search)
    ProgressBar loading;
    @BindView(R.id.no_record)
    TextView noRecord;
    @BindView(R.id.history_search)
    ListView history;
    Disposable queryHistory;
    Disposable queryFavorite;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history_search, container, false);
        ButterKnife.bind(HistorySearchFragment.this, rootView);
        refreshAllListData();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmark_favorite, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint(getString(R.string.action_bookmark_search_favorite_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    refreshAllListData();
                } else {
                    Observable<List<Bean>> listObservable = DaoManager.getInstance().queryHistoryLike(s);
                    cancelQuery();
                    queryFavorite = listObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                            subscribe(list -> refreshListData(list));
                }
                return true;
            }
        });
        SearchView.SearchAutoComplete mSearchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelOffset(R.dimen.search_text_size));
        mSearchAutoComplete.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                needCloseSearchView();
            }
        });
    }

    private void refreshAllListData() {
        cancelQuery();
        queryHistory = DaoManager.getInstance().queryAllHistory().subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(this::refreshListData);
    }

    private void refreshListData(List<Bean> beans) {
        if (beans != null) {
            if (beans.size() == 0) {
                noRecord.setVisibility(View.VISIBLE);
            } else {
                noRecord.setVisibility(View.GONE);
            }
            if (history.getAdapter() == null) {
                history.setAdapter(new HistoryAdapter(beans));
            } else {
                ((HistoryAdapter) history.getAdapter()).updateData(beans);
            }
        }
        loading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    ((BaseActivity) getActivity()).startBrowser(bean);
                }
                break;
        }
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

    @Override
    public void onDestroy() {
        dismissPopupMenu();
        cancelQuery();
        super.onDestroy();
    }

    private void cancelQuery() {
        if (queryHistory != null) {
            if (!queryHistory.isDisposed()) {
                queryHistory.dispose();
            }
        }
    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    Bean bean = (Bean) view.getTag();
                    DaoManager.getInstance().deleteHistory(bean);
                    refreshAllListData();
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

    private class HistoryAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private List<Bean> list;

        HistoryAdapter(List<Bean> objects) {
            factory = LayoutInflater.from(getActivity());
            list = objects;
        }

        void updateData(List<Bean> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Bean getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;

            if (convertView == null) {
                view = (TextView) factory.inflate(R.layout.item_list_website, parent, false);
            } else {
                view = (TextView) convertView;
            }

            Bean search = getItem(position);
            view.setText(search.name);
            view.setOnClickListener(HistorySearchFragment.this);
            view.setOnLongClickListener(HistorySearchFragment.this);
            view.setTag(search);
            return view;
        }

    }


}
