package peter.util.searcher.net;

import peter.util.searcher.bean.EnginesInfo;
import retrofit2.http.GET;
import rx.Observable;

/**
 * 获取引擎列表接口
 * Created by peter on 2017/5/27.
 */

public interface IEngineService {

    @GET("engines170527.json")
    Observable<EnginesInfo> getInfo();

}
