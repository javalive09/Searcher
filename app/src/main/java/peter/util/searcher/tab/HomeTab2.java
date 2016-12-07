package peter.util.searcher.tab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class HomeTab2 extends LocalViewTab {

    public HomeTab2(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_home2;
    }

    @Override
    public void onCreate() {
        TextView version = (TextView) mainActivity.findViewById(R.id.version);
        version.setText(mainActivity.getVersionName());
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
        return mainActivity.getString(R.string.home);
    }

    @Override
    public String getUrl() {
        return URL_HOME;
    }



}
