package peter.util.searcher.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;

/**
 * Created by peter on 16/5/9.
 */
public class EnterFragment extends BaseFragment implements View.OnClickListener {

    String url = "http://200code.com/[object%20Object]/Searcher%E5%BF%AB%E9%80%9F%E6%90%9C%E7%B4%A2/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_enter, container, false);
        View js = rootView.findViewById(R.id.js);
        js.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.js:
                BaseActivity activity = (BaseActivity) getActivity();
                activity.startBrowser(url, "");
                break;
        }
    }

}
