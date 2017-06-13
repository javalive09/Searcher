package peter.util.searcher.bean;

import com.google.gson.annotations.SerializedName;

public class UrlInfo {

	@SerializedName("url")
	private String url;

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	@Override
 	public String toString(){
		return 
			"UrlInfo{" +
			"url = '" + url + '\'' + 
			"}";
		}
}