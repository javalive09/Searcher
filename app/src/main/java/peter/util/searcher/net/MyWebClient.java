package peter.util.searcher.net;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import peter.util.searcher.R;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.tab.WebViewTab;
import peter.util.searcher.utils.Constants;
import peter.util.searcher.utils.IntentUtils;
import peter.util.searcher.utils.Utils;

/**
 * webView 配置的WebViewClient
 * Created by peter on 16/6/6.
 */
public class MyWebClient extends WebViewClient {
    private MainActivity mainActivity;
    private IntentUtils mIntentUtils;

    public MyWebClient(MainActivity activity) {
        this.mainActivity = activity;
        mIntentUtils = new IntentUtils(activity);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (view.isShown()) {
            view.postInvalidate();
        }
        mainActivity.refreshTitle();
        mainActivity.refreshProgress(100);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i("peter", "url=" + url);
        mainActivity.showTopbar();
    }

    @Override
    public void onReceivedHttpAuthRequest(final WebView view, @NonNull final HttpAuthHandler handler,
                                          final String host, final String realm) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        final EditText name = new EditText(mainActivity);
        final EditText password = new EditText(mainActivity);
        LinearLayout passLayout = new LinearLayout(mainActivity);
        passLayout.setOrientation(LinearLayout.VERTICAL);

        passLayout.addView(name);
        passLayout.addView(password);

        name.setHint(mainActivity.getString(R.string.hint_username));
        name.setSingleLine();
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSingleLine();
        password.setTransformationMethod(new PasswordTransformationMethod());
        password.setHint(mainActivity.getString(R.string.hint_password));
        builder.setTitle(mainActivity.getString(R.string.title_sign_in));
        builder.setView(passLayout);
        builder.setCancelable(true)
                .setPositiveButton(mainActivity.getString(R.string.title_sign_in),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String user = name.getText().toString();
                                String pass = password.getText().toString();
                                handler.proceed(user.trim(), pass.trim());

                            }
                        })
                .setNegativeButton(mainActivity.getString(R.string.action_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull final SslErrorHandler handler, @NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = getAllSslErrorMessageCodes(error);
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer messageCode : errorCodeMessageCodes) {
            stringBuilder.append(" - ").append(mainActivity.getString(messageCode)).append('\n');
        }
        String alertMessage =
                mainActivity.getString(R.string.message_insecure_connection, stringBuilder.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(mainActivity.getString(R.string.title_warning));
        builder.setMessage(alertMessage)
                .setCancelable(true)
                .setPositiveButton(mainActivity.getString(R.string.action_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.proceed();
                            }
                        })
                .setNegativeButton(mainActivity.getString(R.string.action_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.cancel();
                            }
                        });
        builder.create().show();
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
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
        return shouldOverrideLoading(view, request.getUrl().toString()) || super.shouldOverrideUrlLoading(view, request);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
        return shouldOverrideLoading(view, url) || super.shouldOverrideUrlLoading(view, url);
    }

    public boolean shouldOverrideLoading(@NonNull WebView view, @NonNull String url) {
        WebViewTab webViewTab = (WebViewTab) mainActivity.getTabManager().getCurrentTabGroup().getCurrentTab();
        Map<String, String> headers = webViewTab.getRequestHeaders();
        if (url.startsWith(Constants.ABOUT)) {
            return continueLoadingUrl(view, url, headers);
        }
        if (isMailOrIntent(url, view) || mIntentUtils.startActivityForUrl(view, url)) {
            return true;
        }
        return continueLoadingUrl(view, url, headers);
    }

    private boolean continueLoadingUrl(@NonNull WebView webView,
                                       @NonNull String url,
                                       @NonNull Map<String, String> headers) {
        if (headers.isEmpty()) {
            return false;
        } else {
            webView.loadUrl(url, headers);
            return true;
        }
    }

    private boolean isMailOrIntent(@NonNull String url, @NonNull WebView view) {
        if (url.startsWith(Constants.MAIL_SCHAME)) {
            MailTo mailTo = MailTo.parse(url);
            Intent i = Utils.newEmailIntent(mailTo.getTo(), mailTo.getSubject(),
                    mailTo.getBody(), mailTo.getCc());
            mainActivity.startActivity(i);
            view.reload();
            return true;
        } else if (url.startsWith(Constants.INTENT_SCHAME)) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException ignored) {
                intent = null;
            }
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
                try {
                    mainActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e("MyWebClient", "ActivityNotFoundException");
                }
                return true;
            }
        }
        return false;
    }

}
