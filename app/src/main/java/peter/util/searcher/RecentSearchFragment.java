package peter.util.searcher;

import android.app.Fragment;
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
public class RecentSearchFragment extends Fragment implements View.OnClickListener {

    View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recent_search, container, false);
        new MyAsyncTask(this).execute();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recent_search_item:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("url", bean.url);
                    intent.putExtra("name", bean.name);
                    startActivity(intent);
                }
                break;
        }
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
                    searches = SqliteHelper.instance(f.getActivity()).queryAllFavorite();
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
                RecentSearchFragment f = wr.get();
                if (f != null) {
                    if (!f.isDetached()) {
                        f.rootView.findViewById(R.id.loading).setVisibility(View.GONE);
                        ListView recentSearch = (ListView) f.rootView.findViewById(R.id.recent_search);
                        recentSearch.setAdapter(new RecentSearchAdapter(beans, f));
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
            TextView view;
            if (convertView == null) {
                view = (TextView) factory.inflate(R.layout.favorite_item, parent, false);
            } else {
                view = (TextView) convertView;
            }

            Bean search = getItem(position);
            view.setText(search.name);
            view.setOnClickListener(f);
            view.setTag(search);
            return view;
        }
    }

}
