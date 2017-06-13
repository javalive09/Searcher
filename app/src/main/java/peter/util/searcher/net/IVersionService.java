package peter.util.searcher.net;

import peter.util.searcher.bean.UrlInfo;
import peter.util.searcher.bean.VersionInfo;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by peter on 2017/5/27.
 */

public interface IVersionService {

    String URL = "https://raw.githubusercontent.com/javalive09/config/master/searcher_update_info";

    @GET
    Observable<VersionInfo> getInfo(@Url String url);

    @GET
    Observable<UrlInfo> getUrl(@Url String url);

}
