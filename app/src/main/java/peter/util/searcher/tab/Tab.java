package peter.util.searcher.tab;


import peter.util.searcher.bean.Bean;

/**
 *
 * Created by peter on 2016/11/17.
 */

public interface Tab {

    String LOCAL_SCHEMA = "local://";
    String NEW_WINDOW = "new_window";
    String URL_HOME2 = LOCAL_SCHEMA + "home2";
    String URL_SETTING = LOCAL_SCHEMA + "setting";
    String URL_FAVORITE = LOCAL_SCHEMA + "toolbar_ic_favorite";
    String URL_HISTORY_SEARCH = LOCAL_SCHEMA + "history_search";
    String URL_HISTORY_URL = LOCAL_SCHEMA + "history_url";

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
