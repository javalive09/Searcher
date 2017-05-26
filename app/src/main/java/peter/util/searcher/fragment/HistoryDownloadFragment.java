package peter.util.searcher.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.download.DownloadHandler;
import peter.util.searcher.tab.DownloadTab;

/**
 * Created by peter on 16/5/9.
 */
public class HistoryDownloadFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;
    @BindView((R.id.no_record))
    TextView noRecord;
    @BindView((R.id.download))
    ListView download;
    @BindView((R.id.loading))
    ProgressBar loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history_download, container, false);
        ButterKnife.bind(HistoryDownloadFragment.this, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();

    }

    private void refreshData() {
        cancelAsyncTask();
        asyncTask = new MyAsyncTask();
        asyncTask.execute();
    }

    private void cancelAsyncTask() {
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_item:
                File file = (File) v.getTag();
                if (file != null) {
                    Uri uri = Uri.fromFile(file);
                    DownloadHandler.openFile(uri, getActivity());
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.download_item:
                popupMenu(v);
                return true;
        }
        return false;
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, List<File>> {

        @Override
        protected List<File> doInBackground(Void... params) {
            List<File> list = null;
            try {
                File[] files = new File(getActivity().getExternalFilesDir(null).toString()).listFiles();
                list = Arrays.asList(files);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<File> beans) {
            super.onPostExecute(beans);
            if (beans != null) {
                if (beans.size() == 0) {
                    noRecord.setVisibility(View.VISIBLE);
                } else {
                    noRecord.setVisibility(View.GONE);
                }
                if (download.getAdapter() == null) {
                    download.setAdapter(new DownloadAdapter(beans));
                } else {
                    ((DownloadAdapter) download.getAdapter()).updateData(beans);
                }
            }
            loading.setVisibility(View.GONE);
        }

    }

    private class DownloadAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private List<File> list;

        public DownloadAdapter(List<File> objects) {
            factory = LayoutInflater.from(getActivity());
            list = objects;
        }

        public void updateData(List<File> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public File getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;

            if (convertView == null) {
                view = (TextView) factory.inflate(R.layout.download_item, parent, false);
            } else {
                view = (TextView) convertView;
            }

            File file = getItem(position);
            view.setText(file.getName());
            view.setOnClickListener(HistoryDownloadFragment.this);
            view.setOnLongClickListener(HistoryDownloadFragment.this);
            view.setTag(file);
            return view;
        }

    }


    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.item, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        File file = (File) view.getTag();
                        file.delete();
                        refreshData();
                        break;
                }

                return true;
            }
        });
        popup.show();
    }

    private void dismissPopupMenu() {
        if (popup != null) {
            popup.dismiss();
        }
    }


}
