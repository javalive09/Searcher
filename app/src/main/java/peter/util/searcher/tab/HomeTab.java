package peter.util.searcher.tab;

import android.view.View;
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
        mainActivity.findViewById(R.id.searcher_button)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.startSearcheActivity();
            }
        });
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
        return "home";
    }

    @Override
    public String getUrl() {
        return URL_HOME;
    }


}
