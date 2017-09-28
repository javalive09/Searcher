package peter.util.searcher.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;

/**
 * 书签fragment父类
 * <p>
 * Created by peter on 2017/9/28.
 */

public abstract class BookmarkFragment extends Fragment {

    protected SearchView mSearchView;

    public boolean needCloseSearchView() {
        if (mSearchView != null && !mSearchView.isIconified()) {//open
            mSearchView.setIconified(true);
            return true;
        }
        return false;
    }

}
