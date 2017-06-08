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
        Constants.AUTO_FULLSCREEN = getAutoFullScreen();
//        Stetho.initializeWithDefaults(this);
    }

    private boolean getAutoFullScreen() {
        SharedPreferences sp = getSharedPreferences("fullscreen", MODE_PRIVATE);
        boolean show = sp.getBoolean("auto", false);
        return show;
    }

}
