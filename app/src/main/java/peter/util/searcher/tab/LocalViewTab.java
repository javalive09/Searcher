package peter.util.searcher.tab;

import android.text.TextUtils;
import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public abstract class LocalViewTab implements Tab{

    protected MainActivity mainActivity;

    public LocalViewTab(MainActivity activity) {
        mainActivity = activity;
    }

    public abstract int onCreateViewResId();

    public abstract String getTitle();

    public abstract String getUrl();


    public void loadUrl(String url, String searchWord, boolean newTab) {
        if (!TextUtils.isEmpty(url)) {
            int viewResId = onCreateViewResId();
            mainActivity.setCurrentView(viewResId);
        }
    }

    public void reload() {}

    public boolean canGoBack() {
        return false;
    }

    public void goBack() {}

    public boolean canGoForward() {
        return false;
    }

    public void goForward() {}


}
