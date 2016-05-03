package peter.util.searcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class SettingActivity extends Activity {

    int currentWebEngine;
    int MAX_PROGRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getVersionName());
        ListView settings = (ListView) findViewById(R.id.setting_list);
        settings.setAdapter(new ArrayAdapter<>(this, R.layout.setting_item, getResources().getStringArray(R.array.settings_name)));
        settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://engine
                        showEngineDialog();
                        break;
                    case 1://clear
                        clearCacheFolder(getCacheDir(), 0);
                        ClearCookies(SettingActivity.this);
                        Toast.makeText(SettingActivity.this, R.string.setting_clear, Toast.LENGTH_LONG).show();
                        break;
                    case 2://feedback
                        sendMailByIntent();
                        break;
                    case 3://update
                        checkVersion("http://7xoxmg.com1.z0.glb.clouddn.com/searcher_update_info");
                        break;
                    case 4://about
                        Toast.makeText(SettingActivity.this, R.string.setting_about, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            currentWebEngine = intent.getIntExtra("currentWebEngine", 0);
        }

    }

    private int getVersionCode() {//获取版本号(内部识别号)
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void checkVersion(final String url) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(SettingActivity.this, R.string.update_toast_start, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                return doGetVersionInfo(url);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                String[] results = result.split(";");
                int version = Integer.valueOf(results[0].trim());
                int currentVersion = getVersionCode();
                if (currentVersion < version) {
                    String url = results[1].trim();
                    if (!TextUtils.isEmpty(url)) {
                        showUpdataDialog(url);
                    }
                } else {
                    Toast.makeText(SettingActivity.this, R.string.update_toast_nonew, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void showUpdataDialog(final String url) {
        new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.update_dialog_title_one)
                .setPositiveButton(R.string.update_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doDownloadApk(url);
                    }
                })
                .setNegativeButton(R.string.update_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .create().show();
    }

    private void installApk(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + path),"application/vnd.android.package-archive");
        startActivity(intent);
    }

    private ProgressDialog getProgressDialog() {
        ProgressDialog mProgressDialog = new ProgressDialog(SettingActivity.this);
        mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
        mProgressDialog.setTitle(R.string.update_dialog_title);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(MAX_PROGRESS);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getText(R.string.update_dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked No so do some stuff */
                    }
                });
        return mProgressDialog;
    }

    private void doDownloadApk(final String apkUrl) {

        final ProgressDialog mProgressDialog = getProgressDialog();
        mProgressDialog.show();

        new AsyncTask<Void, Integer, String>() {

            int count;
            boolean finished;
            int current;
            int progress;

            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(apkUrl);
                    URLConnection conn = url.openConnection();
                    count = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    OutputStream os = new FileOutputStream(new File(getExternalFilesDir(null), "shuihu.apk"));
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while (!finished) {
                        while ((len = is.read(buffer)) > 0) {
                            current += len;
                            os.write(buffer, 0, len);
                            progress = current * 100 / count;
                            publishProgress(progress);
                        }
                        finished = true;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return progress + "";
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                mProgressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (progress >= MAX_PROGRESS) {
                    mProgressDialog.dismiss();
                    installApk(new File(getExternalFilesDir(null), "shuihu.apk").getAbsolutePath());
                }

            }
        }.execute();

    }

    private String doGetVersionInfo(String urlStr) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5 * 1000);
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
            }
            conn.disconnect();
        }
        return null;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void showEngineDialog() {
        new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.engine_title)
                .setSingleChoiceItems(R.array.engine_web_names, currentWebEngine, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(whichButton != currentWebEngine) {
                            currentWebEngine = whichButton;
                            getSharedPreferences("setting", MODE_PRIVATE).edit().putInt("engine", currentWebEngine).commit();
                            Intent intent = new Intent();
                            intent.putExtra("currentWebEngine", currentWebEngine);
                            setResult(RESULT_OK, intent);
                        }
                        dialog.dismiss();
                    }
                }).create().show();
    }


    private String getVersionName() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;

        if (TextUtils.isEmpty(version)) {
            return "";
        } else {
            return "version " + version;
        }
    }

    private int clearCacheFolder(final File dir, final int numDays) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("peter", String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    @SuppressWarnings("deprecation")
    private void ClearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    public void sendMailByIntent() {
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse(getString(R.string.setting_feedback_address)));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body));
        startActivity(data);
    }

}
