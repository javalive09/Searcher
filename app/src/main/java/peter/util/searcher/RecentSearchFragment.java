package peter.util.searcher;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by peter on 16/5/9.
 */
public class RecentSearchFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    View rootView;
    PopupMenu popup;
    MyAsyncTask asyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recent_search, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onClick(View v) {
        Bean bean = (Bean)v.getTag();
        switch (v.getId()) {
            case R.id.recent_search_item:
                if (bean != null) {
                    SearchActivity searchActivity = (SearchActivity) getActivity();
//                    searchActivity.startBrowserFromSearch(getActivity(), bean.url, bean.name);
                    searchActivity.setSearchWord(bean.name);
                    searchActivity.setEngineFragment(SearchActivity.ENGINE_LIST);
                }
                break;
            case R.id.choose:
                if (bean != null) {
                    SearchActivity searchActivity = (SearchActivity) getActivity();
                    searchActivity.setSearchWord(bean.name);
                }
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

    private static class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

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
                    View loading = f.rootView.findViewById(R.id.loading);
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }
                    if (beans != null) {
                        ListView recentSearch = (ListView) f.rootView.findViewById(R.id.recent_search);
                        if(recentSearch != null) {
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
                holder.choice = (ImageView) convertView.findViewById(R.id.choose);
                convertView.setTag(R.id.recent_search, holder);
            } else {
                holder = (Holder) convertView.getTag(R.id.recent_search);
            }

            Bean search = getItem(position);
            holder.content.setText(search.name);
            holder.content.setOnClickListener(f);
            holder.content.setOnLongClickListener(f);
            holder.content.setTag(search);
            holder.choice.setTag(search);
            holder.choice.setOnClickListener(f);
            return convertView;
        }
    }

    static class Holder {
        TextView content;
        ImageView choice;
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
