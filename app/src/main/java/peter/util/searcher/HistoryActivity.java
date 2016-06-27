package peter.util.searcher;

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

/**
 * Created by peter on 16/5/9.
 */
public class HistoryActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
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
                SqliteHelper.instance(HistoryActivity.this).deleteAllHistory();
                refreshData();
                return true;
            case R.id.action_exit:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.history_item:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    startBrowser(HistoryActivity.this, bean.url, bean.name);
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.history_item:
                popupMenu(v);
                return true;
        }
        return false;
    }

    private static class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

        WeakReference<HistoryActivity> wr;

        public MyAsyncTask(HistoryActivity act) {
            wr = new WeakReference<>(act);
        }

        @Override
        protected List<Bean> doInBackground(Void... params) {
            BaseActivity act = wr.get();
            List<Bean> searches = null;
            if (act != null) {
                try {
                    searches = SqliteHelper.instance(act).queryAllHistory();
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
                HistoryActivity act = wr.get();
                if (act != null) {
                    act.findViewById(R.id.loading).setVisibility(View.GONE);
                    ListView history = (ListView) act.findViewById(R.id.history);
                    if (history.getAdapter() == null) {
                        history.setAdapter(new HistoryAdapter(beans, act));
                    } else {
                        ((HistoryAdapter) (history.getAdapter())).updateData(beans);
                    }
                }
            }

        }

    }

    private static class HistoryAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        HistoryActivity act;
        private List<Bean> list;

        public HistoryAdapter(List<Bean> objects, HistoryActivity act) {
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
                view = (TextView) factory.inflate(R.layout.history_item, parent, false);
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
        popup = new PopupMenu(HistoryActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        Bean bean = (Bean) view.getTag();
                        SqliteHelper.instance(HistoryActivity.this).deleteHistory(bean);
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
