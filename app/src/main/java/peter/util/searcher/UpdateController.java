package peter.util.searcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

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

/**
 * Created by peter on 16/5/4.
 */
public class UpdateController {

    private static UpdateController controller;

    private static final String URL = "https://raw.githubusercontent.com/javalive09/config/master/searcher_update_info";

    boolean isChecking = false;

    Context mContext;

    public static int MAX_PROGRESS = 100;

    private String APK_NAME = "searcher.apk";

    private UpdateController(Context context) {
        mContext = context;
    }

    public static UpdateController instance(Context context) {
        synchronized (UpdateController.class) {
            if (controller == null) {
                controller = new UpdateController(context);
            }
            return controller;
        }
    }

    protected synchronized void checkVersion(final AsynWindowHandler handler, final Boolean showToast) {
        if (isChecking) {
            return;
        }
        isChecking = true;
        setCheckedTime(System.currentTimeMillis());

        if(handler.isActDestory()) {
            return;
        }

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (showToast) {
                    Toast.makeText(mContext, R.string.update_toast_start, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                return doGetVersionInfo(URL);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                isChecking = false;
                if (!TextUtils.isEmpty(result) && result.contains(";")) {
                    String[] results = result.split(";");
                    int version = Integer.valueOf(results[0].trim());
                    int currentVersion = getVersionCode();
                    if (currentVersion < version) {
                        if(results.length > 1) {
                            String url = results[1].trim();
                            if (!TextUtils.isEmpty(url)) {
                                String content = "";
                                if(results.length > 2) {
                                    for (int i =2, len = results.length - 1; i < len; i++) {
                                        content += results[i].trim();
                                    }
                                }

                                Message msg = Message.obtain();
                                msg.what = AsynWindowHandler.SHOW_NEW_UPDATE_DIALOG;
                                Bundle bundle = new Bundle();
                                bundle.putString("version", version+ "");
                                bundle.putString("url", url);
                                bundle.putString("content", content);
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }
                        }
                    } else {
                        if (showToast) {
                            Toast.makeText(mContext, R.string.update_toast_nonew, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }.execute();
    }

    //TODO:  download manager do it is good !
    public void doDownloadApk(final String apkUrl, final AsynWindowHandler handler) {

        handler.sendEmptyMessage(AsynWindowHandler.SHOW_UPDATE_PROGRESS);
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
                    OutputStream os = new FileOutputStream(new File(mContext.getExternalCacheDir(), APK_NAME));
                    byte[] buffer = new byte[1024];
                    int len;
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

                Message msg = Message.obtain();
                msg.what = AsynWindowHandler.INCREASE_UPDATE_PROGRESS;
                msg.arg1 = values[0];
                handler.sendMessage(msg);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (progress >= MAX_PROGRESS) {
                    Message msg = Message.obtain();
                    msg.what = AsynWindowHandler.DISMISS_UPDATE_PROGRESS;
                    handler.sendMessage(msg);
                    installApk(new File(mContext.getExternalCacheDir(), APK_NAME).getAbsolutePath());
                }
            }
        }.execute();

    }

    private void installApk(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    private String doGetVersionInfo(String urlStr) {
        URL url;
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

    private int getVersionCode() {//获取版本号(内部识别号)
        try {
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected void autoCheckVersion(final AsynWindowHandler handler) {
        long time = getCheckedTime();
        if (time == 0) {
            checkVersion(handler, false);
        } else {
            long delta = System.currentTimeMillis() - time;
            if (delta > 1000 * 60 * 60 * 24) {//24h
                checkVersion(handler, false);
            }
        }
    }

    private long getCheckedTime() {
        return mContext.getSharedPreferences("updateTime", Context.MODE_PRIVATE).getLong("lastTime", 0);
    }

    private void setCheckedTime(long time) {
        mContext.getSharedPreferences("updateTime", Context.MODE_PRIVATE).edit().putLong("lastTime",
                time).commit();
    }

}
