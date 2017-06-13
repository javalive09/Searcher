package peter.util.searcher;

import android.app.Application;
import android.content.SharedPreferences;

//import com.facebook.stetho.Stetho;

import peter.util.searcher.net.CommonRetrofit;
import peter.util.searcher.utils.Constants;

/**
 * Created by peter on 16/5/19.
 */
public class Searcher extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CommonRetrofit.getInstance().init(this);
        Setting.getInstance().init(this);
//        Stetho.initializeWithDefaults(this);
    }



}
