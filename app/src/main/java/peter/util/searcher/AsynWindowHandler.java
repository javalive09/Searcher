package peter.util.searcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListPopupWindow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 16/5/5.
 */
public class AsynWindowHandler extends Handler {

    public static final int SHOW_NEW_UPDATE_DIALOG = 1;
    public static final int DISMISS_UPDATE_DIALOG = 2;

    public static final int SHOW_HINT_LIST = 3;
    public static final int UPDATE_HINT_LIST_DATA = 4;
    public static final int DISMISS_HINT_LIST = 5;

    public static final int SHOW_UPDATE_PROGRESS = 6;
    public static final int INCREASE_UPDATE_PROGRESS = 7;
    public static final int DISMISS_UPDATE_PROGRESS = 8;

    public static final int DESTROY = 9;

    private AlertDialog dialog;
    private ProgressDialog progress;
    private ListPopupWindow hintList;
    private HintAdapter hintAdapter;
    private boolean destroy;

    private WeakReference<Activity> mAct;

    public AsynWindowHandler(Activity act) {
        mAct = new WeakReference<>(act);
    }

    @Override
    public void handleMessage(Message msg) {
        if(destroy) {
            return;
        }
        final Activity act = mAct.get();
        if(act != null) {
            switch (msg.what) {
                case SHOW_NEW_UPDATE_DIALOG:
                    final String url = (String) msg.obj;
                    dialog = new AlertDialog.Builder(act)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setTitle(R.string.update_dialog_title_one)
                            .setPositiveButton(R.string.update_dialog_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    UpdateController.instance(act.getApplicationContext()).doDownloadApk(url, AsynWindowHandler.this);
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
                case SHOW_HINT_LIST:
                    if(act instanceof MainActivity) {
                        if(hintList == null) {
                            View search = (View) msg.obj;
                            MainActivity mainActivity = (MainActivity) act;
                            hintList = new ListPopupWindow(act);
                            hintAdapter = new HintAdapter(new ArrayList<Search>(1), mainActivity);
                            hintList.setAdapter(hintAdapter);
                            hintList.setAnchorView(search);
                            hintList.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                            hintList.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                            hintList.setModal(false);
                        }
                        if (!hintList.isShowing()) {
                            hintList.show();
                        }
                    }
                    break;
                case UPDATE_HINT_LIST_DATA:
                    if(hintList != null && hintAdapter != null) {
                        List<Search> objects = (List<Search>) msg.obj;
                        hintAdapter.updateData(objects);
                        if (!hintList.isShowing()) {
                            hintList.show();
                        }
                    }
                    break;
                case DISMISS_HINT_LIST:
                    if(hintList != null && hintList.isShowing()) {
                        hintList.dismiss();
                    }
                    break;
                case DESTROY:
                    destroy = true;
                    if(dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    if(progress != null && progress.isShowing()) {
                        progress.dismiss();
                    }

                    if(hintList != null && hintList.isShowing()) {
                        hintList.dismiss();
                    }

                    break;

            }
        }
    }

    public boolean isHintListShowing() {
        if(hintList != null && hintList.isShowing()) {
            return true;
        }
        return false;
    }

}
