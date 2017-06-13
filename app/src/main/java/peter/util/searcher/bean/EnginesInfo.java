package peter.util.searcher.bean;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class EnginesInfo {

	@SerializedName("engines")
	private List<EnginesItem> engines;

	public void setEngines(List<EnginesItem> engines){
		this.engines = engines;
	}

	public List<EnginesItem> getEngines(){
		return engines;
	}

	@Override
 	public String toString(){
		return 
			"EnginesInfo{" +
			"engines = '" + engines + '\'' + 
			"}";
		}
}