package peter.util.searcher;

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

/**
 * Created by peter on 16/6/6.
 */
public class MyWebChromeClient extends WebChromeClient {

    private static final String TAG = MyWebChromeClient.class.getSimpleName();
    private MainActivity act;
    // Full Screen Video Views
    private int mOriginalOrientation;
    private FrameLayout mFullscreenContainer;
    private VideoView mVideoView;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private SearcherWebView searcherWebView;

    public MyWebChromeClient(SearcherWebView view, MainActivity act) {
        this.act = act;
        this.searcherWebView = view;
    }

    @Override
    public void onHideCustomView() {
        hideCustomView();
        Log.i("peter", "onHideCustomView");
    }

    public void onProgressChanged(WebView view, int newProgress) {
        searcherWebView.setOptStatus(view);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
        searcherWebView.setMainColor(icon);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        int requestedOrientation = mOriginalOrientation = act.getRequestedOrientation();
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
//        ((WebView.WebViewTransport) resultMsg.obj).setWebView(new WebView(view.getContext()));
//        resultMsg.sendToTarget();
//        return true;


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
        act.setFullscreen(false, false);
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
        act.setRequestedOrientation(mOriginalOrientation);
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
        mOriginalOrientation = act.getRequestedOrientation();
        mCustomViewCallback = callback;
        mCustomView = view;

        act.setRequestedOrientation(requestedOrientation);
        final FrameLayout decorView = (FrameLayout) act.getWindow().getDecorView();

        mFullscreenContainer = new FrameLayout(act);
        mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(act, android.R.color.black));
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
        act.setFullscreen(true, true);
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
