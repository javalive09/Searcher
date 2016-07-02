package peter.util.searcher;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 16/5/9.
 */
public class CommonEnter2Fragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    ListView mList;
    View rootView;
    PopupMenu popup;
    MyAsyncTask asyncTask;
    static final String NAV_ENTER = "http://h5.mse.360.cn/navi.html";
    static final String NEWS_163 = "http://3g.163.com/touch/all?version=v_standard";
    static final String WEATHER_URL = "http://mobile.weathercn.com/index.do";
    static final String HISTORY_TODAY_URL = "http://wap.lssdjt.com/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_common_website, container, false);
        mList = (ListView) rootView.findViewById(R.id.web_sites);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void cancelAsyncTask() {
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(true);
        }
    }

    private void refreshData() {
        cancelAsyncTask();
        asyncTask = new MyAsyncTask(this);
        asyncTask.execute();
    }

    private List<CommonBean> getData() {
        List<CommonBean> list = new ArrayList();
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.history_today_title), HISTORY_TODAY_URL, 0));
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.weeks_weather), WEATHER_URL, 0));
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.web_guide), NAV_ENTER, 0));
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.news_163), NEWS_163, 0));

        list.add(new CommonBean(R.drawable.history_icon, getString(R.string.action_history), "", 0));
        list.add(new CommonBean(R.drawable.history_url_icon, getString(R.string.action_history_url), "", 0));
        list.add(new CommonBean(R.drawable.fav_icon, getString(R.string.action_collection), "", 0));
        list.add(new CommonBean(R.drawable.settings_icon, getString(R.string.setting_title), "", 0));
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.web_site_item:
                BaseActivity baseActivity = (BaseActivity) (getActivity());
                CommonBean bean = (CommonBean) v.getTag();
                switch (bean.iconId) {
                    case R.drawable.web_site_icon:
                        baseActivity.startBrowser(getActivity(), bean.url, bean.title);
                        break;
                    case R.drawable.history_icon:
                        baseActivity.startHistoryAct();
                        break;
                    case R.drawable.history_url_icon:
                        baseActivity.startHistorUrlyAct();
                        break;
                    case R.drawable.fav_icon:
                        baseActivity.startFavAct();
                        break;
                    case R.drawable.settings_icon:
                        startActivity(new Intent(baseActivity, SettingActivity.class));
                        break;
                }

                break;
            default:
                break;
        }
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

    private static class MyAsyncTask extends AsyncTask<Void, Void, List<CommonBean>> {

        WeakReference<CommonEnter2Fragment> wr;

        public MyAsyncTask(CommonEnter2Fragment f) {
            wr = new WeakReference<>(f);
        }

        @Override
        protected List<CommonBean> doInBackground(Void... params) {
            CommonEnter2Fragment f = wr.get();
            List<CommonBean> searches = new ArrayList<>();
            if (f != null) {
                List<Bean> recentList = null;
                try {
                    recentList = SqliteHelper.instance(f.getActivity()).queryRecentData();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(recentList != null) {
                        for (Bean bean : recentList) {
                            CommonBean cb = new CommonBean(R.drawable.search_small, bean.name, bean.url, bean.time);
                            searches.add(cb);
                        }
                        searches.addAll(f.getData());
                    }
                }
            }
            return searches;
        }

        @Override
        protected void onPostExecute(List<CommonBean> beans) {
            super.onPostExecute(beans);
            CommonEnter2Fragment f = wr.get();
            if (f != null) {
                if (!f.isDetached()) {
                    View loading = f.rootView.findViewById(R.id.loading);
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }
                    if (beans != null) {
                        ListView recentSearch = (ListView) f.rootView.findViewById(R.id.recent_search);
                        if(recentSearch != null) {
                            RecentSearchAdapter adapter = (RecentSearchAdapter) recentSearch.getAdapter();
                            if(adapter == null) {
                                recentSearch.setAdapter(new RecentSearchAdapter(beans, f));
                            }else {
                                adapter.refresh(beans);
                            }
                        }
                    }
                }
            }

        }

    }

    private static class RecentSearchAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        CommonEnter2Fragment f;
        private List<CommonBean> list;

        public RecentSearchAdapter(List<CommonBean> list, CommonEnter2Fragment f) {
            this.f = f;
            factory = LayoutInflater.from(f.getActivity());
            this.list = list;
        }

        public void refresh(List<CommonBean> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CommonBean getItem(int position) {
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

            CommonBean search = getItem(position);
            holder.content.setText(search.title);

            Drawable drawable= f.getResources().getDrawable(search.iconId);
            /// 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            holder.content.setCompoundDrawables(drawable,null,null,null);

            if(search.iconId == R.drawable.search_small) {
                holder.choice.setVisibility(View.VISIBLE);
                holder.choice.setOnClickListener(f);
                holder.choice.setTag(search);
                holder.content.setOnLongClickListener(f);
            }else {
                holder.choice.setVisibility(View.GONE);
            }

            holder.content.setOnClickListener(f);
            holder.content.setTag(search);

            return convertView;
        }
    }

    static class Holder {
        TextView content;
        ImageView choice;
    }

    public static class CommonBean{
        int iconId;
        String title;
        String url;
        long time;

        public CommonBean(int iconId, String title, String url, long time) {
            this.iconId = iconId;
            this.title = title;
            this.url = url;
            this.time = time;
        }
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

}
