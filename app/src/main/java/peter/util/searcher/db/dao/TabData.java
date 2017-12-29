package peter.util.searcher.db.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;
import java.util.Arrays;

/**
 * tab
 * <p>
 * Created by peter on 2017/11/27.
 */

@Entity
public class TabData implements Serializable {

    private static final long serialVersionUID = 42L;

    @Index(unique = true)
    @Id(autoincrement = true)
    private Long id;

    private int tabCount;

    private int tabGroupCount;

    private long time;

    private int pageNo;

    private int tabIndex;

    private boolean isCurrentTab;

    private boolean isCurrentTabGroup;

    private int groupTabIndex;

    private String searchWord;

    private String url;

    private String title;

    private byte[] bundle;

    @Generated(hash = 1291563145)
    public TabData(Long id, int tabCount, int tabGroupCount, long time, int pageNo,
            int tabIndex, boolean isCurrentTab, boolean isCurrentTabGroup,
            int groupTabIndex, String searchWord, String url, String title,
            byte[] bundle) {
        this.id = id;
        this.tabCount = tabCount;
        this.tabGroupCount = tabGroupCount;
        this.time = time;
        this.pageNo = pageNo;
        this.tabIndex = tabIndex;
        this.isCurrentTab = isCurrentTab;
        this.isCurrentTabGroup = isCurrentTabGroup;
        this.groupTabIndex = groupTabIndex;
        this.searchWord = searchWord;
        this.url = url;
        this.title = title;
        this.bundle = bundle;
    }

    @Generated(hash = 912424272)
    public TabData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTabCount() {
        return this.tabCount;
    }

    public void setTabCount(int tabCount) {
        this.tabCount = tabCount;
    }

    public int getTabGroupCount() {
        return this.tabGroupCount;
    }

    public void setTabGroupCount(int tabGroupCount) {
        this.tabGroupCount = tabGroupCount;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getPageNo() {
        return this.pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getTabIndex() {
        return this.tabIndex;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public boolean getIsCurrentTab() {
        return this.isCurrentTab;
    }

    public void setIsCurrentTab(boolean isCurrentTab) {
        this.isCurrentTab = isCurrentTab;
    }

    public boolean getIsCurrentTabGroup() {
        return this.isCurrentTabGroup;
    }

    public void setIsCurrentTabGroup(boolean isCurrentTabGroup) {
        this.isCurrentTabGroup = isCurrentTabGroup;
    }

    public int getGroupTabIndex() {
        return this.groupTabIndex;
    }

    public void setGroupTabIndex(int groupTabIndex) {
        this.groupTabIndex = groupTabIndex;
    }

    public String getSearchWord() {
        return this.searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getBundle() {
        return this.bundle;
    }

    public void setBundle(byte[] bundle) {
        this.bundle = bundle;
    }

    @Override
    public String toString() {
        return "TabData{" +
                "id=" + id +
                ", tabCount=" + tabCount +
                ", tabGroupCount=" + tabGroupCount +
                ", time=" + time +
                ", pageNo=" + pageNo +
                ", tabIndex=" + tabIndex +
                ", isCurrentTab=" + isCurrentTab +
                ", isCurrentTabGroup=" + isCurrentTabGroup +
                ", groupTabIndex=" + groupTabIndex +
                ", searchWord='" + searchWord + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", bundle=" + Arrays.toString(bundle) +
                '}';
    }
}
