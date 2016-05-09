package peter.util.searcher;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FavoriteAdapter extends BaseAdapter {

    private final LayoutInflater factory;
    FavoriteActivity act;
    private List<Bean> list;

    public FavoriteAdapter(List<Bean> objects, FavoriteActivity act) {
        this.act = act;
        factory = LayoutInflater.from(act);
        list = objects;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Bean getItem(int position) {
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
            view = (TextView) factory.inflate(R.layout.history_item, parent, false);
        } else {
            view = (TextView) convertView;
        }

        Bean search = getItem(position);

        Drawable drawable = act.getResources().getDrawable(R.drawable.search_small);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        view.setCompoundDrawables(drawable, null, null, null);

        view.setText(search.name);
        view.setOnClickListener(act);
        view.setTag(search);
        return view;
    }

}