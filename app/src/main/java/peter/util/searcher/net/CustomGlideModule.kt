package peter.util.searcher.net

import android.content.Context

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl

import java.io.InputStream
import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient

class CustomGlideModule : OkHttpGlideModule() {
    override fun applyOptions(context: Context?, builder: GlideBuilder?) {
        // stub
    }

    override fun registerComponents(context: Context?, glide: Glide) {
        val builder = OkHttpClient.Builder()
        // set your timeout here
        builder.readTimeout(30, TimeUnit.SECONDS)
        builder.writeTimeout(30, TimeUnit.SECONDS)
        builder.connectTimeout(30, TimeUnit.SECONDS)
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(builder.build()))
    }

}