package com.example.gbor.mp3player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class PlaylistFragment extends Fragment implements PlaylistItemDragListener{


    private OnPlaylistFragmentInteractionListener mPlaylistFragmentInteractionListener;
    private SongAdapter mSongAdapter;
    private Activity mActivity;
    private TextView mPlaylistName;
    private ImageButton mPlaylistButton;
    private ImageButton mPlaylistSongAddButton;
    private ImageButton mPlaylistSongRemoveButton;
    private List<Integer> mPositions = new ArrayList<>();
    private RecyclerView mPlaylist;
    private PlaylistItemTouchHelperCallback mItemTouchHelperCallback;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mIsSelecting = false;


    public enum DialogTypes {
        SWITCHING,
        ADDING
    }


    public interface OnPlaylistFragmentInteractionListener {
        void startSelectedSong(int position);
        void removeSelectedSongs(List<Integer> positions);
        void removeSelectedSong(int position);
        void swapSongPositions(int from, int to);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mActivity = getActivity();
        }



    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mPlaylistName = view.findViewById(R.id.playlist_name);
        mPlaylistButton = view.findViewById(R.id.playlist_button);
        mPlaylistSongAddButton = view.findViewById(R.id.playlist_add_button);
        mPlaylistSongRemoveButton = view.findViewById(R.id.playlist_remove_button);
        mPlaylistSongAddButton.setEnabled(false);
        mPlaylistSongRemoveButton.setEnabled(false);
        mPlaylist = view.findViewById(R.id.playlist);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mPlaylist.setLayoutManager(mLayoutManager);
        mPlaylist.setAdapter(mSongAdapter);
        CustomDividerItemDecoration itemDecor = new CustomDividerItemDecoration(mActivity,
                ContextCompat.getColor(mActivity,R.color.playlist_background),1);
        mPlaylist.addItemDecoration(itemDecor);
        mPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DialogTypes.SWITCHING);
            }
        });
        mPlaylistSongAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DialogTypes.ADDING);

            }
        });
        mPlaylistSongRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlaylistFragmentInteractionListener.removeSelectedSongs(mPositions);
//                mSongAdapter.notifyDataSetChanged();
            }
        });


        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPlaylistFragmentInteractionListener) {
            mPlaylistFragmentInteractionListener = (OnPlaylistFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPlaylistFragmentInteractionListener = null;
    }

    @Override
    public void onDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void updateUI(int pos){
        mSongAdapter.updatePosition(pos);
        if (mPlaylistName!= null) {
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mPlaylist);
            if(mPlaylistName.getText() != getString(R.string.all_songs)){
                mItemTouchHelperCallback.setEditable(true);
            }else {
                mItemTouchHelperCallback.setEditable(false);
            }
        }
    }

    public void changeCursor(Cursor newCursor) {
        if(mSongAdapter == null) {
            mSongAdapter = new SongAdapter(null,0, this);
            mSongAdapter.setHasStableIds(true);
            mItemTouchHelperCallback = new PlaylistItemTouchHelperCallback(mSongAdapter);
        }
        if (newCursor != null) {
            updateUI(0);
            mSongAdapter.changeCursor(newCursor);
        }
    }

    private void selecting(int position){
        if(mPositions.contains(position)){
            mPositions.remove(Integer.valueOf(position));
        }else {
            mPositions.add(position);
        }

        if (mPositions.isEmpty()) {
            mIsSelecting = false;
            mPlaylistButton.setEnabled(true);
            mPlaylistSongRemoveButton.setEnabled(false);
            mPlaylistSongAddButton.setEnabled(false);
        }

    }

    public void showDialog(DialogTypes type) {
        FragmentManager manager = mActivity.getFragmentManager();
        Bundle arg = new Bundle();
        arg.putSerializable("type",type);
        arg.putString("name",mPlaylistName.getText().toString());
        PlaylistDialogFragment dialog = new PlaylistDialogFragment();
        dialog.setArguments(arg);
        dialog.show(manager,"PlaylistSelectionDialog");

    }

    public void changeName(String name){
        mPlaylistName.setText(name);
    }

    public List<Integer> getSelectedSongs(){
        return mPositions;
    }

    public void resetPlaylistUI(){
        mPositions.clear();
        mIsSelecting = false;
        mPlaylistButton.setEnabled(true);
        mPlaylistSongRemoveButton.setEnabled(false);
        mPlaylistSongAddButton.setEnabled(false);
    }

    private class SongAdapter extends RecyclerView.Adapter<SongAdapter.PlaylistViewHolder>
            implements PlaylistItemTouchHelper{

        private int mCurrent;
        private Cursor mCursor;
        PlaylistItemDragListener mPlaylistItemDragListener;


        class PlaylistViewHolder extends RecyclerView.ViewHolder implements PlaylistItemTouchHelperViewHolder{
            TextView songArtist;
            TextView songTitle;
            ImageView dragIcon;


            PlaylistViewHolder(View itemView) {
                super(itemView);
                songArtist = itemView.findViewById(R.id.songArtist);
                songTitle = itemView.findViewById(R.id.songTitle);
                dragIcon = itemView.findViewById(R.id.drag_icon);
            }

            @Override
            public void onItemSelected() {

            }

            @Override
            public void itemClear() {

            }
        }

        SongAdapter(Cursor c,int position, PlaylistItemDragListener playlistItemDragListener) {
            mCurrent = position;
            mCursor = c;
            mPlaylistItemDragListener = playlistItemDragListener;
        }

        @SuppressLint("ClickableViewAccessibility")
        @NonNull
        @Override
        public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mActivity).inflate(R.layout.item_playlist,parent,false);
            final PlaylistViewHolder playlistViewHolder = new PlaylistViewHolder(view);
            playlistViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mPlaylist.getChildAdapterPosition(v);
                    if (mIsSelecting) {
                        selecting(position);
                        mSongAdapter.notifyDataSetChanged();
                    }else
                        mPlaylistFragmentInteractionListener.startSelectedSong(position);
                }
            });
            playlistViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = mPlaylist.getChildAdapterPosition(v);
                    mIsSelecting = true;
                    mPlaylistButton.setEnabled(false);
                    mPlaylistSongAddButton.setEnabled(true);
                    if (!mPlaylistName.getText().equals(getString(R.string.all_songs))) {
                        mPlaylistSongRemoveButton.setEnabled(true);
                    }
                    selecting(position);
                    mSongAdapter.notifyDataSetChanged();
                    return true;
                }
            });
            playlistViewHolder.dragIcon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() ==
                            MotionEvent.ACTION_DOWN) {
                        mPlaylistItemDragListener.onDrag(playlistViewHolder);
                    }
                    return true;
                }
            });
            playlistViewHolder.dragIcon.performClick();
            return playlistViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.songArtist.setSelected(false);
            holder.songTitle.setSelected(false);
            if(mPositions.contains(mCursor.getPosition())) {
                holder.itemView.setBackgroundResource(R.drawable.list_edit_bg);
                holder.dragIcon.setBackgroundResource(R.drawable.border_drag_edit);
            }else if (mCursor.getPosition()== mCurrent) {
                holder.itemView.setBackgroundResource(R.drawable.list_select_bg);
                holder.dragIcon.setBackgroundResource(R.drawable.border_drag_selected);
                holder.songArtist.setSelected(true);
                holder.songTitle.setSelected(true);
            }else{
                holder.itemView.setBackgroundResource(R.drawable.list_bg);
                holder.dragIcon.setBackgroundResource(R.drawable.border_drag);
            }
            holder.songArtist.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            holder.songTitle.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            holder.dragIcon.setImageResource(R.drawable.ic_drag_handle);

        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }

        void changeCursor(Cursor newCursor) {
            Cursor old = mCursor;
            mCursor = newCursor;
            if (old != null)
                old.close();
            notifyDataSetChanged();
        }

        void updatePosition(int pos){
            mCurrent =pos;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            mPlaylistFragmentInteractionListener.swapSongPositions(fromPosition,toPosition);
            notifyDataSetChanged();
//            notifyItemMoved(fromPosition,toPosition);

        }

        @Override
        public void onItemDismiss(int position) {
            mPlaylistFragmentInteractionListener.removeSelectedSong(position);
        }
    }

}
