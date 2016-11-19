package peter.util.searcher.tab;

import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class SettingTab extends LocalViewTab {

    public static final String URL = "local://setting";

    public SettingTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return 0;
    }

    @Override
    public String getTitle() {
        return "home";
    }

    @Override
    public String getUrl() {
        return URL;
    }

}
