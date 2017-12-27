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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;

/**
 * 收藏夹fragment
 * Created by peter on 16/5/9.
 */
public class FavoriteFragment extends BookmarkFragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    @BindView(R.id.favorite)
    ListView favorite;
    @BindView(R.id.no_record)
    View noRecord;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindArray(R.array.favorite_urls)
    String[] urls;
    @BindArray(R.array.favorite_urls_names)
    String[] names;
    Disposable queryFavorite;
    View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                    Observable<List<TabData>> listObservable = DaoManager.getInstance().queryFavoriteLike(s);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(FavoriteFragment.this, rootView);
        refreshAllListData();
        return rootView;
    }

    private List<TabData> getDefaultFav() {
        List<TabData> list = new ArrayList<>(urls.length);
        for (int i = 0; i < urls.length; i++) {
            TabData bean = new TabData();
            bean.setTitle(names[i]);
            bean.setUrl(urls[i]);
            bean.setTime(-1);
            list.add(bean);
        }
        return list;
    }

    private void refreshAllListData() {
        cancelQuery();
        queryFavorite = DaoManager.getInstance().queryAllFavorite().subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(list -> {
            list.addAll(0, getDefaultFav());
            refreshListData(list);
        });
    }

    private void refreshListData(List<TabData> beans) {
        if (beans != null) {
            if (beans.size() == 0) {
                noRecord.setVisibility(View.VISIBLE);
            } else {
                noRecord.setVisibility(View.GONE);
            }
            if (favorite.getAdapter() == null) {
                favorite.setAdapter(new FavoriteAdapter(beans));
            } else {
                ((FavoriteAdapter) favorite.getAdapter()).updateData(beans);
            }
        }
        loading.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item:
                TabData bean = (TabData) v.getTag();
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
        if (queryFavorite != null) {
            if (!queryFavorite.isDisposed()) {
                queryFavorite.dispose();
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
                    TabData bean = (TabData) view.getTag();
                    DaoManager.getInstance().deleteFav(bean);
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

    private class FavoriteAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private List<TabData> list;

        FavoriteAdapter(List<TabData> objects) {
            factory = LayoutInflater.from(getActivity());
            list = objects;
        }

        void updateData(List<TabData> list) {
            this.list = list;
            notifyDataSetChanged();
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
            TextView view;

            if (convertView == null) {
                view = (TextView) factory.inflate(R.layout.item_list_website, parent, false);
            } else {
                view = (TextView) convertView;
            }

            TabData bean = getItem(position);
            view.setText(bean.getTitle());
            view.setOnClickListener(FavoriteFragment.this);
            if (containInnerName(bean) && containInnerUrl(bean)) {
                view.setOnLongClickListener(null);
            } else {
                view.setOnLongClickListener(FavoriteFragment.this);
            }
            view.setTag(bean);
            return view;
        }
    }

    private boolean containInnerName(TabData bean) {
        for (String name : names) {
            if (name.equals(bean.getTitle())) {
                return true;
            }
        }
        return false;
    }

    private boolean containInnerUrl(TabData bean) {
        for (String url : urls) {
            if (url.equals(bean.getUrl())) {
                return true;
            }
        }
        return false;
    }


}
