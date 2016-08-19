package peter.util.searcher;

import android.app.Application;
import peter.util.searcher.net.RequestManager;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
/**
 * Created by peter on 16/5/19.
 */
public class Searcher extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RequestManager.init(this);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5775fcb4");
    }

}
