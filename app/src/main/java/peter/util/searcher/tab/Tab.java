package peter.util.searcher.tab;


import peter.util.searcher.bean.Bean;

/**
 *
 * Created by peter on 2016/11/17.
 */

public interface Tab {

    String LOCAL_SCHEMA = "local://";
    String URL_HOME = LOCAL_SCHEMA + "home";
    String ACTION_NEW_WINDOW = "new_window";

    void loadUrl(Bean bean);

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
