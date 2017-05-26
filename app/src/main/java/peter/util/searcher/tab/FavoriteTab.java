package peter.util.searcher.tab;

import android.os.AsyncTask;
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
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;

/**
 * Created by peter on 2016/11/18.
 */

public class FavoriteTab extends LocalViewTab implements View.OnClickListener,View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;

    public FavoriteTab(MainActivity activity) {
        super(activity);
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
    public int onCreateViewResId() {
        return R.layout.tab_favorite;
    }

    @Override
    public void onCreate() {
        refreshData();

    }

    @Override
    public void onDestory() {
    }

    @Override
    public String getSearchWord() {
        return "";
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.fast_enter_favorite);
    }

    @Override
    public String getUrl() {
        return URL_FAVORITE;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.favorite_item:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    mainActivity.startBrowser(bean.url, bean.name);
                }
                break;
        }
    }


    private class FavoriteAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private List<Bean> list;

        public FavoriteAdapter(List<Bean> objects) {
            factory = LayoutInflater.from(mainActivity);
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
            view.setOnClickListener(FavoriteTab.this);
            view.setOnLongClickListener(FavoriteTab.this);
            view.setTag(search);
            return view;
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

    private class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

        @Override
        protected List<Bean> doInBackground(Void... params) {
            List<Bean> searches = null;
            try {
                searches = SqliteHelper.instance(mainActivity).queryAllFavorite();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return searches;
        }

        @Override
        protected void onPostExecute(List<Bean> beans) {
            super.onPostExecute(beans);
            if (beans != null) {
                mainActivity.findViewById(R.id.loading).setVisibility(View.GONE);
                ListView favorite = (ListView) mainActivity.findViewById(R.id.history);
                if (favorite.getAdapter() == null) {
                    favorite.setAdapter(new FavoriteAdapter(beans));
                } else {
                    ((FavoriteAdapter) favorite.getAdapter()).updateData(beans);
                }
            }
        }

    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(mainActivity, view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        Bean bean = (Bean) view.getTag();
                        SqliteHelper.instance(mainActivity).deleteFav(bean);
                        refreshData();
                        break;
                }

                return true;
            }
        });
        popup.show();
    }

    private void dismissPopupMenu() {
        if(popup != null) {
            popup.dismiss();
        }
    }

}
