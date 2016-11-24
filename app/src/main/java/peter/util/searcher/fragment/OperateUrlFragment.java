package peter.util.searcher.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;
import peter.util.searcher.R;
import peter.util.searcher.activity.SearchActivity;


/**
 * Created by peter on 2016/11/24.
 */

public class OperateUrlFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_operate_url, container, false);
        GridView operateUrl = (GridView) rootView.findViewById(R.id.operate_url);

        ArrayList<IconItem> iconItems = new ArrayList<>(4);
        IconItem copy = new IconItem();
        copy.iconResId = R.drawable.copy;
        copy.iconName = getString(R.string.operate_url_copy);

        IconItem enter = new IconItem();
        enter.iconResId = R.drawable.enter;
        enter.iconName = getString(R.string.operate_url_enter);

        iconItems.add(copy);
        iconItems.add(enter);
        OperateUrlAdapter adapter = new OperateUrlAdapter(iconItems);

        operateUrl.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        IconItem iconItem = (IconItem) v.getTag();
        SearchActivity searchActivity = (SearchActivity) getActivity();
        String url = searchActivity.getSearchWord();
        switch (iconItem.iconResId) {
            case R.drawable.copy:
                ClipboardManager cmb = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(url.trim());
                break;
            case R.drawable.enter:
                searchActivity.startBrowser(url, "");
                break;
        }
    }

    private class OperateUrlAdapter extends BaseAdapter {

        private ArrayList<IconItem> iconItems;

        public OperateUrlAdapter(ArrayList<IconItem> iconItems) {
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
            if (convertView == null) {
                LayoutInflater factory = LayoutInflater.from(getActivity());
                convertView = factory.inflate(R.layout.fragment_operate_url_item, parent, false);
            }
            IconItem iconItem = getItem(position);
            convertView.findViewById(R.id.operate_url_icon).setBackgroundResource(iconItem.iconResId);
            ((TextView) convertView.findViewById(R.id.operate_url_title)).setText(iconItem.iconName);
            convertView.setOnClickListener(OperateUrlFragment.this);
            convertView.setTag(iconItem);
            return convertView;
        }
    }

    private class IconItem {
        int iconResId;
        String iconName;
    }

}
