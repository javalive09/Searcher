package peter.util.searcher.tab;

import android.graphics.drawable.Drawable;
import android.view.View;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.db.dao.TabData;

/**
 * search tab
 * Created by peter on 2016/11/17.
 */

public abstract class SearcherTab implements Tab {

    final MainActivity mainActivity;

    private Drawable iconDrawable;

    private TabData tabData;

    public SearcherTab(MainActivity activity) {
        mainActivity = activity;
    }

    public int onCreateViewResId() {
        return 0;
    }

    void onCreate() {

    }

    void onResume() {

    }

    void onPause() {

    }

    void onDestroy() {

    }

    public MainActivity getActivity() {
        return mainActivity;
    }

    public abstract String getSearchWord();

    public abstract View getView();

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public abstract String getTitle();

    public abstract String getUrl();

    public SearcherTab create(TabData tabData) {
        this.tabData = tabData;
        return this;
    }

    public abstract void loadUrl(TabData bean);

    public abstract void reload();

    public abstract boolean canGoBack();

    public abstract void goBack();

    public abstract boolean canGoForward();

    public abstract void goForward();

    public TabData getTabData() {
        return tabData;
    }
}
