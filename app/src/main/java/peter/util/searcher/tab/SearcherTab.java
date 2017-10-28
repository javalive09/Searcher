package peter.util.searcher.tab;

import android.graphics.drawable.Drawable;
import android.view.View;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.TabBean;

/**
 * search tab
 * Created by peter on 2016/11/17.
 */

public abstract class SearcherTab implements Tab {

    final MainActivity mainActivity;

    private Drawable iconDrawable;

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

    public abstract View getView();

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
        mainActivity.updateTabs();
    }

    public abstract String getTitle();

    public abstract String getUrl();

    public abstract SearcherTab create(TabBean bean);

    public abstract void loadUrl(TabBean bean);

    public abstract void reload();

    public abstract boolean canGoBack();

    public abstract void goBack();

    public abstract boolean canGoForward();

    public abstract void goForward();

}
