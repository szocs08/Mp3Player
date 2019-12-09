package hu.application.gbor.mp3player;

public interface PlaylistItemTouchHelper {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

}
