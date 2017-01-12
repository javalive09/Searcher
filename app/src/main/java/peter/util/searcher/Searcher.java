package peter.util.searcher;

import android.app.Application;
import android.content.Context;

import peter.util.searcher.net.RequestManager;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
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
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5775fcb4");
    }

    public static Context context() {
        return mContext;
    }

}
