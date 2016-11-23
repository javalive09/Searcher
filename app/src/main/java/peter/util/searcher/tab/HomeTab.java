package peter.util.searcher.tab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class HomeTab extends LocalViewTab implements View.OnClickListener{

    public HomeTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_home;
    }

    @Override
    public void onCreate() {
        mainActivity.findViewById(R.id.searcher_button)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.startSearcheActivity("");
            }
        });

        GridView fastEnter = (GridView) mainActivity.findViewById(R.id.fast_enter);
        ArrayList<IconItem> iconItems = new ArrayList<>();
        IconItem navigation = new IconItem();
        navigation.iconResId = R.drawable.navigation;
        navigation.iconName = mainActivity.getString(R.string.fast_enter_navigation);
        navigation.url = mainActivity.getString(R.string.fast_enter_navigation_url);

        IconItem favorite = new IconItem();
        favorite.iconResId = R.drawable.favorite;
        favorite.iconName = mainActivity.getString(R.string.fast_enter_favorite);
        favorite.url = Tab.URL_FAVORITE;

        IconItem history = new IconItem();
        history.iconResId = R.drawable.history;
        history.iconName = mainActivity.getString(R.string.fast_enter_history);
        history.url = Tab.URL_HISTORY;

        IconItem setting = new IconItem();
        setting.iconResId = R.drawable.setting;
        setting.iconName = mainActivity.getString(R.string.fast_enter_setting);
        setting.url = Tab.URL_SETTING;

        iconItems.add(navigation);
        iconItems.add(favorite);
        iconItems.add(history);
        iconItems.add(setting);

        FastEnterAdapter fastEnterAdapter = new FastEnterAdapter(iconItems);
        fastEnter.setAdapter(fastEnterAdapter);
    }

    @Override
    public void onDestory() {

    }

    @Override
    public String getSearchWord() {
        return null;
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.home);
    }

    @Override
    public String getUrl() {
        return URL_HOME;
    }

    @Override
    public void onClick(View v) {
        IconItem iconItem = (IconItem) v.getTag();
        mainActivity.loadUrl(iconItem.url, false);
    }

    private class FastEnterAdapter extends BaseAdapter {

        private ArrayList<IconItem> iconItems;

        public FastEnterAdapter(ArrayList<IconItem> iconItems) {
            this.iconItems = iconItems;
        }

        @Override
        public int getCount() {
            return iconItems.size();
        }

        @Override
        public IconItem getItem(int position) {
            return iconItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater factory = LayoutInflater.from(mainActivity);
                convertView = factory.inflate(R.layout.tab_home_enter_item, parent, false);
            }
            IconItem iconItem = getItem(position);
            convertView.findViewById(R.id.fast_enter_icon).setBackgroundResource(iconItem.iconResId);
            ((TextView)convertView.findViewById(R.id.fast_enter_title)).setText(iconItem.iconName);
            convertView.setOnClickListener(HomeTab.this);
            convertView.setTag(iconItem);
            return convertView;
        }
    }

    private class IconItem{
        int iconResId;
        String iconName;
        String url;
    }

}
