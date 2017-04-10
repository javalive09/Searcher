package peter.util.searcher.tab;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.download.DownloadHandler;

/**
 * Created by peter on 2016/11/18.
 */

public class DownloadTab extends LocalViewTab implements View.OnClickListener, View.OnLongClickListener {

    PopupMenu popup;
    MyAsyncTask asyncTask;

    public DownloadTab(MainActivity activity) {
        super(activity);
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
    public int onCreateViewResId() {
        return R.layout.tab_download;
    }

    @Override
    public void onCreate() {
        refreshData();
    }

    @Override
    public void onDestory() {
    }

    @Override
    public String getSearchWord() {
        return "";
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.action_download);
    }

    @Override
    public String getUrl() {
        return URL_DOWNLOAD;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_item:
                File file = (File) v.getTag();
                if (file != null) {
                    Uri uri = Uri.fromFile(file);
                    DownloadHandler.openFile(uri, mainActivity);
                }
                break;
        }
    }

    private class DownloadAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        private List<File> list;

        public DownloadAdapter(List<File> objects) {
            factory = LayoutInflater.from(mainActivity);
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
            view.setOnClickListener(DownloadTab.this);
            view.setOnLongClickListener(DownloadTab.this);
            view.setTag(file);
            return view;
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
                File[] files = new File(mainActivity.getExternalFilesDir(null).toString()).listFiles();
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
                mainActivity.findViewById(R.id.loading).setVisibility(View.GONE);
                ListView download = (ListView) mainActivity.findViewById(R.id.download);
                if (download.getAdapter() == null) {
                    download.setAdapter(new DownloadAdapter(beans));
                } else {
                    ((DownloadAdapter) download.getAdapter()).updateData(beans);
                }
            }
        }

    }

    private void popupMenu(final View view) {
        dismissPopupMenu();
        popup = new PopupMenu(mainActivity, view);
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
