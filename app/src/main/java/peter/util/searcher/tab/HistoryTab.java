package peter.util.searcher.tab;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class HistoryTab extends LocalViewTab {

    public HistoryTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_history;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestory() {

    }

    @Override
    public String getSearchWord() {
        return null;
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.fast_enter_history);
    }

    @Override
    public String getUrl() {
        return URL_FAVORITE;
    }

}
