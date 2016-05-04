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
                        UpdateController.instance(getApplicationContext()).checkVersion(dialogProvider, true);
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


    private DialogProvider dialogProvider = new DialogProvider() {
        @Override
        public ProgressDialog initProgress() {
            ProgressDialog mUpdateProgressDialog = new ProgressDialog(SettingActivity.this);
            mUpdateProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            mUpdateProgressDialog.setTitle(R.string.update_dialog_title);
            mUpdateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mUpdateProgressDialog.setMax(UpdateController.MAX_PROGRESS);
            mUpdateProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getText(R.string.update_dialog_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
            return mUpdateProgressDialog;
        }

        @Override
        public AlertDialog initAlert(final String url) {
            AlertDialog updateDialog = new AlertDialog.Builder(SettingActivity.this)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(R.string.update_dialog_title_one)
                    .setPositiveButton(R.string.update_dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            UpdateController.instance(getApplicationContext()).doDownloadApk(url, dialogProvider);
                        }
                    })
                    .setNegativeButton(R.string.update_dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).create();
            return updateDialog;
        }
    };

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("peter", "onDestroy");
        dialogProvider.end();
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
