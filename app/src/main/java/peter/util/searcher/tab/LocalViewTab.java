package peter.util.searcher.tab;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.TabBean;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public abstract class LocalViewTab extends SearcherTab{

    View mView;

    LocalViewTab(MainActivity activity) {
        super(activity);
    }

    public abstract int onCreateViewResId();

    abstract void onCreate();

    abstract void onDestroy();

    public abstract String getTitle();

    public abstract String getUrl();

    @Override
    public final LocalViewTab create(TabBean bean) {
        if(!TextUtils.isEmpty(bean.url)) {
            if(mView == null) {
                int viewResId = onCreateViewResId();
                LayoutInflater factory = LayoutInflater.from(mainActivity);
                mView = factory.inflate(viewResId, mainActivity.getWebViewContainer(), false);
                onCreate();
            }
        }
        return this;
    }

    public void loadUrl(TabBean bean) {
        mainActivity.setCurrentView(mView);
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
