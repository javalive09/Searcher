package peter.util.searcher.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.bean.Bean;

/**
 * Created by peter on 16/5/9.
 */
public class RecentSearchFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;
    CharSequence word;
    @BindView(R.id.enter)
    View enter;
    @BindView(R.id.enter_txt)
    View enterTxt;
    @BindView(R.id.paste)
    View paste;
    @BindView(R.id.loading)
    View loading;
    @BindView(R.id.recent_search)
    ListView recentSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent_search, container, false);
        ButterKnife.bind(RecentSearchFragment.this, rootView);
        paste.setOnClickListener(RecentSearchFragment.this);
        enter.setEnabled(false);
        enterTxt.setEnabled(false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
        ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        word = cmb.getText();
        if (TextUtils.isEmpty(word)) {
            paste.setEnabled(false);
        } else {
            paste.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        Bean bean = (Bean) v.getTag();
        switch (v.getId()) {
            case R.id.recent_search_item:
                if (bean != null) {
                    BaseActivity activity = (BaseActivity) getActivity();
                    activity.setSearchWord(bean.name);
                }
                break;
            case R.id.paste:
                SearchActivity searchActivity = (SearchActivity) getActivity();
                searchActivity.setSearchWord(word.toString());
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
            case R.id.recent_search_item:
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
                    searches = SqliteHelper.instance(f.getActivity()).queryRecentData();
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
            Holder holder;
            if (convertView == null) {
                convertView = factory.inflate(R.layout.recent_search_item, parent, false);
                holder = new Holder();
                holder.content = (TextView) convertView.findViewById(R.id.recent_search_item);
                convertView.setTag(R.id.recent_search, holder);
            } else {
                holder = (Holder) convertView.getTag(R.id.recent_search);
            }

            Bean search = getItem(position);
            holder.content.setText(search.name);
            holder.content.setOnClickListener(f);
            holder.content.setOnLongClickListener(f);
            holder.content.setTag(search);
            return convertView;
        }
    }

    static class Holder {
        TextView content;
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
                        SqliteHelper.instance(getActivity()).deleteHistory(bean);
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
