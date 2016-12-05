package peter.util.searcher.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

public class MenuWindowAdapter extends BaseAdapter {

    private ArrayList<MenuItem> mList;

    private MainActivity mainActivity;

    public MenuWindowAdapter(MainActivity activity, ArrayList<MenuItem> mList) {
        mainActivity = activity;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public MenuItem getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Holder holder = null;
        if (convertView == null) {
            convertView = mainActivity.getLayoutInflater().inflate(R.layout.menuwindow_item, parent, false);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        }

        if (holder == null) {
            holder = (Holder) convertView.getTag();
        }

        MenuItem menuItem = getItem(position);
        if (menuItem != null) {
            holder.title.setText(menuItem.titleRes);
            holder.icon.setImageResource(menuItem.iconRes);
            convertView.setOnClickListener(mainActivity);
            convertView.setTag(R.id.menu_window_item_tag, menuItem);
        }
        return convertView;
    }

    private static class Holder {
        TextView title;
        ImageView icon;
    }

    public static class MenuItem {
        public int iconRes;
        public int titleRes;

        public MenuItem(int iconRes, int titleRes) {
            this.iconRes = iconRes;
            this.titleRes = titleRes;
        }
    }

}

