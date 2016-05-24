package peter.util.searcher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by peter on 16/5/19.
 */
public class EnterActivity extends BaseActivity {

    static final int LOGO = 1;
    static final int SEARCH = 2;
    static final int HOT_LIST = 3;
    static final int HOT_LIST_START = 4;
    static final int HOT_LIST_END = 5;
    static final String WEATHER_URL = "http://e.weather.com.cn/d/index/101010100.shtml";
    static final String HISTORY_TODAY_URL = "http://wap.lssdjt.com/";
    AsynWindowHandler windowHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        ListView listView = (ListView) findViewById(R.id.enter_list);
        listView.setAdapter(new EnterAdapter(EnterActivity.this, getData()));
        windowHandler = new AsynWindowHandler(this);
        UpdateController.instance(getApplicationContext()).autoCheckVersion(new AsynWindowHandler(this));
    }

    private ArrayList<TypeBean> getData() {
        ArrayList<TypeBean> list = new ArrayList();
        list.add(new TypeBean(LOGO));
        list.add(new TypeBean(SEARCH));

        list.add(new TypeBean(HOT_LIST_START, R.id.hot_list_top, getString(R.string.hot_title)));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_history_today, getString(R.string.history_today_title), HISTORY_TODAY_URL));
        list.add(new TypeBean(HOT_LIST_END, R.id.hot_list_id_week_weather, getString(R.string.weeks_weather), WEATHER_URL));

        list.add(new TypeBean(HOT_LIST_START, R.id.hot_list_history, getString(R.string.action_history)));
        list.add(new TypeBean(HOT_LIST_END, R.id.hot_list_favorite, getString(R.string.action_collection)));

        list.add(new TypeBean(HOT_LIST_START, R.id.hot_list_share_app, getString(R.string.setting_share_app)));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_clear_cache, getString(R.string.setting_clear_cache)));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_feedback, getString(R.string.setting_feedback)));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_update, getString(R.string.setting_update)));
        list.add(new TypeBean(HOT_LIST_END, R.id.hot_list_about, getString(R.string.action_about)));

        return list;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enter:
                startSearch(null);
                break;
            case R.id.hot_list:
                TypeBean bean = (TypeBean) v.getTag(R.id.hot_list_id);
                switch (bean.id) {
                    case R.id.hot_list_top:
                        Intent intent = new Intent(EnterActivity.this, HotTopActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.hot_list_history_today:
                        intent = new Intent(EnterActivity.this, MainActivity.class);
                        intent.putExtra("url", bean.url);
                        startActivity(intent);
                        break;
                    case R.id.hot_list_id_week_weather:
                        intent = new Intent(EnterActivity.this, MainActivity.class);
                        intent.putExtra("url", bean.url);
                        startActivity(intent);
                        break;
                    case R.id.hot_list_history:
                        startActivity(new Intent(EnterActivity.this, HistoryActivity.class));
                        break;
                    case R.id.hot_list_favorite:
                        startActivity(new Intent(EnterActivity.this, FavoriteActivity.class));
                        break;
                    case R.id.hot_list_share_app:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_url));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
                        break;
                    case R.id.hot_list_clear_cache:
                        clearCacheFolder(getCacheDir(), 0);
                        ClearCookies(EnterActivity.this);
                        Toast.makeText(EnterActivity.this, R.string.setting_clear, Toast.LENGTH_LONG).show();
                        break;
                    case R.id.hot_list_feedback:
                        sendMailByIntent();
                        break;
                    case R.id.hot_list_update:
                        UpdateController.instance(getApplicationContext()).checkVersion(windowHandler, true);
                        break;
                    case R.id.hot_list_about:
                        showAlertDialog(getString(R.string.action_about), getString(R.string.setting_about));
                        break;
                }
                break;
        }
    }


    static class EnterAdapter extends BaseAdapter {

        EnterActivity act;
        LayoutInflater inflater;
        ArrayList<TypeBean> list;

        public EnterAdapter(EnterActivity act, ArrayList<TypeBean> list) {
            this.act = act;
            this.list = list;
            inflater = LayoutInflater.from(act);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public TypeBean getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.enter_item, parent, false);
                holder = new Holder();
                holder.version = (TextView) convertView.findViewById(R.id.version);
                holder.search = convertView.findViewById(R.id.enter);
                holder.hotList = (TextView)convertView.findViewById(R.id.hot_list);
                holder.favoriteUrl = convertView.findViewById(R.id.favorite_url);
                convertView.setTag(holder);
            }else {
                holder = (Holder) convertView.getTag();
            }

            holder.version.setVisibility(View.GONE);
            holder.search.setVisibility(View.GONE);
            holder.hotList.setVisibility(View.GONE);

            holder.version.setOnClickListener(act);
            holder.search.setOnClickListener(act);
            holder.hotList.setOnClickListener(act);

            switch(getItemViewType(position)) {
                case LOGO:
                    holder.version.setVisibility(View.VISIBLE);
                    holder.version.setText(act.getVersionName());
                    break;
                case SEARCH:
                    holder.search.setVisibility(View.VISIBLE);
                    break;
                case HOT_LIST:
                    TypeBean bean = getItem(position);
                    holder.hotList.setVisibility(View.VISIBLE);
                    holder.hotList.setText(bean.content);
                    holder.hotList.setTag(R.id.hot_list_id, bean);
                    holder.hotList.setBackgroundResource(R.drawable.enter_item_selector);
                    break;
                case HOT_LIST_START:
                    bean = getItem(position);
                    holder.hotList.setVisibility(View.VISIBLE);
                    holder.hotList.setText(bean.content);
                    holder.hotList.setTag(R.id.hot_list_id, bean);
                    holder.hotList.setBackgroundResource(R.drawable.enter_item_start_selector);
                    break;
                case HOT_LIST_END:
                    bean = getItem(position);
                    holder.hotList.setVisibility(View.VISIBLE);
                    holder.hotList.setText(bean.content);
                    holder.hotList.setTag(R.id.hot_list_id, bean);
                    holder.hotList.setBackgroundResource(R.drawable.enter_item_end_selector);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.hotList.getLayoutParams();
                    params.bottomMargin = act.getResources().getDimensionPixelOffset(R.dimen.enter_item_margin);
                    holder.hotList.setLayoutParams(params);
                    break;
            }
            return convertView;
        }
    }

    static class Holder{
        TextView version;
        View search;
        TextView hotList;
        View favoriteUrl;
    }

    static class TypeBean{
        int type;
        int id;
        String content;
        String url;

        public TypeBean(int type) {
            this.type = type;
        }

        public TypeBean(int type, int id) {
            this.type = type;
            this.id = id;
        }

        public TypeBean(int type, int id, String content) {
            this.type = type;
            this.id = id;
            this.content = content;
        }

        public TypeBean(int type, int id, String content, String url) {
            this.type = type;
            this.id = id;
            this.content = content;
            this.url = url;
        }
    }
}
