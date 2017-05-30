package peter.util.searcher.bean;

import com.google.gson.annotations.SerializedName;

public class ItemItem{

	private int pageNo;

	@SerializedName("name")
	private String name;

	@SerializedName("icon")
	private String icon;

	@SerializedName("url")
	private String url;

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setIcon(String icon){
		this.icon = icon;
	}

	public String getIcon(){
		return icon;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	@Override
 	public String toString(){
		return 
			"ItemItem{" + 
			"name = '" + name + '\'' + 
			",icon = '" + icon + '\'' + 
			",url = '" + url + '\'' + 
			"}";
		}
}