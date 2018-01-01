package peter.util.searcher.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

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
import peter.util.searcher.utils.Utils;

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
    RecyclerView history;
    Disposable queryHistory;
    HistoryAdapter historyAdapter;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_search, container, false);
        ButterKnife.bind(HistorySearchFragment.this, view);
        refreshAllListData();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmark_history, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint(getString(R.string.action_bookmark_search_history_hint));
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
                    Observable<List<TabData>> listObservable = DaoManager.getInstance().queryHistoryLike(s);
                    cancelQuery();
                    queryHistory = listObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
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

    private void refreshListData(List<TabData> beans) {
        if (beans != null) {
            if (beans.size() == 0) {
                noRecord.setVisibility(View.VISIBLE);
            } else {
                noRecord.setVisibility(View.GONE);
            }
            if (history.getAdapter() == null) {
                final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                history.setLayoutManager(linearLayoutManager);
                history.setAdapter(historyAdapter = new HistoryAdapter(beans));
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
                    TabData bean = (TabData) view.getTag();
                    DaoManager.getInstance().deleteHistory(bean);
                    int position = (int) view.getTag(R.id.history_item_position_tag);
                    historyAdapter.remove(position);
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

    private class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LayoutInflater factory;
        private List<TabData> list;

        @SuppressLint("SimpleDateFormat")
        HistoryAdapter(List<TabData> objects) {
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = factory.inflate(R.layout.history_item_recycler_view, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof RecyclerViewHolder) {
                final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;

                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_recycler_item_show);
                recyclerViewHolder.mView.startAnimation(animation);

                TabData tabData = list.get(position);

                recyclerViewHolder.title.setText(tabData.getTitle());

                Drawable drawable;
                if (tabData.getIcon() != null) {
                    drawable = new BitmapDrawable(getResources(), Utils.Bytes2Bitmap(tabData.getIcon()));
                } else {
                    drawable = getResources().getDrawable(R.drawable.ic_website);
                }
                recyclerViewHolder.icon.setBackground(drawable);
                recyclerViewHolder.mView.setTag(tabData);
                recyclerViewHolder.mView.setTag(R.id.history_item_position_tag, position);
                recyclerViewHolder.mView.setOnClickListener(HistorySearchFragment.this);
                recyclerViewHolder.mView.setOnLongClickListener(HistorySearchFragment.this);

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

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.icon)
        ImageView icon;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }


}
