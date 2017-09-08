package peter.util.searcher.fragment;

import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.utils.UrlUtils;

/**
 * 最近搜索fragment
 * Created by peter on 16/5/9.
 */
public class RecentSearchFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;
    CharSequence word;
    @BindView(R.id.paste_enter)
    View pasteEnter;
    @BindView(R.id.paste)
    View paste;
    @BindView(R.id.loading)
    View loading;
    @BindView(R.id.recent_search)
    ListView recentSearch;

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
        word = cmb.getText();
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

    @Override
    public void onClick(View v) {
        Bean bean = (Bean) v.getTag();
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
                searchActivity.startBrowser(new Bean("", word.toString()));
                break;
        }
    }

    private void refreshData() {
        cancelAsyncTask();
        asyncTask = new MyAsyncTask(this);
        asyncTask.execute();
    }

    @Override
    public void onDestroy() {
        dismissPopupMenu();
        cancelAsyncTask();
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

    private class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

        WeakReference<RecentSearchFragment> wr;

        public MyAsyncTask(RecentSearchFragment f) {
            wr = new WeakReference<>(f);
        }

        @Override
        protected List<Bean> doInBackground(Void... params) {
            RecentSearchFragment f = wr.get();
            List<Bean> searches = null;
            if (f != null) {
                try {
                    searches = DaoManager.getInstance().queryRecentData(9);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return searches;
        }

        @Override
        protected void onPostExecute(List<Bean> beans) {
            super.onPostExecute(beans);
            RecentSearchFragment f = wr.get();
            if (f != null) {
                if (!f.isDetached()) {
                    loading.setVisibility(View.GONE);
                    if (beans != null) {

                        if (beans.size() > 0) {
                            recentSearch.setAdapter(new RecentSearchAdapter(beans, f));
                        }
                    }
                }
            }
        }

    }

    private static class RecentSearchAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        RecentSearchFragment f;
        private List<Bean> list;

        public RecentSearchAdapter(List<Bean> list, RecentSearchFragment f) {
            this.f = f;
            factory = LayoutInflater.from(f.getActivity());
            this.list = list;
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
            if (convertView == null) {
                convertView = factory.inflate(R.layout.item_list_recentsearch, parent, false);
            }
            TextView content = (TextView) convertView;
            Bean search = getItem(position);
            content.setText(search.name);
            content.setOnClickListener(f);
            content.setOnLongClickListener(f);
            content.setTag(search);
            return convertView;
        }
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
                        DaoManager.getInstance().deleteHistory(bean);
                        refreshData();
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

    private void cancelAsyncTask() {
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(true);
        }
    }

}
