package peter.util.searcher.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;

/**
 * Created by peter on 16/5/9.
 */
public class BaseFragment extends Fragment {

    public boolean canGoBack() {
        return false;
    }

    public void GoBack() {

    }

}
