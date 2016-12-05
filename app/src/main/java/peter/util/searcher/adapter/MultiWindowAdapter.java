package peter.util.searcher.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.tab.TabGroup;

public class MultiWindowAdapter extends BaseAdapter {

    private ArrayList<TabGroup> mList;

    private MainActivity mainActivity;

    public void update(MainActivity activity) {
        mainActivity = activity;
        mList = mainActivity.getManager().getList();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public TabGroup getItem(int position) {
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
            convertView = mainActivity.getLayoutInflater().inflate(R.layout.multiwindow_item, parent, false);
            holder = new Holder();
            holder.close = (ImageView) convertView.findViewById(R.id.close_tab);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        }

        if (holder == null) {
            holder = (Holder) convertView.getTag();
        }

        TabGroup tabGroup = getItem(position);
        if (tabGroup != null) {
            if (mainActivity.getManager().getCurrentTabGroup() == tabGroup) {
                convertView.setActivated(true);
            } else {
                convertView.setActivated(false);
            }
            holder.title.setText(tabGroup.getCurrentTab().getTitle());
            Drawable icon = tabGroup.getCurrentTab().getIconDrawable();
            if (icon != null) {
                holder.icon.setBackground(icon);
            } else {
                holder.icon.setBackgroundResource(R.drawable.web_site_icon);
            }
            holder.close.setOnClickListener(mainActivity);
            holder.close.setTag(tabGroup);
            convertView.setOnClickListener(mainActivity);
            convertView.setTag(R.id.multi_window_item_tag, tabGroup);
        }
        return convertView;
    }

    private static class Holder {
        ImageView icon;
        TextView title;
        ImageView close;
    }

}