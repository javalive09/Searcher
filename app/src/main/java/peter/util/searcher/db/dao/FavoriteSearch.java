package peter.util.searcher.db.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by peter on 2017/9/8.
 */

@Entity
public class FavoriteSearch {

    @Index(unique = true)
    @Id
    private String searchWord;
    private long time;
    private String url;
    private int pageNo;
    @Generated(hash = 1907163316)
    public FavoriteSearch(String searchWord, long time, String url, int pageNo) {
        this.searchWord = searchWord;
        this.time = time;
        this.url = url;
        this.pageNo = pageNo;
    }
    @Generated(hash = 48156489)
    public FavoriteSearch() {
    }
    public String getSearchWord() {
        return this.searchWord;
    }
    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }
    public long getTime() {
        return this.time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public int getPageNo() {
        return this.pageNo;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

}
