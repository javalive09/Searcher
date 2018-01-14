package peter.util.searcher;

import com.javalive09.codebag.Play;
import com.javalive09.codebag.Player;
import com.javalive09.codebag.PlayerActivity;

import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.utils.UrlUtils;

/**
 * Created by peter on 2018/1/14.
 */

@Player(name = "一些单元测试")
public class UnitTest {

    @Play(name = "插入10个新的 url tab")
    public void insertUrl() {
        String searchWord = "a";
        for (int i = 0; i < 10; i++) {
            flushUrl(searchWord + i);
        }
    }

    /**
     * test insert url
     *
     * @param searchWord 搜索词
     */
    public void flushUrl(String searchWord) {
        String engineUrl = PlayerActivity.context().getString(R.string.default_engine_url);
        String url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl);
        TabData tabData = new TabData();
        tabData.setTitle(searchWord);
        tabData.setUrl(url);
        TabGroupManager.getInstance().load(tabData, true);
    }
}
