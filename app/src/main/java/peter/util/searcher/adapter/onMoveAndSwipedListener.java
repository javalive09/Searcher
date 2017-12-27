package peter.util.searcher.adapter;

/**
 * listener
 */
public interface onMoveAndSwipedListener {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

}
