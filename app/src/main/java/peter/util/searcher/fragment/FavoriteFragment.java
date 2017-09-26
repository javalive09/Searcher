package peter.util.searcher.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
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

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.activity.BookMarkActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.DaoManager;

/**
 * 收藏夹fragment
 * Created by peter on 16/5/9.
 */
public class FavoriteFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

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
    SearchView mSearchView;
    Disposable queryFavorite;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(FavoriteFragment.this, rootView);
        installFavUrl();
        return rootView;
    }

    private void installFavUrl() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                for (int i = 0; i < urls.length; i++) {
                    Bean bean = new Bean();
                    bean.name = names[i];
                    bean.url = urls[i];
                    bean.time = -1;
                    DaoManager.getInstance().insertFavorite(bean);
                }
                return null;
            }
        }.execute();

    }

    private void refreshListData() {
        queryFavorite = DaoManager.getInstance().queryAllFavorite().subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).subscribe(beans -> {
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
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshListData();
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
        if (queryFavorite != null) {
            if (!queryFavorite.isDisposed()) {
                queryFavorite.dispose();
            }
        }
        super.onDestroy();
    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        Bean bean = (Bean) view.getTag();
                        DaoManager.getInstance().deleteFav(bean);
                        refreshListData();
                        break;
                }

                return true;
            }
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
        private List<Bean> list;

        public FavoriteAdapter(List<Bean> objects) {
            factory = LayoutInflater.from(getActivity());
            list = objects;
        }

        public void updateData(List<Bean> list) {
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

            Bean bean = getItem(position);
            view.setText(bean.name);
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

    private boolean containInnerName(Bean bean) {
        for (String name : names) {
            if (name.equals(bean.name)) {
                return true;
            }
        }
        return false;
    }

    private boolean containInnerUrl(Bean bean) {
        for (String url : urls) {
            if (url.equals(bean.url)) {
                return true;
            }
        }
        return false;
    }


}
