package peter.util.searcher

import android.content.Context

class SettingsManager private constructor() {

    private val name = "setting"
    var isAutoFullScreen: Boolean = false
    var isNoTrack: Boolean = false

    private object Holder { val INSTANCE = SettingsManager() }

    companion object {
        val instance: SettingsManager by lazy { Holder.INSTANCE }
    }

    init {
        isAutoFullScreen = Searcher.context!!.getSharedPreferences(name, Context.MODE_PRIVATE)
                .getBoolean(Searcher.context!!.getString(R.string.action_auto_fullscreen), false)
        isNoTrack = Searcher.context!!.getSharedPreferences(name, Context.MODE_PRIVATE).
                getBoolean(Searcher.context!!.getString(R.string.no_track), false)
    }

    fun saveAutoFullScreenSp(autoFullScreen: Boolean) {
        this.isAutoFullScreen = autoFullScreen
        val sp = Searcher.context!!.getSharedPreferences(name, Context.MODE_PRIVATE)
        sp.edit().putBoolean(Searcher.context!!.getString(R.string.action_auto_fullscreen), autoFullScreen).apply()
    }

    fun saveNoTrackSp(noTrack: Boolean) {
        this.isNoTrack = noTrack
        val sp = Searcher.context!!.getSharedPreferences(name, Context.MODE_PRIVATE)
        sp.edit().putBoolean(Searcher.context!!.getString(R.string.no_track), isAutoFullScreen).apply()
    }




}