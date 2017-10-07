package peter.util.searcher.bean

import com.google.gson.annotations.SerializedName

class EnginesItem {

    @SerializedName("item")
    var item: List<ItemItem>? = null

    @SerializedName("title")
    var title: String? = null

}