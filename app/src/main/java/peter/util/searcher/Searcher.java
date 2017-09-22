package peter.util.searcher;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.net.CommonRetrofit;

/**
 * application 入口
 * Created by peter on 16/5/19.
 */
public class Searcher extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        context = this;
        super.onCreate();
        CommonRetrofit.getInstance().init(this);
        SettingsManager.getInstance().init(this);
        DaoManager.getInstance().init(this);
        Stetho.initializeWithDefaults(this);
    }

}
