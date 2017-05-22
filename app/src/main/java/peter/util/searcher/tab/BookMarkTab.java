package peter.util.searcher.tab;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import java.util.ArrayList;
import java.util.List;
import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;

/**
 * Created by peter on 2016/11/18.
 */

public class BookMarkTab extends LocalViewTab implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;

    public BookMarkTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_bookmark;
    }

    @Override
    public void onCreate() {
        LayoutInflater lf = mainActivity.getLayoutInflater();
        View favorite = lf.inflate(R.layout.tab_favorite, null);
        View historySearch = lf.inflate(R.layout.tab_history_search, null);
        View historyDownload = lf.inflate(R.layout.tab_download, null);

        List<View> viewList = new ArrayList<>();
        viewList.add(favorite);
        viewList.add(historySearch);
        viewList.add(historyDownload);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(viewList);
        ViewPager viewPager = (ViewPager) mView.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onDestory() {
    }

    @Override
    public String getSearchWord() {
        return "";
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.fast_enter_favorite);
    }

    @Override
    public String getUrl() {
        return URL_FAVORITE;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.favorite_item:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    mainActivity.startBrowser(bean.url, bean.name);
                }
                break;
        }
    }


    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.favorite_item:
                popupMenu(v);
                return true;
        }
        return false;
    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(mainActivity, view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        Bean bean = (Bean) view.getTag();
                        SqliteHelper.instance(mainActivity).deleteFav(bean);
                        break;
                }

                return true;
            }
        });
        popup.show();
    }

    private void dismissPopupMenu() {
        if (popup != null) {
            popup.dismiss();
        }
    }

}
