package peter.util.searcher

import android.app.Application
import android.content.Context

import com.facebook.stetho.Stetho

/**
 * application 入口
 * Created by peter on 16/5/19.
 */
class Searcher : Application() {

    override fun onCreate() {
        context = this
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

    companion object {
        var context: Context? = null
    }

}
