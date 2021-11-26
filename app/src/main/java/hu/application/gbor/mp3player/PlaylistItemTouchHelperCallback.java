package hu.application.gbor.mp3player;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class PlaylistItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final PlaylistItemTouchHelper mPlaylistItemTouchHelper;
    private boolean mIsEditable;

    PlaylistItemTouchHelperCallback(PlaylistItemTouchHelper mPlaylistItemTouchHelper) {
        this.mPlaylistItemTouchHelper = mPlaylistItemTouchHelper;
    }

    void setEditable(boolean editable) {
        mIsEditable = editable;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (mIsEditable)
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,ItemTouchHelper.LEFT);
        return 0;

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mPlaylistItemTouchHelper.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mPlaylistItemTouchHelper.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        PlaylistItemTouchHelperViewHolder playlistItemTouchHelperViewHolder = (PlaylistItemTouchHelperViewHolder) viewHolder;
        playlistItemTouchHelperViewHolder.itemClear();
    }
}
