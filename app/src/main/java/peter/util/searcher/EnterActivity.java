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
import android.widget.TextView;

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
    static final int FAVORITE_URL = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        ListView listView = (ListView) findViewById(R.id.enter_list);
        listView.setAdapter(new EnterAdapter(EnterActivity.this, getData()));
    }

    private ArrayList<TypeBean> getData() {
        ArrayList<TypeBean> list = new ArrayList();
        list.add(new TypeBean(LOGO, -1, ""));
        list.add(new TypeBean(SEARCH, -1, ""));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_top, getString(R.string.hot_title)));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_history_today, getString(R.string.history_today_title)));
        list.add(new TypeBean(HOT_LIST, R.id.hot_list_id_week_weather, getString(R.string.weeks_weather)));
        list.add(new TypeBean(FAVORITE_URL, -1, ""));
        return list;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enter:
                startSearch(null);
                break;
            case R.id.hot_list:
                int id = (int) v.getTag(R.id.hot_list_id);
                switch (id) {
                    case R.id.hot_list_top:
                        Intent intent = new Intent(EnterActivity.this, HotTopActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.hot_list_history_today:
                        intent = new Intent(EnterActivity.this, MainActivity.class);
                        intent.putExtra("url", "http://wap.lssdjt.com/");
                        startActivity(intent);
                        break;
                    case R.id.hot_list_id_week_weather:
                        intent = new Intent(EnterActivity.this, MainActivity.class);
                        intent.putExtra("url", "http://e.weather.com.cn/d/index/101010100.shtml");
                        startActivity(intent);
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
                holder.logo = convertView.findViewById(R.id.logo);
                holder.search = convertView.findViewById(R.id.enter);
                holder.hotList = (TextView)convertView.findViewById(R.id.hot_list);
                holder.favoriteUrl = convertView.findViewById(R.id.favorite_url);
                convertView.setTag(holder);
            }else {
                holder = (Holder) convertView.getTag();
            }

            holder.logo.setVisibility(View.GONE);
            holder.search.setVisibility(View.GONE);
            holder.hotList.setVisibility(View.GONE);
            holder.favoriteUrl.setVisibility(View.GONE);

            holder.logo.setOnClickListener(act);
            holder.search.setOnClickListener(act);
            holder.hotList.setOnClickListener(act);
            holder.favoriteUrl.setOnClickListener(act);

            switch(getItemViewType(position)) {
                case LOGO:
                    holder.logo.setVisibility(View.VISIBLE);
                    break;
                case SEARCH:
                    holder.search.setVisibility(View.VISIBLE);
                    break;
                case HOT_LIST:
                    TypeBean bean = getItem(position);
                    holder.hotList.setVisibility(View.VISIBLE);
                    holder.hotList.setText(bean.content);
                    holder.hotList.setTag(R.id.hot_list_id, bean.id);
                    break;
                case FAVORITE_URL:
                    holder.favoriteUrl.setVisibility(View.VISIBLE);
                    break;
            }

            return convertView;
        }

    }

    static class Holder{
        View logo;
        View search;
        TextView hotList;
        View favoriteUrl;
    }

    static class TypeBean{
        int type;
        int id;
        String content;

        public TypeBean(int type, int id, String content) {
            this.type = type;
            this.id = id;
            this.content = content;
        }
    }
}
