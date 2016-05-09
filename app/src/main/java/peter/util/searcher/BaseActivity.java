package peter.util.searcher;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

/**
 * Created by peter on 16/5/9.
 */
public class BaseActivity extends Activity {

    AsynWindowHandler windowHandler;
    private boolean isDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        if(windowHandler != null) {
            windowHandler.sendEmptyMessage(AsynWindowHandler.DESTROY);
        }
        super.onDestroy();
    }

    public boolean isActivityDestroyed() {
        return isDestroyed;
    }

}
