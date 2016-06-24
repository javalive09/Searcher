package peter.util.searcher;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 16/5/9.
 */
public class CommonWebSiteFragment extends Fragment implements View.OnClickListener {

    private ListView mList;
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
        View rootView = inflater.inflate(R.layout.fragment_common_website, container, false);
        mList = (ListView) rootView.findViewById(R.id.web_sites);
        init();
        return rootView;
    }

    private void init() {
        List<WebSiteBean> list = new ArrayList();
        list.add(new WebSiteBean(getString(R.string.news_163), NEWS_163));
        list.add(new WebSiteBean(getString(R.string.web_guide), NAV_ENTER));
        list.add(new WebSiteBean(getString(R.string.weeks_weather), WEATHER_URL));
        list.add(new WebSiteBean(getString(R.string.history_today_title), HISTORY_TODAY_URL));
        mList.setAdapter(new MyAdapter(list,this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.web_site_item:
                BaseActivity baseActivity = (BaseActivity) (getActivity());
                WebSiteBean bean = (WebSiteBean) v.getTag();
                if (bean != null) {
                    baseActivity.startBrowser(getActivity(), bean.url, bean.title);
                    baseActivity.finish();
                }
                break;
            default:
                break;
        }
    }

    private static class MyAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        CommonWebSiteFragment f;
        private List<WebSiteBean> list;

        public MyAdapter(List<WebSiteBean> list, CommonWebSiteFragment f) {
            this.f = f;
            factory = LayoutInflater.from(f.getActivity());
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public WebSiteBean getItem(int position) {
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
                view = (TextView) factory.inflate(R.layout.website_item, parent, false);
            } else {
                view = (TextView) convertView;
            }

            WebSiteBean search = getItem(position);
            view.setText(search.title);
            view.setOnClickListener(f);
            view.setTag(search);
            return view;
        }

    }

    public static class WebSiteBean{
        String title;
        String url;

        public WebSiteBean(String title, String url) {
            this.title = title;
            this.url = url;
        }
    }

}
