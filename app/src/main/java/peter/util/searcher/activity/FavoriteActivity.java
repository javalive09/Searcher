package peter.util.searcher.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import peter.util.searcher.bean.Bean;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;

/**
 * Created by peter on 16/5/9.
 */
public class FavoriteActivity extends BaseActivity implements View.OnClickListener,View.OnLongClickListener{

    PopupMenu popup;
    MyAsyncTask asyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            refreshData();
        }
    }

    private void refreshData() {
        cancelAsyncTask();
        asyncTask = new MyAsyncTask(this);
        asyncTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                SqliteHelper.instance(FavoriteActivity.this).deleteAllFav();
                refreshData();
                return true;
            case R.id.action_exit:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.favorite_item:
                Bean bean = (Bean) v.getTag();
                if(bean != null) {
                    startBrowser(bean.url, bean.name);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dismissPopupMenu();
        cancelAsyncTask();
        super.onDestroy();
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

    private static class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

        WeakReference<FavoriteActivity> wr;

        public MyAsyncTask(FavoriteActivity act) {
            wr = new WeakReference<>(act);
        }

        @Override
        protected List<Bean> doInBackground(Void... params) {
            BaseActivity act = wr.get();
            List<Bean> searches = null;
            if(act != null) {
                try {
                    searches = SqliteHelper.instance(act).queryAllFavorite();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return searches;
        }

        @Override
        protected void onPostExecute(List<Bean> beans) {
            super.onPostExecute(beans);
            if (beans != null) {
                FavoriteActivity act = wr.get();
                if(act != null) {
                    act.findViewById(R.id.loading).setVisibility(View.GONE);
                    ListView favorite = (ListView) act.findViewById(R.id.history);
                    if(favorite.getAdapter() == null) {
                        favorite.setAdapter(new FavoriteAdapter(beans, act));
                    }else {
                        ((FavoriteAdapter)favorite.getAdapter()).updateData(beans);
                    }
                }
            }

        }

    }

    private static class FavoriteAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        FavoriteActivity act;
        private List<Bean> list;

        public FavoriteAdapter(List<Bean> objects, FavoriteActivity act) {
            this.act = act;
            factory = LayoutInflater.from(act);
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
            view.setOnClickListener(act);
            view.setOnLongClickListener(act);
            view.setTag(search);
            return view;
        }
    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        Bean bean = (Bean) view.getTag();
                        SqliteHelper.instance(FavoriteActivity.this).deleteFav(bean);
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

    private void cancelAsyncTask() {
        if(asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(true);
        }
    }

}
