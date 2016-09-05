package peter.util.searcher.net;

import android.app.Activity;
import android.graphics.Bitmap;
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

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.fragment.WebViewFragment;

/**
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
    private WebViewFragment fragment;

    public MyWebChromeClient(WebViewFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onHideCustomView() {
        hideCustomView();
        Log.i("peter", "onHideCustomView");
    }

    public void onProgressChanged(WebView view, int newProgress) {

    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
//        mainActivity.setMainColor(icon);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        int requestedOrientation = mOriginalOrientation = fragment.getActivity().getRequestedOrientation();
        showCustomView(view, callback, requestedOrientation);
        Log.i("peter", "onShowCustomView 2");
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation,
                                 CustomViewCallback callback) {
        showCustomView(view, callback, requestedOrientation);
        Log.i("peter", "onShowCustomView 3");
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(new WebView(view.getContext()));    //此webview可以是一般新创建的
        resultMsg.sendToTarget();
        return true;
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
        fragment.setFullscreen(false, false);
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
        fragment.getActivity().setRequestedOrientation(mOriginalOrientation);
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
        Activity mainActivity = fragment.getActivity();
        if(mainActivity != null) {
            try {
                view.setKeepScreenOn(true);
            } catch (SecurityException e) {
                Log.e(TAG, "WebView is not allowed to keep the screen on");
            }
            mOriginalOrientation = mainActivity.getRequestedOrientation();
            mCustomViewCallback = callback;
            mCustomView = view;

            mainActivity.setRequestedOrientation(requestedOrientation);
            final FrameLayout decorView = (FrameLayout) mainActivity.getWindow().getDecorView();

            mFullscreenContainer = new FrameLayout(mainActivity);
            mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(mainActivity, android.R.color.black));
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
            fragment.setFullscreen(true, true);
        }
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
