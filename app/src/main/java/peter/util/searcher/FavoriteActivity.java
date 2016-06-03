package peter.util.searcher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by peter on 16/5/9.
 */
public class FavoriteActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        ((TextView)findViewById(R.id.title_txt)).setText(R.string.action_collection);
        findViewById(R.id.back).setOnClickListener(this);
        new MyAsyncTask(FavoriteActivity.this).execute();
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
                    Utils.startSearchAct(FavoriteActivity.this, bean.url, bean.name);
                    finish();
                }
                break;
        }
    }

    private static class MyAsyncTask extends AsyncTask<Void, Void, List<Bean>> {

        WeakReference<BaseActivity> wr;

        public MyAsyncTask(BaseActivity act) {
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
                BaseActivity act = wr.get();
                if(act != null) {
                    act.findViewById(R.id.loading).setVisibility(View.GONE);
                    ListView favorite = (ListView) act.findViewById(R.id.history);
                    favorite.setAdapter(new FavoriteAdapter(beans, act));
                }
            }

        }

    }

    private static class FavoriteAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        BaseActivity act;
        private List<Bean> list;

        public FavoriteAdapter(List<Bean> objects, BaseActivity act) {
            this.act = act;
            factory = LayoutInflater.from(act);
            list = objects;
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
            view.setTag(search);
            return view;
        }
    }

}
