package peter.util.searcher.bean

import com.google.gson.annotations.SerializedName

class ItemItem {

    var pageNo: Int = 0

    @SerializedName("name")
    var name: String? = null

    @SerializedName("icon")
    var icon: String? = null

    @SerializedName("url")
    var url: String? = null


}