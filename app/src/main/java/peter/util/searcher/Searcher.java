package peter.util.searcher;

import android.app.Application;
import android.content.Context;

import peter.util.searcher.net.RequestManager;
/**
 * Created by peter on 16/5/19.
 */
public class Searcher extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        RequestManager.init(this);
    }

    public static Context context() {
        return mContext;
    }

}
