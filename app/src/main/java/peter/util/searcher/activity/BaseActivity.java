package peter.util.searcher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.bean.Bean;

/**
 * Created by peter on 16/5/9.
 */
public class BaseActivity extends AppCompatActivity {

    public static final String ACTION_INNER_BROWSE = "peter.util.searcher.inner";
    public static final String NAME_BEAN = "peter.util.searcher.bean";
    public static final String NAME_WORD = "peter.util.searcher.word";
    private static final ArrayList<Activity> LIST = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LIST.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LIST.remove(this);
    }

    public void exit() {
        for (Activity act : LIST) {
            act.finish();
        }
    }

    public String getSearchWord() {
        return "";
    }

    public void setSearchWord(String word) {
    }

    public void startBrowser(Bean bean) {
        Intent intent = new Intent(BaseActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setAction(ACTION_INNER_BROWSE);
        intent.putExtra(NAME_BEAN, bean);
        startActivity(intent);
    }

    public int clearCacheFolder(final File dir, final int numDays) {
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
    public void ClearCookies(Context context) {
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
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse(getString(R.string.setting_feedback_address)));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body));
        startActivity(data);
    }

    public AlertDialog showAlertDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(BaseActivity.this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.show();
        return dialog;
    }

    public AlertDialog showAlertDialog(int titleRes, int contentRes) {
        return showAlertDialog(getString(titleRes), getString(contentRes));
    }

    public String getVersionName() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String version = "";
        if (packInfo != null) {
            version = packInfo.versionName;
        }

        if (TextUtils.isEmpty(version)) {
            return "";
        } else {
            return "version " + version;
        }
    }

}
