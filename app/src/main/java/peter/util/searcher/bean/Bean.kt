package peter.util.searcher.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * load 操作的基本数据
 * Created by peter on 16/4/8.
 */
class Bean : Parcelable {
    var time: Long = 0
    var name: String? = null
    var url: String? = null
    var pageNo: Int = 0

    constructor()

    constructor(name: String?) {
        this.name = name
    }

    constructor(name: String?, url: String) {
        this.name = name
        this.url = url
    }

    constructor(`in`: Parcel) {
        time = `in`.readLong()
        name = `in`.readString()
        url = `in`.readString()
        pageNo = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(time)
        dest.writeString(name)
        dest.writeString(url)
        dest.writeInt(pageNo)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Bean> = object : Parcelable.Creator<Bean> {
            override fun createFromParcel(`in`: Parcel): Bean = Bean(`in`)

            override fun newArray(size: Int): Array<Bean?> = arrayOfNulls(size)
        }
    }
}
