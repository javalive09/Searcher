package peter.util.searcher.fragment;

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

import java.util.List;

import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;

/**
 * Created by peter on 16/5/9.
 */
public class FavoriteFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;
    View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        return rootView;
    }

    private void refreshData() {
        cancelAsyncTask();
        asyncTask = new MyAsyncTask();
        asyncTask.execute();
    }

    private void cancelAsyncTask() {
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.favorite_item:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    ((BaseActivity) getActivity()).startBrowser(bean.url, bean.name);
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.favorite_item:
                popupMenu(v);
                return true;
        }
        return false;
    }


    @Override
    public void onDestroy() {
        dismissPopupMenu();
        cancelAsyncTask();
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

    private class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

        @Override
        protected List<Bean> doInBackground(Void... params) {
            List<Bean> searches = null;
            try {
                searches = SqliteHelper.instance(getActivity()).queryAllFavorite();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return searches;
        }

        @Override
        protected void onPostExecute(List<Bean> beans) {
            super.onPostExecute(beans);
            if (beans != null) {
                if (beans.size() == 0) {
                    rootView.findViewById(R.id.no_record).setVisibility(View.VISIBLE);
                } else {
                    rootView.findViewById(R.id.no_record).setVisibility(View.GONE);
                    ListView favorite = (ListView) rootView.findViewById(R.id.history);
                    if (favorite.getAdapter() == null) {
                        favorite.setAdapter(new FavoriteAdapter(beans));
                    } else {
                        ((FavoriteAdapter) favorite.getAdapter()).updateData(beans);
                    }
                }
            }
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
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
                view = (TextView) factory.inflate(R.layout.favorite_item, parent, false);
            } else {
                view = (TextView) convertView;
            }

            Bean search = getItem(position);
            view.setText(search.name);
            view.setOnClickListener(FavoriteFragment.this);
            view.setOnLongClickListener(FavoriteFragment.this);
            view.setTag(search);
            return view;
        }
    }


}
