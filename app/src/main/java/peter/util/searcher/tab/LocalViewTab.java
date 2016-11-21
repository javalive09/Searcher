package peter.util.searcher.tab;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public abstract class LocalViewTab extends SearcherTab{

    protected View mView;

    public LocalViewTab(MainActivity activity) {
        super(activity);
    }

    public abstract int onCreateViewResId();

    public abstract void onCreate();

    public abstract void onDestory();

    public abstract String getTitle();

    public abstract String getUrl();

    public void loadUrl(String url, String searchWord) {
        if (!TextUtils.isEmpty(url)) {
            if(mView == null) {
                int viewResId = onCreateViewResId();
                mView = mainActivity.setCurrentView(viewResId);
                onCreate();
            }else {
                mainActivity.setCurrentView(mView);
            }

            if (!TextUtils.isEmpty(searchWord)) {
                mSearchWord = searchWord;
            }
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
