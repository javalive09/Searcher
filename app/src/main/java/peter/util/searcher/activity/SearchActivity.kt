package peter.util.searcher.activity

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View

import com.umeng.analytics.MobclickAgent

import org.jetbrains.annotations.NotNull
import peter.util.searcher.R
import peter.util.searcher.bean.Bean
import peter.util.searcher.fragment.EngineInfoViewPagerFragment
import peter.util.searcher.fragment.OperateUrlFragment
import peter.util.searcher.fragment.RecentSearchFragment
import peter.util.searcher.tab.Tab
import peter.util.searcher.utils.UrlUtils
import kotlinx.android.synthetic.main.activity_search.*


/**
 * 搜索页activity
 * Created by peter on 16/5/19.
 */
class SearchActivity : BaseActivity(), View.OnClickListener {

    private var currentFragmentTag = ""
    private var bean: Bean? = null

    override fun getSearchWord(): String? = top_txt!!.text.toString().trim()

    @NotNull
    override fun setSearchWord(word: String) {
        top_txt!!.setText(word)
        top_txt!!.setSelection(word.length)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        init()
    }

    public override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    private fun checkData(intent: Intent) {
        this.bean = intent.getParcelableExtra(BaseActivity.Companion.NAME_BEAN)
        if (bean?.name?.isEmpty() != null) {
            if (bean!!.name!!.contains(Tab.LOCAL_SCHEMA)) {
                setSearchWord(bean!!.name!!)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BaseActivity.Companion.NAME_WORD, getSearchWord())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setSearchWord(savedInstanceState.getString(BaseActivity.Companion.NAME_WORD))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkData(intent)
    }

    public override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    private fun init() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar!!.setNavigationOnClickListener { _ ->
            closeIME()
            finish()
        }
        top_txt!!.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(s.toString())) {
                    setEngineFragment(RECENT_SEARCH)
                    clearall!!.visibility = View.GONE
                } else {
                    clearall!!.visibility = View.VISIBLE
                    if (UrlUtils.guessUrl(s.toString())) {
                        setEngineFragment(OPERATE_URL)
                    } else {
                        setEngineFragment(ENGINE_LIST)
                    }
                }
            }
        })
        setEngineFragment(RECENT_SEARCH)
        checkData(intent)
    }

    fun setEngineFragment(tag: String) {
        if (currentFragmentTag != tag) {
            currentFragmentTag = tag
            var fragment: Fragment? = null
            when (tag) {
                RECENT_SEARCH -> fragment = RecentSearchFragment()
                ENGINE_LIST -> fragment = EngineInfoViewPagerFragment()
                OPERATE_URL -> fragment = OperateUrlFragment()
            }

            if (fragment != null) {
                val bundle = Bundle()
                bundle.putParcelable(BaseActivity.Companion.NAME_BEAN, bean)
                fragment.arguments = bundle
                val fragmentManager = fragmentManager
                val ft = fragmentManager.beginTransaction()
                ft.replace(R.id.content_frame, fragment, tag)
                ft.commitAllowingStateLoss()
            }
        }
    }

    override fun onClick(v: View) {
        openIME()
        top_txt!!.requestFocus()
        top_txt!!.setText("")
    }

    companion object {
        private val RECENT_SEARCH = "recent_search"
        val ENGINE_LIST = "engine_list"
        val OPERATE_URL = "operate_url"
    }


}
