package peter.util.searcher.net;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import peter.util.searcher.R;
import peter.util.searcher.db.SqliteHelper;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.fragment.WebViewFragment;
import peter.util.searcher.utils.IntentUtils;
import peter.util.searcher.utils.Utils;

/**
 * Created by peter on 16/6/6.
 */
public class MyWebClient extends WebViewClient {
    private WebViewFragment fragment;
    IntentUtils mIntentUtils;

    public MyWebClient(WebViewFragment fragment) {
        this.fragment = fragment;
        mIntentUtils = new IntentUtils(fragment.getActivity());
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if(view.isShown()) {
            view.postInvalidate();
        }
        fragment.setStatusLevel(0);
        saveUrlData(fragment.getWebViewTitle(), url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        fragment.setStatusLevel(1);
        Log.i("peter", "url=" + url);
    }

    private void saveUrlData(final String title, final String url) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bean search = new Bean();
                search.name = TextUtils.isEmpty(title) ? url : title;
                search.time = System.currentTimeMillis();
                search.url = url;
                SqliteHelper.instance(fragment.getActivity()).insertHistoryURL(search);
                return null;
            }
        }.execute();
    }

    @Override
    public void onReceivedHttpAuthRequest(final WebView view, @NonNull final HttpAuthHandler handler,
                                          final String host, final String realm) {

        Activity mActivity = fragment.getActivity();
        if(mActivity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            final EditText name = new EditText(mActivity);
            final EditText password = new EditText(mActivity);
            LinearLayout passLayout = new LinearLayout(mActivity);
            passLayout.setOrientation(LinearLayout.VERTICAL);

            passLayout.addView(name);
            passLayout.addView(password);

            name.setHint(mActivity.getString(R.string.hint_username));
            name.setSingleLine();
            password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            password.setSingleLine();
            password.setTransformationMethod(new PasswordTransformationMethod());
            password.setHint(mActivity.getString(R.string.hint_password));
            builder.setTitle(mActivity.getString(R.string.title_sign_in));
            builder.setView(passLayout);
            builder.setCancelable(true)
                    .setPositiveButton(mActivity.getString(R.string.title_sign_in),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    String user = name.getText().toString();
                                    String pass = password.getText().toString();
                                    handler.proceed(user.trim(), pass.trim());

                                }
                            })
                    .setNegativeButton(mActivity.getString(R.string.action_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    handler.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull final SslErrorHandler handler, @NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = getAllSslErrorMessageCodes(error);
        Activity mActivity = fragment.getActivity();
        if(mActivity != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Integer messageCode : errorCodeMessageCodes) {
                stringBuilder.append(" - ").append(fragment.getString(messageCode)).append('\n');
            }
            String alertMessage =
                    mActivity.getString(R.string.message_insecure_connection, stringBuilder.toString());

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mActivity.getString(R.string.title_warning));
            builder.setMessage(alertMessage)
                    .setCancelable(true)
                    .setPositiveButton(mActivity.getString(R.string.action_yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    handler.proceed();
                                }
                            })
                    .setNegativeButton(mActivity.getString(R.string.action_no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    handler.cancel();
                                }
                            });
            builder.create().show();
        }
    }

    @NonNull
    private static List<Integer> getAllSslErrorMessageCodes(@NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = new ArrayList<>(1);

        if (error.hasError(SslError.SSL_DATE_INVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_date_invalid);
        }
        if (error.hasError(SslError.SSL_EXPIRED)) {
            errorCodeMessageCodes.add(R.string.message_certificate_expired);
        }
        if (error.hasError(SslError.SSL_IDMISMATCH)) {
            errorCodeMessageCodes.add(R.string.message_certificate_domain_mismatch);
        }
        if (error.hasError(SslError.SSL_NOTYETVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_not_yet_valid);
        }
        if (error.hasError(SslError.SSL_UNTRUSTED)) {
            errorCodeMessageCodes.add(R.string.message_certificate_untrusted);
        }
        if (error.hasError(SslError.SSL_INVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_invalid);
        }

        return errorCodeMessageCodes;
    }

    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
        if (url.startsWith("about:") && Utils.doesSupportHeaders()) {
            view.loadUrl(url);
            return true;
        }
        if (url.startsWith("mailto:")) {
            MailTo mailTo = MailTo.parse(url);
            Intent i = Utils.newEmailIntent(mailTo.getTo(), mailTo.getSubject(),
                    mailTo.getBody(), mailTo.getCc());
            fragment.startActivity(i);
            view.reload();
            return true;
        } else if (url.startsWith("intent://")) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException ignored) {
                intent = null;
            }
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    intent.setSelector(null);
                }
                try {
                    fragment.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return mIntentUtils.startActivityForUrl(view, url);
    }

}
