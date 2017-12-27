package peter.util.searcher.tab;


import peter.util.searcher.db.dao.TabData;

/**
 *
 * Created by peter on 2016/11/17.
 */

public interface Tab {

    String LOCAL_HOST = "local";
    String LOCAL_SCHEMA = LOCAL_HOST + "://";
    String URL_HOME = LOCAL_SCHEMA + "home";
    String ACTION_NEW_WINDOW = "new_window";

    void loadUrl(TabData bean);

    String getUrl();

    String getSearchWord();

    int getPageNo();

    void reload();

    boolean canGoBack();

    void goBack();

    boolean canGoForward();

    void goForward();

    String getTitle();

    String getHost();

}
