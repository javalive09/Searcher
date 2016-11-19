package peter.util.searcher.tab;


/**
 * Created by peter on 2016/11/17.
 */

public interface Tab {

    void loadUrl(String url, String searchWord, boolean newTab);

    String getUrl();

    void reload();

    boolean canGoBack();

    void goBack();

    boolean canGoForward();

    void goForward();

    String getTitle();

}
