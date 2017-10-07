package peter.util.searcher.fragment

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.Toast

import peter.util.searcher.R
import peter.util.searcher.SettingsManager
import peter.util.searcher.activity.BaseActivity
import peter.util.searcher.net.UpdateController

/**
 * 设置列表
 * Created by peter on 2017/6/14.
 */

class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference)
        init()
    }

    private fun init() {
        findPreference(getString(R.string.app_share)).onPreferenceClickListener = this
        findPreference(getString(R.string.clear_cache)).onPreferenceClickListener = this
        findPreference(getString(R.string.feed_back)).onPreferenceClickListener = this
        findPreference(getString(R.string.manual_update)).onPreferenceClickListener = this
        findPreference(getString(R.string.app_about)).onPreferenceClickListener = this
        findPreference(getString(R.string.one_key)).onPreferenceClickListener = this
        findPreference(getString(R.string.no_track)).onPreferenceChangeListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.titleRes) {
            R.string.app_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_url))
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)))
            }

            R.string.clear_cache -> {
                (activity as BaseActivity).clearCacheFolder(activity.cacheDir, 0)
                (activity as BaseActivity).clearCookies()
                Toast.makeText(activity, R.string.setting_clear, Toast.LENGTH_LONG).show()
            }

            R.string.feed_back -> (activity as BaseActivity).sendMailByIntent()
            R.string.manual_update -> UpdateController.instance.checkVersion(activity as BaseActivity, true)

            R.string.app_about -> (activity as BaseActivity).showAlertDialog(R.string.action_about, R.string.setting_about)
            R.string.one_key -> (activity as BaseActivity).showAlertDialog(R.string.one_step_title, R.string.one_step_txt)
        }
        return false
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        var checked = false
        if (newValue is Boolean) {
            checked = java.lang.Boolean.TRUE == newValue
        }

        when (preference.titleRes) {
            R.string.no_track -> {
                SettingsManager.instance.saveNoTrackSp(checked)
                return true
            }
        }
        return false
    }
}
