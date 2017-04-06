package peter.util.searcher.tab;


/**
 *
 * Created by peter on 2016/11/17.
 */

public interface Tab {

    String LOCAL_SCHEMA = "local://";
    String NEW_WINDOW = "new_window";
    String URL_HOME = LOCAL_SCHEMA + "home";
    String URL_HOME2 = LOCAL_SCHEMA + "home2";
    String URL_HOME3 = LOCAL_SCHEMA + "home3";
    String URL_SETTING = LOCAL_SCHEMA + "setting";
    String URL_FAVORITE = LOCAL_SCHEMA + "favorite";
    String URL_HISTORY_SEARCH = LOCAL_SCHEMA + "history_search";
    String URL_HISTORY_URL = LOCAL_SCHEMA + "history_url";
    String URL_DOWNLOAD = LOCAL_SCHEMA + "download_url";

    void loadUrl(String url, String searchWord);

    String getUrl();

    String getSearchWord();

    void reload();

    boolean canGoBack();

    void goBack();

    boolean canGoForward();

    void goForward();

    String getTitle();

    String getHost();

}
