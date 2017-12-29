package peter.util.searcher.adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.tab.TabGroup;

/**
 * tabs adapter
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements onMoveAndSwipedListener {

    private TabsActivity context;
    private List<TabGroup> mItems = new ArrayList<>();

    public RecyclerViewAdapter(TabsActivity context) {
        this.context = context;
    }

    public void setItems(List<TabGroup> data) {
        this.mItems.addAll(data);
        notifyDataSetChanged();
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

            AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.1f);
            aa1.setDuration(400);
            recyclerViewHolder.rela_round.startAnimation(aa1);

            AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
            aa.setDuration(400);

            recyclerViewHolder.rela_round.startAnimation(aa);

            TabGroup tabGroup = mItems.get(position);

            if (TabGroupManager.getInstance().getCurrentTabGroup() == tabGroup) {
                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.active)));
            } else {
                recyclerViewHolder.rela_round.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.no_active)));
            }
            recyclerViewHolder.title.setText(tabGroup.getTitle());
            recyclerViewHolder.host.setText(tabGroup.getHost());
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
            DaoManager.getInstance().saveTabs();
            context.exit();
        }
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        @BindView(R.id.rela_round)
        RelativeLayout rela_round;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.url)
        TextView url;
        @BindView(R.id.host)
        TextView host;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

}
