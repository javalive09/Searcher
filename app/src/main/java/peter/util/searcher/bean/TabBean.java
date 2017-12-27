//package peter.util.searcher.bean;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
///**
// * load 操作的基本数据
// * Created by peter on 16/4/8.
// */
//public class TabBean implements Parcelable {
//    public long time;
//    public String name;
//    public String url;
//    public int pageNo;
//
//    public TabBean() {
//    }
//
//    public TabBean(String name) {
//        this.name = name;
//    }
//
//    public TabBean(String name, String url) {
//        this.name = name;
//        this.url = url;
//    }
//
//    protected TabBean(Parcel in) {
//        time = in.readLong();
//        name = in.readString();
//        url = in.readString();
//        pageNo = in.readInt();
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeLong(time);
//        dest.writeString(name);
//        dest.writeString(url);
//        dest.writeInt(pageNo);
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    public static final Creator<TabBean> CREATOR = new Creator<TabBean>() {
//        @Override
//        public TabBean createFromParcel(Parcel in) {
//            return new TabBean(in);
//        }
//
//        @Override
//        public TabBean[] newArray(int size) {
//            return new TabBean[size];
//        }
//    };
//}
