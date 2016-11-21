package peter.util.searcher.tab;

import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public abstract class SearcherTab implements Tab {

    protected MainActivity mainActivity;

    protected String mSearchWord;

    public SearcherTab(MainActivity activity) {
        mainActivity = activity;
    }

    public int onCreateViewResId() {
        return 0;
    }

    public void onCreate() {

    }

    public void onDestory() {

    }

    public String getSearchWord() {
        return mSearchWord;
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
