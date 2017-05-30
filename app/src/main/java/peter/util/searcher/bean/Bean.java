package peter.util.searcher.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by peter on 16/4/8.
 */
public class Bean implements Parcelable {
    public long time;
    public String name;
    public String url;
    public int pageNo;

    public Bean(){}

    public Bean(String name) {
        this.name = name;
    }

    public Bean(String name, String url) {
        this.name = name;
        this.url = url;
    }

    protected Bean(Parcel in) {
        time = in.readLong();
        name = in.readString();
        url = in.readString();
        pageNo = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(time);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeInt(pageNo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Bean> CREATOR = new Creator<Bean>() {
        @Override
        public Bean createFromParcel(Parcel in) {
            return new Bean(in);
        }

        @Override
        public Bean[] newArray(int size) {
            return new Bean[size];
        }
    };
}
