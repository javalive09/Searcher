package peter.util.searcher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import peter.util.searcher.R;
import peter.util.searcher.SettingsManager;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.net.UpdateController;

/**
 * 设置列表
 * Created by peter on 2017/6/14.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        init();
    }

    private void init() {
        findPreference(getString(R.string.app_share)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.clear_cache)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.feed_back)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.manual_update)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.app_about)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.one_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.no_track)).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getTitleRes()) {
            case R.string.app_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_url));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
                break;

            case R.string.clear_cache:
                ((BaseActivity) getActivity()).clearCacheFolder(getActivity().getCacheDir(), 0);
                ((BaseActivity) getActivity()).ClearCookies();
                Toast.makeText(getActivity(), R.string.setting_clear, Toast.LENGTH_LONG).show();
                break;

            case R.string.feed_back:
                ((BaseActivity) getActivity()).sendMailByIntent();
                break;
            case R.string.manual_update:
                UpdateController.instance().checkVersion(((BaseActivity) getActivity()), true);
                break;

            case R.string.app_about:
                ((BaseActivity) getActivity()).showAlertDialog(R.string.action_about, R.string.setting_about);
                break;
            case R.string.one_key:
                ((BaseActivity) getActivity()).showAlertDialog(R.string.one_step_title, R.string.one_step_txt);
                break;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean checked = false;
        if (newValue instanceof Boolean) {
            checked = Boolean.TRUE.equals(newValue);
        }

        switch (preference.getTitleRes()) {
            case R.string.no_track:
                SettingsManager.getInstance().saveNoTrackSp(checked);
                break;
        }


        return false;
    }
}
