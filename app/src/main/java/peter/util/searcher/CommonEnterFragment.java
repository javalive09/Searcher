package peter.util.searcher;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
public class CommonEnterFragment extends Fragment implements View.OnClickListener {

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
        List<CommonBean> list = new ArrayList();
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.news_163), NEWS_163));
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.web_guide), NAV_ENTER));
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.weeks_weather), WEATHER_URL));
        list.add(new CommonBean(R.drawable.web_site_icon, getString(R.string.history_today_title), HISTORY_TODAY_URL));
        list.add(new CommonBean(R.drawable.history_icon, getString(R.string.action_history), ""));
        list.add(new CommonBean(R.drawable.history_url_icon, getString(R.string.action_history_url), ""));
        list.add(new CommonBean(R.drawable.fav_icon, getString(R.string.action_collection), ""));
        list.add(new CommonBean(R.drawable.settings_icon, getString(R.string.setting_title), ""));
        mList.setAdapter(new MyAdapter(list,this));
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

    private static class MyAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        CommonEnterFragment f;
        private List<CommonBean> list;

        public MyAdapter(List<CommonBean> list, CommonEnterFragment f) {
            this.f = f;
            factory = LayoutInflater.from(f.getActivity());
            this.list = list;
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
            TextView view;

            if (convertView == null) {
                view = (TextView) factory.inflate(R.layout.website_item, parent, false);
            } else {
                view = (TextView) convertView;
            }
            CommonBean search = getItem(position);
            Drawable drawable= f.getResources().getDrawable(search.iconId);
            /// 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            view.setCompoundDrawables(drawable,null,null,null);
            view.setText(search.title);
            view.setOnClickListener(f);
            view.setTag(search);
            return view;
        }

    }

    public static class CommonBean{
        int iconId;
        String title;
        String url;

        public CommonBean(int iconId, String title, String url) {
            this.iconId = iconId;
            this.title = title;
            this.url = url;
        }
    }

}
