package peter.util.searcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import peter.util.searcher.factory.BooleanFactory;

/**
 * Created by peter on 16/5/9.
 */
public class BaseActivity extends AppCompatActivity{

    public static final String ACTION_INNER_BROWSE = "peter.util.searcher.inner";
    public static final String NEW_TAB = "peter.util.searcher.new.tab";
    public static final String NAME_URL = "peter.util.searcher.url";
    public static final String NAME_WORD = "peter.util.searcher.word";
    public static final String NAME_LAUNCHER = "peter.util.searcher.launcher";
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

    protected void exit() {
        for(Activity act: LIST) {
            act.finish();
        }
        SearcherWebViewManager.instance().clear();
    }

    public void startHome(boolean isNewTab) {
        Intent intent = new Intent(BaseActivity.this, EnterActivity.class);
        intent.putExtra(NEW_TAB, isNewTab);
        startActivity(intent);
    }

    public void startLauncher() {
//        Intent intent = new Intent(BaseActivity.this, EnterActivity.class);
        Intent intent = new Intent(BaseActivity.this, SearchActivity.class);
        intent.putExtra(NEW_TAB, false);
        intent.putExtra(NAME_LAUNCHER, true);
        startActivity(intent);
//        finish();
    }

    public boolean isNewTab(Intent intent){
        boolean isNewTab = intent.getBooleanExtra(NEW_TAB, true);
        return isNewTab;
    }

    public boolean isLaunchTab(Intent intent) {
        boolean isLaunchTab = intent.getBooleanExtra(NAME_LAUNCHER, false);
        return isLaunchTab;
    }

    public void startBrowserFromSearch(Activity act, String url, String word) {
        startBrowser(act, url, word, isNewTab(getIntent()));
        act.finish();
    }

    public void startFavAct() {
        Intent intent = new Intent(BaseActivity.this, FavoriteActivity.class);
        intent.putExtra(NEW_TAB, isNewTab(getIntent()));
        startActivity(intent);
    }

    public void startHistoryAct() {
        Intent intent = new Intent(BaseActivity.this, HistoryActivity.class);
        intent.putExtra(NEW_TAB, isNewTab(getIntent()));
        startActivity(intent);
    }

    public void startHistorUrlyAct() {
        Intent intent = new Intent(BaseActivity.this, HistoryURLActivity.class);
        intent.putExtra(NEW_TAB, isNewTab(getIntent()));
        startActivity(intent);
    }

    private void startBrowser(Activity act, String url, String word, boolean isNewTab) {
        Intent intent = new Intent(act, MainActivity.class);
        intent.setAction(ACTION_INNER_BROWSE);
        intent.putExtra(NAME_URL, url);
        intent.putExtra(NAME_WORD, word);
        intent.putExtra(NEW_TAB, isNewTab);
        act.startActivity(intent);
        act.finish();
    }

    public void startBrowser(Activity act, String url, String word) {
        startBrowser(act, url, word, isNewTab(getIntent()));
    }

    public void switchBrowser(Activity act, String url) {
        startBrowser(act, url, "", false);
    }

    public void startSearch(boolean isNewTab) {
        Intent intent = new Intent(BaseActivity.this, SearchActivity.class);
        intent.putExtra(NEW_TAB,isNewTab);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NEW_TAB, isNewTab(getIntent()));
        outState.putBoolean(NAME_LAUNCHER, isLaunchTab(getIntent()));
    }

    protected int clearCacheFolder(final File dir, final int numDays) {
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
    protected void ClearCookies(Context context) {
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

    protected void sendMailByIntent() {
        Intent data=new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse(getString(R.string.setting_feedback_address)));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body));
        startActivity(data);
    }

    protected AlertDialog showAlertDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(BaseActivity.this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.show();
        return dialog;
    }

    protected String getVersionName() {
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

}
