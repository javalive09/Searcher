package peter.util.searcher;

import android.app.Application;
import android.content.Context;

import peter.util.searcher.net.CommonRetrofit;
import peter.util.searcher.net.RequestManager;
import peter.util.searcher.update.UpdateController;

/**
 * Created by peter on 16/5/19.
 */
public class Searcher extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RequestManager.init(this);
        CommonRetrofit.getInstance().init(this);
    }

}
