package peter.util.searcher.engine;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
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

import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.activity.EnterActivity;
import peter.util.searcher.bean.Bean;

/**
 * Created by peter on 16/5/9.
 */
public class RecentSearchFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    View rootView;
    PopupMenu popup;
    MyAsyncTask asyncTask;
    View js;
    String url = "http://200code.com/[object%20Object]/Searcher%E5%BF%AB%E9%80%9F%E6%90%9C%E7%B4%A2/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recent_search, container, false);
        js = rootView.findViewById(R.id.js);
        js.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onClick(View v) {
        Bean bean = (Bean) v.getTag();
        switch (v.getId()) {
            case R.id.recent_search_item:
                if (bean != null) {
                    EnterActivity enterActivity = (EnterActivity) getActivity();
                    enterActivity.setSearchWord(bean.name);
                }
                break;
            case R.id.js:
                BaseActivity act = (BaseActivity) getActivity();
                act.startBrowser(url, "");
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

                        if(beans.size() > 0) {
                            f.js.setVisibility(View.GONE);
                            ListView recentSearch = (ListView) f.rootView.findViewById(R.id.recent_search);
                            if (recentSearch != null) {
                                recentSearch.setAdapter(new RecentSearchAdapter(beans, f));
                            }
                        }else {
                            f.js.setVisibility(View.VISIBLE);
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
