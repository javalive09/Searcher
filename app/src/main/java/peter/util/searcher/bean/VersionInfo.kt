package peter.util.searcher.bean

import com.google.gson.annotations.SerializedName

class VersionInfo {

    @SerializedName("msg")
    var msg: List<String>? = null

    @SerializedName("code")
    var code: Int = 0

    @SerializedName("num")
    var num: String? = null

    @SerializedName("url")
    var url: String? = null

    val message: String
        get() {
            var showMessage = ""
            for (s in msg!!) {
                showMessage += s + "\n"
            }
            return showMessage
        }

}