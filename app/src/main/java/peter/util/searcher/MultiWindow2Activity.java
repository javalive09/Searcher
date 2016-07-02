package peter.util.searcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.deckview.views.DeckChildView;
import com.appeaser.deckview.views.DeckView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 16/5/19.
 */
public class MultiWindow2Activity extends BaseActivity implements View.OnClickListener{

    MyAdapter adapter;
    Drawable defaultHeadIcon;
    ListView listView;
    int scrollToChildIndex = -1;
    // SavedInstance bundle keys
    final String CURRENT_SCROLL = "current.scroll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiwindow2);
        listView = (ListView) findViewById(R.id.list);
        adapter = new MyAdapter(SearcherWebViewManager.instance().getAllViews(), MultiWindow2Activity.this);
        listView.setAdapter(adapter);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_SCROLL)) {
                scrollToChildIndex = savedInstanceState.getInt(CURRENT_SCROLL);
            }
        }
        defaultHeadIcon = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        if (scrollToChildIndex == -1) {
            scrollToChildIndex = SearcherWebViewManager.instance().getCurrentWebViewPos();
        }
        setListViewPos(scrollToChildIndex);

        final View newTab = findViewById(R.id.new_tab);
        if(newTab != null) {
            newTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSearch(true);
                }
            });
        }

    }

    private void setListViewPos(int pos) {
        if (android.os.Build.VERSION.SDK_INT >= 8) {
            listView.smoothScrollToPosition(pos);
        } else {
            listView.setSelection(pos);
        }
    }

    @Override
    public void onBackPressed() {
        SearcherWebView view = SearcherWebViewManager.instance().getCurrentWebView();
        switchBrowser(MultiWindow2Activity.this, view.getUrl());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save current scroll and the list

        int currentChildIndex = listView.getFirstVisiblePosition();
        outState.putInt(CURRENT_SCROLL, currentChildIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        SearcherWebView item = (SearcherWebView) v.getTag();
        switch(v.getId()) {
            case R.id.list_item:
                switchBrowser(MultiWindow2Activity.this, item.getUrl());
                finish();
                break;
            case R.id.close:
                SearcherWebViewManager.instance().removeWebView(item);
                if (SearcherWebViewManager.instance().getWebViewCount() > 0) {
                    SearcherWebViewManager.instance().setDefaultCurrentWebView();
                    adapter.refresh(SearcherWebViewManager.instance().getAllViews());
                } else {
                    startBrowser(MultiWindow2Activity.this, "", "");
                }
                break;
        }
    }

    private static class MyAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        MultiWindow2Activity f;
        private List<SearcherWebView> list;

        public MyAdapter(List<SearcherWebView> list, MultiWindow2Activity m) {
            this.f = m;
            factory = LayoutInflater.from(m);
            this.list = list;
        }

        public void refresh(List<SearcherWebView> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public SearcherWebView getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = factory.inflate(R.layout.multi_window_list_item, parent, false);
                holder = new Holder();
                holder.content = (TextView) convertView.findViewById(R.id.title);
                holder.close = (ImageView) convertView.findViewById(R.id.close);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(R.id.multi_window, holder);
            } else {
                holder = (Holder) convertView.getTag(R.id.multi_window);
            }
            SearcherWebView search = getItem(position);
            holder.icon.setImageBitmap(search.getFavIcon());
            holder.content.setText(search.getTitle());
            convertView.setOnClickListener(f);
            convertView.setTag(search);
            holder.close.setTag(search);
            holder.close.setOnClickListener(f);
            return convertView;
        }
    }

    static class Holder {
        TextView content;
        ImageView close;
        ImageView icon;
    }
}
