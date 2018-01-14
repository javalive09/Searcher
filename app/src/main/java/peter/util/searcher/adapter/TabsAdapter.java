package peter.util.searcher.adapter;

import android.content.res.ColorStateList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.activity.TabsActivity;
import peter.util.searcher.tab.TabGroup;

/**
 * tabs adapter
 */
public class TabsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements onMoveAndSwipedListener {

    private TabsActivity context;
    private List<TabGroup> mItems = new ArrayList<>();

    public TabsAdapter(TabsActivity context) {
        this.context = context;
    }

    public void setItems(List<TabGroup> data) {
        this.mItems.addAll(data);
        notifyDataSetChanged();
    }

    public void updateItems(List<TabGroup> data) {
        mItems.clear();
        setItems(data);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_item_recycler_view, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof RecyclerViewHolder) {
            final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;

            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
            recyclerViewHolder.mView.startAnimation(animation);

            TabGroup tabGroup = mItems.get(position);

            if (TabGroupManager.getInstance().getCurrentTabGroup() == tabGroup) {
                recyclerViewHolder.selectTip.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.active)));
            } else {
                recyclerViewHolder.selectTip.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.no_active)));
            }
            recyclerViewHolder.title.setText(tabGroup.getTitle());
            recyclerViewHolder.url.setText(tabGroup.getUrl());
            recyclerViewHolder.icon.setBackground(tabGroup.getCurrentTab().getIconDrawable());

            recyclerViewHolder.mView.setOnClickListener(view -> {
                TabGroupManager.getInstance().switchTabGroup(tabGroup);
                context.finish();
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(final int position) {
        final TabGroup tabGroup = mItems.get(position);
        boolean dismissActiveItem = false;
        if (tabGroup == TabGroupManager.getInstance().getCurrentTabGroup()) {
            dismissActiveItem = true;
        }
        TabGroupManager.getInstance().removeTabGroup(tabGroup);
        if (TabGroupManager.getInstance().getTabGroupCount() > 0) {
            mItems.remove(position);
            notifyItemRemoved(position);
            if (dismissActiveItem) {
                int currentIndex = TabGroupManager.getInstance().getCurrentTabIndex();
                notifyItemChanged(currentIndex);
            }
        } else {
            context.saveAndExit();
        }
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        @BindView(R.id.selectTip)
        RelativeLayout selectTip;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.url)
        TextView url;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

}
