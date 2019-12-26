package peter.util.searcher.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.databinding.FavoriteItemRecyclerViewBinding;
import peter.util.searcher.databinding.FragmentFavoriteBinding;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.utils.Utils;

/**
 * 收藏夹fragment
 * Created by peter on 16/5/9.
 */
public class FavoriteFragment extends BookmarkFragment implements View.OnClickListener, View.OnLongClickListener {

    FragmentFavoriteBinding binding;
    PopupMenu popup;
    String[] urls;
    String[] names;
    Disposable queryFavorite;
    FavoriteAdapter favoriteAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        urls = getResources().getStringArray(R.array.favorite_urls);
        names = getResources().getStringArray(R.array.favorite_urls_names);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false);
        refreshAllListData();
        return binding.getRoot();
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
                binding.noRecord.setVisibility(View.VISIBLE);
            } else {
                binding.noRecord.setVisibility(View.GONE);
            }
            if (binding.favorite.getAdapter() == null) {
                final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                binding.favorite.setLayoutManager(linearLayoutManager);
                binding.favorite.setAdapter(favoriteAdapter = new FavoriteAdapter(beans));
            } else {
                ((FavoriteAdapter) binding.favorite.getAdapter()).updateData(beans);
            }
        }
        binding.loading.setVisibility(View.GONE);
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
                    TabData tabData = (TabData) view.getTag();
                    DaoManager.getInstance().deleteFav(tabData);
                    int position = (int) view.getTag(R.id.favorite_item_position_tag);
                    favoriteAdapter.remove(position);
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

    private class FavoriteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

        void remove(int position) {
            list.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FavoriteItemRecyclerViewBinding binding = DataBindingUtil.inflate(factory, R.layout.favorite_item_recycler_view, parent, false);
            return new RecyclerViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof RecyclerViewHolder) {
                final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;

                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_recycler_item_show);
                recyclerViewHolder.binding.item.startAnimation(animation);

                TabData tabData = list.get(position);

                recyclerViewHolder.binding.title.setText(tabData.getTitle());

                Drawable drawable;
                if (tabData.getIcon() != null) {
                    drawable = new BitmapDrawable(getResources(), Utils.Bytes2Bitmap(tabData.getIcon()));
                } else {
                    drawable = getResources().getDrawable(R.drawable.ic_website);
                }
                recyclerViewHolder.binding.icon.setBackground(drawable);
                recyclerViewHolder.binding.item.setTag(tabData);
                recyclerViewHolder.binding.item.setTag(R.id.favorite_item_position_tag, position);

                if (containInnerName(tabData) && containInnerUrl(tabData)) {
                    recyclerViewHolder.binding.item.setOnLongClickListener(null);
                } else {
                    recyclerViewHolder.binding.item.setOnLongClickListener(FavoriteFragment.this);
                }
                recyclerViewHolder.binding.item.setOnClickListener(FavoriteFragment.this);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private FavoriteItemRecyclerViewBinding binding;

        private RecyclerViewHolder(FavoriteItemRecyclerViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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
