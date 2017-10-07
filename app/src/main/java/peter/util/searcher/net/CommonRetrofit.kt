package peter.util.searcher.net


//import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File
import java.util.concurrent.TimeUnit

import okhttp3.Cache
import okhttp3.OkHttpClient
import peter.util.searcher.Searcher
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CommonRetrofit private constructor() {

    private var retrofit: Retrofit? = null

    private object SingletonInstance {
        val INSTANCE = CommonRetrofit()
    }

    companion object {
        private val URL = "http://7xoxmg.com1.z0.glb.clouddn.com/"
        private val CACHE_NAME = "engines"
        private val MAX_CACHE = (1024 * 1024 * 10).toLong() //10M
        val instance: CommonRetrofit by lazy { SingletonInstance.INSTANCE }
    }

    fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .cache(Cache(File(Searcher.context!!.cacheDir, CACHE_NAME), MAX_CACHE))
                    //                    .addNetworkInterceptor(new StethoInterceptor())
                    .build()
            //            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = Retrofit.Builder()
                    .baseUrl(URL)
                    .client(okHttpClient)
                    //                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }
        return retrofit
    }


}