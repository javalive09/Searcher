package peter.util.searcher.tab;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class FavoriteTab extends LocalViewTab {

    public FavoriteTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_favorite;
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
        return mainActivity.getString(R.string.fast_enter_favorite);
    }

    @Override
    public String getUrl() {
        return URL_FAVORITE;
    }

}
