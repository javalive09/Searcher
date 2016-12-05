package peter.util.searcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;

import peter.util.searcher.R;

/**
 *
 * Created by peter on 2016/12/2.
 */

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MessageQueue.IdleHandler handler = new MessageQueue.IdleHandler() {

            @Override
            public boolean queueIdle() {

                //long time consuming Operating
                try {
                    Thread.sleep(1000);//耗时操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                return false;
            }

        };
        Looper.myQueue().addIdleHandler(handler);
    }

}
