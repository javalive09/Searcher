package peter.util.searcher.bean;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class VersionInfo {

	@SerializedName("msg")
	private List<String> msg;

	@SerializedName("code")
	private int code;

	@SerializedName("num")
	private String num;

	@SerializedName("url")
	private String url;

	public void setMsg(List<String> msg){
		this.msg = msg;
	}

	public List<String> getMsg(){
		return msg;
	}

	public String getMessage() {
		StringBuilder showMessage = new StringBuilder();
		for(String s : msg) {
			showMessage.append(s).append("\n");
		}
		return showMessage.toString();
	}

	public void setCode(int code){
		this.code = code;
	}

	public int getCode(){
		return code;
	}

	public void setNum(String num){
		this.num = num;
	}

	public String getNum(){
		return num;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	@Override
 	public String toString(){
		return 
			"VersionInfo{" +
			"msg = '" + msg + '\'' + 
			",code = '" + code + '\'' + 
			",num = '" + num + '\'' + 
			",url = '" + url + '\'' + 
			"}";
		}
}