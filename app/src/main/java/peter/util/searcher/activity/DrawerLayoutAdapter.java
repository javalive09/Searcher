package peter.util.searcher.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import peter.util.searcher.R;

/**
 * Adapter for the planet data used in our drawer menu,
 */
public class DrawerLayoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int VERSION = 0;
    static final int HOT_LIST = 1;
    static final int CUSTOM = 2;
    private OnItemClickListener mListener;
    List<TypeBean> list;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnItemClickListener {
        void onClick(View view, TypeBean bean);
    }

    /**
     * Custom version view holder.
     */
    public static class VersionViewHolder extends RecyclerView.ViewHolder {
        TextView version;
        public VersionViewHolder(TextView version) {
            super(version);
            this.version = version;
        }
    }
    /**
     * Custom hotList view holder.
     */
    public static class HotListViewHolder extends RecyclerView.ViewHolder {
        TextView hotList;
        public HotListViewHolder(TextView hotList) {
            super(hotList);
            this.hotList = hotList;
        }
    }

    public DrawerLayoutAdapter(List<TypeBean> list, OnItemClickListener listener) {
        this.list = list;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VERSION:
                View v = vi.inflate(R.layout.searcher_item_version, parent, false);
                TextView version = (TextView) v.findViewById(R.id.version);
                holder = new VersionViewHolder(version);
                break;
            case CUSTOM:
            case HOT_LIST:
                v = vi.inflate(R.layout.searcher_item_hoslist, parent, false);
                TextView hotList = (TextView) v.findViewById(R.id.hot_list);
                holder = new HotListViewHolder(hotList);
                break;
        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HotListViewHolder){
            ((HotListViewHolder)holder).hotList.setText(list.get(position).content);
            ((HotListViewHolder)holder).hotList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClick(view, list.get(holder.getAdapterPosition()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class TypeBean{
        int type;
        int id;
        String content;
        String url;

        public TypeBean(int type) {
            this.type = type;
        }

        public TypeBean(int type, int id, String content, String url) {
            this.type = type;
            this.id = id;
            this.content = content;
            this.url = url;
        }
    }
}
