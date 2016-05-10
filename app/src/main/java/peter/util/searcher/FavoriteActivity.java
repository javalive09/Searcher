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
public class FavoriteActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        findViewById(R.id.back).setOnClickListener(this);
        new AsyncTask<Void, Void, List<Bean>>() {


            @Override
            protected List<Bean> doInBackground(Void... params) {
                List<Bean> searches = null;
                try {
                    searches = SqliteHelper.instance(FavoriteActivity.this).queryAllFavorite();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return searches;
            }

            @Override
            protected void onPostExecute(List<Bean> beans) {
                super.onPostExecute(beans);
                if (beans != null) {
                    if(!isFinishing() && !isActivityDestroyed()) {
                        ListView history = (ListView) findViewById(R.id.history);
                        history.setAdapter(new FavoriteAdapter(beans, FavoriteActivity.this));
                    }
                }

            }
        }.execute();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            default:
                Bean bean = (Bean) v.getTag();
                if (bean != null) {
                    Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
                    intent.putExtra("url", bean.url);
                    intent.putExtra("name", bean.name);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

}