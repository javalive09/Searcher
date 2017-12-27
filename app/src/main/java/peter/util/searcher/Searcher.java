package peter.util.searcher;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

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
        Stetho.initializeWithDefaults(this);
    }

}
