package peter.util.searcher.net;


import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import peter.util.searcher.BuildConfig;
import peter.util.searcher.Searcher;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommonRetrofit {

    private Retrofit retrofit;
    private static final String URL = "http://searcher-1254131086.file.myqcloud.com/";
    private static final String CACHE_NAME = "engines";
    private static final long MAX_CACHE = 1024 * 1024 * 10; //10M

    private CommonRetrofit() {
    }

    private static class SingletonInstance {
        private static final CommonRetrofit INSTANCE = new CommonRetrofit();
    }

    public static CommonRetrofit getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            if (BuildConfig.DEBUG) {
                // Log信息拦截器
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);//这里可以选择拦截级别
                //设置 Debug Log 模式
                builder.addInterceptor(loggingInterceptor);
            }

            OkHttpClient okHttpClient = builder
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .cache(new Cache(new File(Searcher.context.getCacheDir(), CACHE_NAME), MAX_CACHE))
                    .build();

            if (BuildConfig.DEBUG) {
                builder.addNetworkInterceptor(new StethoInterceptor());
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }


}