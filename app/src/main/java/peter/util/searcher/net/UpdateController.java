package peter.util.searcher.net;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.bean.UrlInfo;
import peter.util.searcher.bean.VersionInfo;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by peter on 16/5/4.
 */
public class UpdateController {

    private static class HOLDER {
        private static final UpdateController INSTANCE = new UpdateController();
    }

    private UpdateController() {
    }

    public static UpdateController instance() {
        return HOLDER.INSTANCE;
    }

    private void showUpdateDialog(BaseActivity act, final VersionInfo versionInfo) {
        final Context context = act.getApplicationContext();
        AlertDialog dialog = new AlertDialog.Builder(act)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(act.getString(R.string.update_dialog_title_one))
                .setMessage(versionInfo.getMessage())
                .setPositiveButton(R.string.update_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        downloadApk(context, versionInfo.getUrl());
                    }
                })
                .setNegativeButton(R.string.update_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void checkVersion(BaseActivity act, final boolean showToast) {
        final WeakReference<BaseActivity> actHolder = new WeakReference<>(act);
        setCheckedTime(act, System.currentTimeMillis());
        if (showToast) {
            Toast.makeText(act, R.string.update_toast_start, Toast.LENGTH_SHORT).show();
        }
        final IVersionService iVersionService = CommonRetrofit.getInstance().getRetrofit().create(IVersionService.class);
        iVersionService.getUrl(IVersionService.URL).retry(5).subscribeOn(Schedulers.io()).flatMap(new Func1<UrlInfo, Observable<VersionInfo>>() {
            @Override
            public Observable<VersionInfo> call(UrlInfo urlInfo) {
                return iVersionService.getInfo(urlInfo.getUrl());
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<VersionInfo>() {

                    @Override
                    public void call(VersionInfo versionInfo) {
                        BaseActivity holdAct = actHolder.get();
                        if (holdAct != null) {
                            if (holdAct.getVersionCode() < versionInfo.getCode()) {
                                showUpdateDialog(holdAct, versionInfo);
                            } else {
                                if(showToast) {
                                    Toast.makeText(holdAct, R.string.update_toast_nonew, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        BaseActivity holdAct = actHolder.get();
                        if (holdAct != null) {
                            Toast.makeText(holdAct, R.string.update_toast_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void downloadApk(final Context context, String url) {
        final Uri uri = Uri.parse(url);
        DownloadManager.Request req = new DownloadManager.Request(uri);
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        req.setDescription(context.getString(R.string.update_toast_des));
        final String mineType = DownloadHandler.getMimeType(url);
        req.setMimeType(mineType);
        String filename = URLUtil.guessFileName(url, "", mineType);
        req.setTitle(filename);
        String location = DownloadHandler.addNecessarySlashes(context.getExternalFilesDir(null).toString());
        final Uri customUri = Uri.parse(DownloadHandler.FILE + location + filename);
        req.setDestinationUri(customUri);

        // Ok go!
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = dm.enqueue(req);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                long currentId = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                if (currentId == downloadId) {
                    DownloadHandler.openFile(customUri, context);
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void autoCheckVersion(BaseActivity act) {
        long time = getCheckedTime(act);
        if (time == 0) {
            checkVersion(act, false);
        } else {
            long delta = System.currentTimeMillis() - time;
            if (delta > 1000 * 60 * 60 * 24) {//24h
                checkVersion(act, false);
            }
        }
    }

    private long getCheckedTime(BaseActivity act) {
        return act.getSharedPreferences("updateTime", Context.MODE_PRIVATE).getLong("lastTime", 0);
    }

    private void setCheckedTime(BaseActivity act, long time) {
        act.getSharedPreferences("updateTime", Context.MODE_PRIVATE).edit().putLong("lastTime",
                time).apply();
    }

}
