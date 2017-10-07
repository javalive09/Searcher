package peter.util.searcher.activity


import android.os.Bundle
import peter.util.searcher.R
import peter.util.searcher.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*


/**
 * 设置
 * Created by peter on 2017/6/14.
 */

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fragmentManager.beginTransaction().replace(R.id.setting_content, SettingsFragment()).commit()

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { _ -> finish() }
    }

}
