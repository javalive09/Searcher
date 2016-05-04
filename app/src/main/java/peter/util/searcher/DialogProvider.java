package peter.util.searcher;

import android.app.AlertDialog;
import android.app.ProgressDialog;

public class DialogProvider {

    private boolean end;
    AlertDialog alertDialog;
    ProgressDialog progressDialog;

    public boolean isEnd() {
        return end;
    }

    public void end() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        end = true;
    }

    public final AlertDialog getAlertDialog(String url) {
        if(alertDialog == null) {
            alertDialog = initAlert(url);
        }
        return alertDialog;
    }

    public final ProgressDialog getProgressDialog() {
        if(progressDialog == null) {
            progressDialog = initProgress();
        }
        return progressDialog;
    }

    public AlertDialog initAlert(String url) {
        return null;
    }

    public ProgressDialog initProgress() {
        return null;
    }

}