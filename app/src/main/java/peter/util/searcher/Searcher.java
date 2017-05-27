package peter.util.searcher;

import android.app.Application;

import peter.util.searcher.net.CommonRetrofit;

/**
 * Created by peter on 16/5/19.
 */
public class Searcher extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CommonRetrofit.getInstance().init(this);
    }

}
