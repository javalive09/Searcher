package peter.util.searcher.tab;

import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public class SearcherTab implements Tab {

    protected MainActivity mainActivity;

    public SearcherTab(MainActivity activity) {
        mainActivity = activity;
    }

    public int onCreateViewResId() {
        return 0;
    }

    public String getTitle() {
        return null;
    }

    public String getUrl() {
        return null;
    }

    public void loadUrl(String url, String searchWord, boolean newTab) {
    }

    public void reload() {
    }

    public boolean canGoBack() {
        return false;
    }

    public void goBack() {

    }

    public boolean canGoForward() {
        return false;
    }

    public void goForward() {

    }


}
