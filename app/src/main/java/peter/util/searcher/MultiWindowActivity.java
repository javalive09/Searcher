package peter.util.searcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.appeaser.deckview.views.DeckChildView;
import com.appeaser.deckview.views.DeckView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by peter on 16/5/19.
 */
public class MultiWindowActivity extends BaseActivity {

    DeckView<SearcherWebView> mDeckView;
    Drawable defaultHeadIcon;
    int scrollToChildIndex = -1;
    // SavedInstance bundle keys
    final String CURRENT_SCROLL = "current.scroll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiwindow);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_SCROLL)) {
                scrollToChildIndex = savedInstanceState.getInt(CURRENT_SCROLL);
            }
        }
        mDeckView = (DeckView<SearcherWebView>) findViewById(R.id.deckview);
        defaultHeadIcon = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        final ArrayList<SearcherWebView> mEntries = SearcherWebViewManager.instance().getAllViews();
        // Callback implementation
        DeckView.Callback<SearcherWebView> deckViewCallback = new DeckView.Callback<SearcherWebView>() {
            @Override
            public ArrayList<SearcherWebView> getData() {
                return mEntries;
            }

            @Override
            public void loadViewData(WeakReference<DeckChildView<SearcherWebView>> dcv, SearcherWebView item) {
                loadViewDataInternal(item, dcv);
            }

            @Override
            public void unloadViewData(SearcherWebView item) {
            }

            @Override
            public void onViewDismissed(SearcherWebView item) {
                SearcherWebViewManager.instance().removeWebView(item);
                if (SearcherWebViewManager.instance().getWebViewCount() > 0) {
                    SearcherWebViewManager.instance().setDefaultCurrentWebView();
                    mDeckView.notifyDataSetChanged();
                } else {
                    startBrowser(MultiWindowActivity.this, "", "");
                }
            }

            @Override
            public void onItemClick(SearcherWebView item) {
                switchBrowser(MultiWindowActivity.this, item.getUrl());
                finish();
            }

            @Override
            public void onNoViewsToDeck() {
                Toast.makeText(MultiWindowActivity.this,
                        "No views to show",
                        Toast.LENGTH_SHORT).show();
            }
        };

        mDeckView.initialize(deckViewCallback);

        if (scrollToChildIndex == -1) {
            scrollToChildIndex = SearcherWebViewManager.instance().getCurrentWebViewPos();
        }
        mDeckView.post(new Runnable() {
            @Override
            public void run() {
                // Restore scroll position
                mDeckView.scrollToChild(scrollToChildIndex);
            }
        });

        final View newTab = findViewById(R.id.new_tab);
        if(newTab != null) {
            newTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    startBrowser(MultiWindowActivity.this, MainActivity.HOME, "", true);
                    startSearch(true);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        SearcherWebView view = SearcherWebViewManager.instance().getCurrentWebView();
        switchBrowser(MultiWindowActivity.this, view.getUrl());
    }

    private void loadViewDataInternal(SearcherWebView item, final WeakReference<DeckChildView<SearcherWebView>> weakView) {
        if (weakView.get() != null) {
            Bitmap thumbnail = item.getThumbNail();
            Bitmap icon = item.getFavIcon();
            Drawable iconDrawable;
            if(icon != null) {
                iconDrawable = new BitmapDrawable(icon);
            }else {
                iconDrawable = defaultHeadIcon;
            }
            weakView.get().onDataLoaded(item, thumbnail,
                    iconDrawable, item.getTitle(), item.getMainColor());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save current scroll and the list
        int currentChildIndex = mDeckView.getCurrentChildIndex();
        outState.putInt(CURRENT_SCROLL, currentChildIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
