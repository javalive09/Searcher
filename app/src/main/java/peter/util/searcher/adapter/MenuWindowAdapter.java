package peter.util.searcher.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

public class MenuWindowAdapter extends BaseAdapter {

    private ArrayList<Integer> mList;

    private MainActivity mainActivity;

    public MenuWindowAdapter(MainActivity activity, ArrayList<Integer> mList) {
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
    public Integer getItem(int position) {
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
            convertView.setTag(holder);
        }

        if (holder == null) {
            holder = (Holder) convertView.getTag();
        }

        Integer itemRes = getItem(position);
        if (itemRes != null) {
            convertView.setActivated(true);
            holder.title.setText(itemRes);
            convertView.setOnClickListener(mainActivity);
            convertView.setTag(R.id.menu_window_item_tag, itemRes);
        }
        return convertView;
    }

    private static class Holder {
        TextView title;
    }

}

