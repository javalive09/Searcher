package peter.util.searcher.net;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.VideoView;

import peter.util.searcher.TabManager;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.tab.WebViewTab;

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
    private MainActivity mActivity;
    private WebViewTab webViewTab;

    public MyWebChromeClient(WebViewTab webViewTab) {
        this.webViewTab = webViewTab;
        this.mActivity = webViewTab.getActivity();
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
        mActivity.refreshProgress(webViewTab, newProgress);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
        webViewTab.setIconDrawable(new BitmapDrawable(mActivity.getResources(), icon));
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        mOriginalOrientation = mActivity.getRequestedOrientation();
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
            TabGroup parentTabGroup = mActivity.getTabManager().getCurrentTabGroup();
            mActivity.getTabManager().loadUrl(new Bean("", Tab.ACTION_NEW_WINDOW), true);
            mActivity.getTabManager().getCurrentTabGroup().setParent(parentTabGroup);
            SearcherTab tab = mActivity.getTabManager().getCurrentTabGroup().getCurrentTab();
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
        TabGroup tabGroup = mActivity.getTabManager().getTabGroup(webViewTab);
        if (tabGroup != null) {
            mActivity.getTabManager().removeTabGroup(tabGroup);
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
        mActivity.setFullscreen(false, false);
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
        mActivity.setRequestedOrientation(mOriginalOrientation);
    }

    public synchronized void showCustomView(final View view, WebChromeClient.CustomViewCallback callback, int requestedOrientation) {
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
        mOriginalOrientation = mActivity.getRequestedOrientation();
        mCustomViewCallback = callback;
        mCustomView = view;

        mActivity.setRequestedOrientation(requestedOrientation);
        final FrameLayout decorView = (FrameLayout) mActivity.getWindow().getDecorView();

        mFullscreenContainer = new FrameLayout(mActivity);
        mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(mActivity, android.R.color.black));
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
        mActivity.setFullscreen(true, true);
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

}
