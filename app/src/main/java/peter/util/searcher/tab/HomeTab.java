package peter.util.searcher.tab;

import android.widget.TextView;
import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class HomeTab extends LocalViewTab {

    public HomeTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_home;
    }

    @Override
    public void onCreate() {
        TextView version = (TextView) mainActivity.findViewById(R.id.version);
        version.setText(mainActivity.getVersionName());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public String getSearchWord() {
        return "";
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.home);
    }

    @Override
    public String getUrl() {
        return URL_HOME;
    }

    public String getHost() {
        return "";
    }

}
