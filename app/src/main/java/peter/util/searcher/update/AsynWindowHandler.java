package peter.util.searcher.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

import peter.util.searcher.R;

/**
 * Created by peter on 16/5/5.
 */
public class AsynWindowHandler extends Handler {

    public static final int SHOW_NEW_UPDATE_DIALOG = 1;
    public static final int DISMISS_UPDATE_DIALOG = 2;

    public static final int SHOW_UPDATE_PROGRESS = 6;
    public static final int INCREASE_UPDATE_PROGRESS = 7;
    public static final int DISMISS_UPDATE_PROGRESS = 8;

    public static final int DESTROY = 9;

    private AlertDialog dialog;
    private ProgressDialog progress;

    private WeakReference<Activity> mAct;

    public AsynWindowHandler(Activity act) {
        mAct = new WeakReference<>(act);
    }

    public boolean isActDestory() {
        return mAct.get() == null;
    }

    @Override
    public void handleMessage(Message msg) {

        final Activity act = mAct.get();
        if(act != null) {
            switch (msg.what) {
                case SHOW_NEW_UPDATE_DIALOG:
                    final String url = msg.getData().getString("url");
                    final String content = msg.getData().getString("content");
                    dialog = new AlertDialog.Builder(act)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(act.getString(R.string.update_dialog_title_one))
                            .setMessage(content)
                            .setPositiveButton(R.string.update_dialog_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    UpdateController.instance().doDownloadApk(url, AsynWindowHandler.this);
                                }
                            })
                            .setNegativeButton(R.string.update_dialog_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }).create();
                    dialog.show();
                    break;
                case DISMISS_UPDATE_DIALOG:
                    if(dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;

                case SHOW_UPDATE_PROGRESS:
                    progress = new ProgressDialog(act);
                    progress.setIconAttribute(android.R.attr.alertDialogIcon);
                    progress.setTitle(R.string.update_dialog_title);
                    progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progress.setMax(UpdateController.MAX_PROGRESS);
                    progress.setButton(DialogInterface.BUTTON_NEGATIVE,
                            act.getText(R.string.update_dialog_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            });
                    progress.show();
                    break;
                case INCREASE_UPDATE_PROGRESS:
                    if(progress != null && progress.isShowing()) {
                        int value = msg.arg1;
                        progress.setProgress(value);
                    }
                    break;

                case DISMISS_UPDATE_PROGRESS:
                    if(progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }
                    break;
                case DESTROY:
                    if(dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    if(progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }

                    break;

            }
        }
    }

}
