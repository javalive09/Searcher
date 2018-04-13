package peter.util.searcher.net;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import peter.util.searcher.R;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.WebViewTab;
import peter.util.searcher.utils.Constants;
import peter.util.searcher.utils.IntentUtils;
import peter.util.searcher.utils.UrlUtils;
import peter.util.searcher.utils.Utils;

/**
 * webView 配置的WebViewClient
 * Created by peter on 16/6/6.
 */
public class MyWebClient extends WebViewClient {
    private final WebViewTab webViewTab;

    public MyWebClient(WebViewTab webViewTab) {
        this.webViewTab = webViewTab;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (view.isShown()) {
            view.postInvalidate();
        }
        webViewTab.getActivity().refreshTitle();
        webViewTab.getActivity().refreshProgress(webViewTab, 100);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i("peter", "url=" + url);
        webViewTab.getActivity().showTopbar();
    }

    @Override
    public void onReceivedHttpAuthRequest(final WebView view, @NonNull final HttpAuthHandler handler,
                                          final String host, final String realm) {

        AlertDialog.Builder builder = new AlertDialog.Builder(webViewTab.getActivity());
        final EditText name = new EditText(webViewTab.getActivity());
        final EditText password = new EditText(webViewTab.getActivity());
        LinearLayout passLayout = new LinearLayout(webViewTab.getActivity());
        passLayout.setOrientation(LinearLayout.VERTICAL);

        passLayout.addView(name);
        passLayout.addView(password);

        name.setHint(webViewTab.getActivity().getString(R.string.hint_username));
        name.setSingleLine();
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSingleLine();
        password.setTransformationMethod(new PasswordTransformationMethod());
        password.setHint(webViewTab.getActivity().getString(R.string.hint_password));
        builder.setTitle(webViewTab.getActivity().getString(R.string.title_sign_in));
        builder.setView(passLayout);
        builder.setCancelable(true)
                .setPositiveButton(webViewTab.getActivity().getString(R.string.title_sign_in), (dialog, which) -> {
                    String user = name.getText().toString();
                    String pass = password.getText().toString();
                    handler.proceed(user.trim(), pass.trim());
                })
                .setNegativeButton(webViewTab.getActivity().getString(R.string.action_cancel), (dialog, which) -> handler.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull final SslErrorHandler handler, @NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = getAllSslErrorMessageCodes(error);
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer messageCode : errorCodeMessageCodes) {
            stringBuilder.append(" - ").append(webViewTab.getActivity().getString(messageCode)).append('\n');
        }
        String alertMessage =
                webViewTab.getActivity().getString(R.string.message_insecure_connection, stringBuilder.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(webViewTab.getActivity());
        builder.setTitle(webViewTab.getActivity().getString(R.string.title_warning));
        builder.setMessage(alertMessage)
                .setCancelable(true)
                .setPositiveButton(webViewTab.getActivity().getString(R.string.action_yes), (dialog, which) -> handler.proceed())
                .setNegativeButton(webViewTab.getActivity().getString(R.string.action_no), (dialog, which) -> handler.cancel());
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
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (AdBlock.isAd(request.getUrl().toString())) {
            ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
            return new WebResourceResponse("text/plain", "utf-8", EMPTY);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (AdBlock.isAd(url)) {
            ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
            return new WebResourceResponse("text/plain", "utf-8", EMPTY);
        }
        return null;
    }

    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
        if (UrlUtils.isInBlackList(request.getUrl().toString())) {
            return true;
        }

        return shouldOverrideLoading(view, request.getUrl().toString()) || super.shouldOverrideUrlLoading(view, request);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
        if (UrlUtils.isInBlackList(url)) {
            return true;
        }

        return shouldOverrideLoading(view, url) || super.shouldOverrideUrlLoading(view, url);
    }

    private boolean shouldOverrideLoading(@NonNull WebView view, @NonNull String url) {
        SearcherTab tab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
        if (tab instanceof WebViewTab) {
            WebViewTab webViewTab = (WebViewTab) tab;
            Map<String, String> headers = webViewTab.getRequestHeaders();
            if (url.startsWith(Constants.ABOUT)) {
                return continueLoadingUrl(view, url, headers);
            }
            return isMailOrIntent(url, view)
                    || IntentUtils.startActivityForUrl(view, url)
                    || continueLoadingUrl(view, url, headers);
        }
        return false;
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
            webViewTab.getActivity().startActivity(i);
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
                    webViewTab.getActivity().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e("MyWebClient", "ActivityNotFoundException");
                }
                return true;
            }
        }
        return false;
    }

}
