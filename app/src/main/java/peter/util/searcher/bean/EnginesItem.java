package peter.util.searcher.bean;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class EnginesItem{

	@SerializedName("item")
	private List<ItemItem> item;

	@SerializedName("title")
	private String title;

	public void setItem(List<ItemItem> item){
		this.item = item;
	}

	public List<ItemItem> getItem(){
		return item;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	@Override
 	public String toString(){
		return 
			"EnginesItem{" + 
			"item = '" + item + '\'' + 
			",title = '" + title + '\'' + 
			"}";
		}
}