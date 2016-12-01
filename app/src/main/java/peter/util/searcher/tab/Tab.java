package peter.util.searcher.tab;


/**
 *
 * Created by peter on 2016/11/17.
 */

public interface Tab {

    String LOCAL_SCHEMA = "local://";
    String NEW_WINDOW = "new_window";
    String URL_HOME = LOCAL_SCHEMA + "home";
    String URL_SETTING = LOCAL_SCHEMA + "setting";
    String URL_FAVORITE = LOCAL_SCHEMA + "favorite";
    String URL_HISTORY = LOCAL_SCHEMA + "history";

    void loadUrl(String url, String searchWord);

    String getUrl();

    String getSearchWord();

    void reload();

    boolean canGoBack();

    void goBack();

    boolean canGoForward();

    void goForward();

    String getTitle();

}
