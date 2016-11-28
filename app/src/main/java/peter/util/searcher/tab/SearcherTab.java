package peter.util.searcher.tab;

import android.graphics.drawable.Drawable;
import android.view.View;

import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public abstract class SearcherTab<T extends View> implements Tab {

    protected MainActivity mainActivity;

    protected Drawable iconDrawable;

    public SearcherTab(MainActivity activity) {
        mainActivity = activity;
    }

    public int onCreateViewResId() {
        return 0;
    }

    public void onCreate() {

    }

    public abstract View getView();

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
        mainActivity.updateMultiWindow();
    }

    public void onDestory() {

    }

    public abstract String getTitle();

    public abstract String getUrl();

    public abstract void loadUrl(String url, String searchWord);

    public abstract void reload();

    public abstract boolean canGoBack();

    public abstract void goBack();

    public abstract boolean canGoForward();

    public abstract void goForward();


}
