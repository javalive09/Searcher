package peter.util.searcher.activity;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.fragment.SettingsFragment;

/**
 *
 * Created by peter on 2017/6/14.
 */

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction().
                replace(R.id.setting_content, new SettingsFragment()).commit();

        ButterKnife.bind(SettingsActivity.this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // this sets the button to the back icon
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }


}
