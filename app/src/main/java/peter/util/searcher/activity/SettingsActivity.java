package peter.util.searcher.activity;


import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import peter.util.searcher.R;
import peter.util.searcher.databinding.ActivitySettingsBinding;
import peter.util.searcher.fragment.SettingsFragment;

/**
 *
 * Created by peter on 2017/6/14.
 */

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        getFragmentManager().beginTransaction().
                replace(R.id.setting_content, new SettingsFragment()).commit();
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // this sets the button to the back icon
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }


}
