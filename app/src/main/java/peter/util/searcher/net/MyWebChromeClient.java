package peter.util.searcher.net;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.tab.WebViewTab;
import peter.util.searcher.utils.Utils;

/**
 * webView的配置chromeClient
 * Created by peter on 16/6/6.
 */
public class MyWebChromeClient extends WebChromeClient {

    private static final String TAG = MyWebChromeClient.class.getSimpleName();
    // Full Screen Video Views
    private int mOriginalOrientation;
    private FrameLayout mFullscreenContainer;
    private VideoView mVideoView;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private final WebViewTab webViewTab;

    public MyWebChromeClient(WebViewTab webViewTab) {
        this.webViewTab = webViewTab;
    }

    @Override
    public void onHideCustomView() {
        hideCustomView();
        Log.i("peter", "onHideCustomView");
    }

    public boolean isCustomViewShow() {
        return mCustomView != null;
    }

    public void onProgressChanged(WebView view, int newProgress) {
        webViewTab.getActivity().refreshProgress(webViewTab, newProgress);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
        Observable.just("onReceivedIcon").observeOn(Schedulers.io()).subscribe(s -> {
            TabData tabData = webViewTab.getTabData();
            tabData.setIcon(Utils.Bitemap2Bytes(icon));
            DaoManager.getInstance().insertTabDataDao(tabData);
        });
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        mOriginalOrientation = webViewTab.getActivity().getRequestedOrientation();
        int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        showCustomView(view, callback, requestedOrientation);
        Log.i("peter", "onShowCustomView 2");
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation,
                                 CustomViewCallback callback) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        showCustomView(view, callback, requestedOrientation);
        Log.i("peter", "onShowCustomView 3");
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        if (resultMsg != null) {
            TabGroup parentTabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
            TabData tabData = new TabData();
            tabData.setUrl(Tab.ACTION_NEW_WINDOW);
            TabGroupManager.getInstance().load(tabData, true);
            TabGroupManager.getInstance().getCurrentTabGroup().setParent(parentTabGroup);
            SearcherTab tab = TabGroupManager.getInstance().getCurrentTabGroup().getCurrentTab();
            View tabView = tab.getView();
            if (tabView != null && tabView instanceof WebView) {
                WebView webView = (WebView) tabView;
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(webView);
                resultMsg.sendToTarget();
            }
        }
        return true;
    }

    @Override
    public void onCloseWindow(WebView window) {
        Log.i("onCloseWindow", window.toString());
        TabGroup tabGroup = TabGroupManager.getInstance().getTabGroup(webViewTab);
        if (tabGroup != null) {
            TabGroupManager.getInstance().removeTabGroup(tabGroup);
        }
    }

    public void hideCustomView() {
        if (mCustomView == null || mCustomViewCallback == null) {
            if (mCustomViewCallback != null) {
                try {
                    mCustomViewCallback.onCustomViewHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding custom view", e);
                }
                mCustomViewCallback = null;
            }
            return;
        }
        Log.d(TAG, "onHideCustomView");
        try {
            mCustomView.setKeepScreenOn(false);
        } catch (SecurityException e) {
            Log.e(TAG, "WebView is not allowed to keep the screen on");
        }
        webViewTab.getActivity().setFullscreen(false, false);
        if (mFullscreenContainer != null) {
            ViewGroup parent = (ViewGroup) mFullscreenContainer.getParent();
            if (parent != null) {
                parent.removeView(mFullscreenContainer);
            }
            mFullscreenContainer.removeAllViews();
        }

        mFullscreenContainer = null;
        mCustomView = null;
        if (mVideoView != null) {
            Log.d(TAG, "VideoView is being stopped");
            mVideoView.stopPlayback();
            mVideoView.setOnErrorListener(null);
            mVideoView.setOnCompletionListener(null);
            mVideoView = null;
        }
        if (mCustomViewCallback != null) {
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Exception e) {
                Log.e(TAG, "Error hiding custom view", e);
            }
        }
        mCustomViewCallback = null;
        webViewTab.getActivity().setRequestedOrientation(mOriginalOrientation);
    }

    private synchronized void showCustomView(final View view, WebChromeClient.CustomViewCallback callback, int requestedOrientation) {
        if (view == null || mCustomView != null) {
            if (callback != null) {
                try {
                    callback.onCustomViewHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding custom view", e);
                }
            }
            return;
        }
        try {
            view.setKeepScreenOn(true);
        } catch (SecurityException e) {
            Log.e(TAG, "WebView is not allowed to keep the screen on");
        }
        mOriginalOrientation = webViewTab.getActivity().getRequestedOrientation();
        mCustomViewCallback = callback;
        mCustomView = view;

        webViewTab.getActivity().setRequestedOrientation(requestedOrientation);
        final FrameLayout decorView = (FrameLayout) webViewTab.getActivity().getWindow().getDecorView();

        mFullscreenContainer = new FrameLayout(webViewTab.getActivity());
        mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(webViewTab.getActivity(), android.R.color.black));
        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                mVideoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                mVideoView.setOnErrorListener(new VideoCompletionListener());
                mVideoView.setOnCompletionListener(new VideoCompletionListener());
            }
        } else if (view instanceof VideoView) {
            mVideoView = (VideoView) view;
            mVideoView.setOnErrorListener(new VideoCompletionListener());
            mVideoView.setOnCompletionListener(new VideoCompletionListener());
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        decorView.addView(mFullscreenContainer, params);
        mFullscreenContainer.addView(mCustomView, params);
        decorView.requestLayout();
        webViewTab.getActivity().setFullscreen(true, true);
    }

    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            hideCustomView();
        }

    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(webViewTab.getActivity(), permissions, new PermissionsResultAction() {

            @Override
            public void onGranted() {
                boolean remember = true;
                String org = origin.length() > 50 ? origin.substring(0, 50) : origin;

                new AlertDialog.Builder(webViewTab.getActivity()).setTitle(R.string.location)
                        .setMessage(org + webViewTab.getActivity().getString(R.string.message_location))
                        .setCancelable(true)
                        .setPositiveButton(webViewTab.getActivity().getString(R.string.action_allow), (dialog, which) ->
                                callback.invoke(origin, true, remember))
                        .setNegativeButton(webViewTab.getActivity().getString(R.string.action_do_not_allow), (dialog, which) ->
                                callback.invoke(origin, false, remember)).show();
            }

            @Override
            public void onDenied(String permission) {
                Toast.makeText(webViewTab.getActivity(), R.string.wr_location_permission, Toast.LENGTH_LONG).show();
            }
        });

    }
}
