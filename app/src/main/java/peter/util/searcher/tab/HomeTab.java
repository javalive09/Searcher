package peter.util.searcher.tab;

import android.graphics.drawable.Drawable;
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
    void onCreate() {
        TextView version = (TextView) mView.findViewById(R.id.version);
        version.setText(mainActivity.getVersionName());
    }

    @Override
    void onDestroy() {

    }

    @Override
    public String getSearchWord() {
        return "";
    }

    @Override
    public Drawable getIconDrawable() {
        return mainActivity.getResources().getDrawable(R.drawable.ic_website);
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.home);
    }

    @Override
    public String getUrl() {
        return URL_HOME;
    }

}
