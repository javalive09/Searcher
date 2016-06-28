package peter.util.searcher;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 16/5/9.
 */
public class WebHintFragment extends Fragment implements View.OnClickListener {

    View rootView;
    MyAsyncTask asyncTask;
    static final String webHintUrl = "http://unionsug.baidu.com/su/?wd=%s";
    static final int LIMIT = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_web_hint, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Bean bean = (Bean) ((View) v.getParent()).getTag();
        switch (v.getId()) {
            case R.id.recent_search_item:
                if (bean != null) {
                    SearchActivity searchActivity = (SearchActivity) getActivity();
//                    String searchWord = bean.name;
//                    String engineUrl = getString(R.string.default_engine_url);
//                    String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
//                    searchActivity.startBrowserFromSearch(getActivity(), url, searchWord);
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

    public void refreshData(String content) {
        cancelAsyncTask();
        asyncTask = new MyAsyncTask(this);
        asyncTask.execute(content);
    }

    @Override
    public void onDestroy() {
        cancelAsyncTask();
        super.onDestroy();
    }

    private static class MyAsyncTask extends AsyncTask<String, Void, List<Bean>> {

        WeakReference<WebHintFragment> wr;

        public MyAsyncTask(WebHintFragment f) {
            wr = new WeakReference<>(f);
        }

        @Override
        protected List<Bean> doInBackground(String... params) {
            WebHintFragment f = wr.get();
            List<Bean> searches = null;
            if (f != null) {
                String content = params[0];
                if(!TextUtils.isEmpty(content)) {
                    String path = String.format(webHintUrl, getEncodeString(content));
                    try {
                        searches = requestByGet(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return searches;
        }

        private List<Bean> requestByGet(String path) throws Exception {
            URL url = new URL(path);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(5 * 1000);
            urlConn.connect();

            if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                byte[] data = readStream(urlConn.getInputStream());
                String result = new String(data, "GBK");
                int start = result.indexOf("[") + 1;
                int end = result.indexOf("]");
                result = result.substring(start, end);
                String[] strs = result.split(",");
                int size = strs.length > LIMIT ? LIMIT : strs.length;
                List<Bean> searches = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    if (!TextUtils.isEmpty(strs[i])) {
                        Bean search = new Bean();
                        search.name = strs[i].replaceAll("\"", "");
                        searches.add(search);
                    }
                }
                return searches;
            }
            urlConn.disconnect();
            return null;
        }

        private byte[] readStream(InputStream inStream) throws Exception {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 2];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
            return outStream.toByteArray();
        }

        private String getEncodeString(String content) {
            try {
                content = URLEncoder.encode(content, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return content;
        }


        @Override
        protected void onPostExecute(List<Bean> beans) {
            super.onPostExecute(beans);
            if (beans != null) {
                WebHintFragment f = wr.get();
                if (f != null) {
                    if (!f.isDetached()) {
                        View loading = f.rootView.findViewById(R.id.loading);
                        if (loading != null) {
                            loading.setVisibility(View.GONE);
                        }
                        ListView recentSearch = (ListView) f.rootView.findViewById(R.id.hint_list);
                        RecentSearchAdapter adapter = (RecentSearchAdapter) recentSearch.getAdapter();
                        if(adapter == null) {
                            recentSearch.setAdapter(new RecentSearchAdapter(beans, f));
                        }else {
                            adapter.update(beans);
                        }
                    }
                }
            }

        }

    }

    private static class RecentSearchAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        WebHintFragment f;
        private List<Bean> list;

        public RecentSearchAdapter(List<Bean> list, WebHintFragment f) {
            this.f = f;
            factory = LayoutInflater.from(f.getActivity());
            this.list = list;
        }

        public void update(List<Bean> list) {
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
            convertView.setTag(search);
            holder.choice.setOnClickListener(f);
            return convertView;
        }
    }

    static class Holder {
        TextView content;
        ImageView choice;
    }

    private void cancelAsyncTask() {
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(true);
        }
    }

}
