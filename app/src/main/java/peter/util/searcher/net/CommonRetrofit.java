package peter.util.searcher.net;

import android.content.Context;

//import com.facebook.stetho.okhttp3.StethoInterceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommonRetrofit {

    private Context context;
    private Retrofit retrofit;
    private static final String URL = "http://7xoxmg.com1.z0.glb.clouddn.com/";
    private static final String CACHE_NAME = "engines";
    private static final long MAX_CACHE = 1024 * 1024 * 10; //10M

    private CommonRetrofit() {
    }

    public void init(Context context) {
        this.context = context;
    }

    private static class SingletonInstance {
        private static final CommonRetrofit INSTANCE = new CommonRetrofit();
    }

    public static CommonRetrofit getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .cache(new Cache(new File(context.getCacheDir(), CACHE_NAME), MAX_CACHE))
//                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();
//            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(okHttpClient)
//                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }


}