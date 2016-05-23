package peter.util.searcher;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by peter on 16/5/19.
 */
public class HotTopActivity extends BaseActivity {

    private static final String URL = "http://top.baidu.com/buzz.php?p=top10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_top);
        new HotTask(HotTopActivity.this).execute();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enter:
                startSearch(null);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Board board = (Board) findViewById(R.id.hot_top);
        board.startAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Board board = (Board) findViewById(R.id.hot_top);
        board.stopAnimation();
    }

    private void showHots(ArrayList<String> hots) {
        Board board = (Board) findViewById(R.id.hot_top);
        board.startAnimation(hots);
    }

    static class HotTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private int LIMIT = 10;
        WeakReference<HotTopActivity> act;

        public HotTask(HotTopActivity a) {
            act = new WeakReference<>(a);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> hots = new ArrayList<>(LIMIT);
            try {
                Document doc = Jsoup.connect(URL).get();
                Elements es = doc.getElementsByClass("list-title");

                for(Element e : es) {
                    if(hots.size() < LIMIT) {
                        hots.add(e.text());
                    }else{
                        break;
                    }
                }
                Log.i("peter", "title = " + hots.toString());
                HotTopActivity activity = act.get();
                if(activity != null) {
                    activity.showHots(hots);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hots;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            HotTopActivity activity = act.get();
            if(activity != null) {
                activity.showHots(list);
            }
        }
    }


}
