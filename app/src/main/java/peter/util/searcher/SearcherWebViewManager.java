package peter.util.searcher;


import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by peter on 16/5/9.
 */
public class SearcherWebViewManager {

    private static final int MAX_VIEWS_COUNT = 19;
    private ArrayList<SearcherWebView> mList = new ArrayList<>();
    private SearcherWebView mCurrentWebView;
    private static SearcherWebViewManager manager;

    public static SearcherWebViewManager instance() {
        synchronized (SearcherWebViewManager.class) {
            if(manager == null) {
                manager = new SearcherWebViewManager();
            }
            return manager;
        }
    }

    public ArrayList<SearcherWebView> getAllViews() {
        return mList;
    }

    public int getWebViewCount() {
        return mList.size();
    }

    public SearcherWebView newWebview(MainActivity activity, String url, String searchWord) {
        SearcherWebView existView = containUrlView(url);
        if(existView != null) {
            setCurrentWebView(existView);
            activity.refreshStatusColor(existView);
            return  existView;
        }else {
            if(isMaxViewsLimite()) {
                SearcherWebView currentWebView = getCurrentWebView();
                currentWebView.loadUrl(activity, url, searchWord);
                return currentWebView;
            }else {
                SearcherWebView searcherWebView = new SearcherWebView(activity, url, searchWord);
                mList.add(searcherWebView);
                int position = mList.indexOf(searcherWebView);
                setCurrentWebView(position);
                return searcherWebView;
            }
        }
    }

    public SearcherWebView containUrlView(String url) {
        for(int i = 0, len = mList.size(); i< len ; i++) {
            SearcherWebView view = mList.get(i);
            if(view.getUrl().equals(url)) {
                return view;
            }
        }
        return null;
    }

    public void setCurrentWebView(int position) {
        if (position >= mList.size()) {
            return;
        }
        mCurrentWebView = mList.get(position);
        for (SearcherWebView view : mList) {
            if (view != mCurrentWebView) {
                view.onPause();
                view.pauseTimers();
            }
        }
        mCurrentWebView.onResume();
        mCurrentWebView.resumeTimers();
    }

    public void setCurrentWebView(SearcherWebView view) {
        setCurrentWebView(mList.indexOf(view));
    }

    private void removeWebView(int position) {
        if (position >= mList.size()) {
            return;
        }
        final SearcherWebView tab = mList.remove(position);
        if (mCurrentWebView == tab) {
            if(mList.size() > 0) {
                mCurrentWebView = mList.get(mList.size() - 1);
            }else {
                mCurrentWebView = null;
            }
        }
        tab.onDestroy();
    }

    public void clearAllWebViews() {
        for(SearcherWebView view : mList) {
            view.onDestroy();
        }
        mList.clear();
        mCurrentWebView = null;
    }

    public void removeWebView(SearcherWebView view) {
        removeWebView(mList.indexOf(view));
    }

    public synchronized SearcherWebView getCurrentWebView() {
        return mCurrentWebView;
    }

    public int getCurrentWebViewPos() {
        if(mCurrentWebView != null) {
            return mList.indexOf(mCurrentWebView);
        }
        return -1;
    }

    public void resumeAll() {
        if (mCurrentWebView != null) {
            mCurrentWebView.resumeTimers();
        }
        for (SearcherWebView tab : mList) {
            if (tab != null) {
                tab.onResume();
            }
        }
    }

    /**
     * Method used to pause all the tabs in the browser.
     * This is necessary because we cannot pause the
     * WebView when the app is open currently due to a
     * bug in the WebView, where calling onResume doesn't
     * consistently resume it.
     */
    public void pauseAll() {
        if (mCurrentWebView != null) {
            mCurrentWebView.pauseTimers();
        }
        for (SearcherWebView tab : mList) {
            if (tab != null) {
                tab.onPause();
            }
        }
    }


    /**
     * Shutdown the manager. This destroys
     * all tabs and clears the references
     * to those tabs. Current tab is also
     * released for garbage collection.
     */
    public synchronized void shutdown() {
        for (SearcherWebView tab : mList) {
            tab.onDestroy();
        }
        mList.clear();
        mCurrentWebView = null;
    }

    /**
     * Return the tab at the given position in tabs list, or
     * null if position is not in tabs list range.
     *
     * @param position the index in tabs list
     * @return the corespondent {@link SearcherWebView},
     * or null if the index is invalid
     */
    public synchronized SearcherWebView getWebViewAtPosition(final int position) {
        if (position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    public boolean isMaxViewsLimite() {
        return mList.size() > MAX_VIEWS_COUNT;
    }

}
