package peter.util.searcher.tab;

import android.text.TextUtils;
import android.view.View;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;

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

    public abstract void onDestroy();

    public abstract String getTitle();

    public abstract String getUrl();

    public void loadUrl(Bean bean) {
        if (!TextUtils.isEmpty(bean.url)) {
            if(mView == null) {
                int viewResId = onCreateViewResId();
                mView = mainActivity.setCurrentView(viewResId);
                onCreate();
            }else {
                mainActivity.setCurrentView(mView);
            }

        }
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public int getPageNo() { return 0;}

    public void reload() {}

    public boolean canGoBack() {
        return false;
    }

    public void goBack() {}

    public boolean canGoForward() {
        return false;
    }

    public void goForward() {}

    @Override
    public String getHost() {
        return getTitle();
    }

}
