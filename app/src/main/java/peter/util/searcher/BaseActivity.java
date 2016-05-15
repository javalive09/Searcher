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
public class BaseActivity extends Activity implements View.OnClickListener{

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.favorite_item:
            case R.id.history_item:
                Bean bean = (Bean) v.getTag();
                if(bean != null) {
                    Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                    intent.putExtra("url", bean.url);
                    intent.putExtra("name", bean.name);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

}
