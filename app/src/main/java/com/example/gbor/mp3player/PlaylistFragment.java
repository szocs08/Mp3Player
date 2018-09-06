package com.example.gbor.mp3player;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class PlaylistFragment extends Fragment{


    private OnPlaylistFragmentInteractionListener mInteractionListener;
    private SongAdapter mSongAdapter;
    private Activity mActivity;
    private TextView mPlaylistName;
    private ImageButton mPlaylistButton;
    private ImageButton mPlaylistSongAddButton;
    private ImageButton mPlaylistSongRemoveButton;
    private List<Integer> mPositions = new ArrayList<>();
    private RecyclerView mPlaylist;
    private Cursor mCursor;
    private boolean mIsSelecting = false;
    public enum DialogTypes {
        SWITCHING,
        ADDING
    }


    public interface OnPlaylistFragmentInteractionListener {
        void startSelectedSong(int position);
        void removeSelectedSongs(List<Integer> positions);
    }

//    public void onListItemClick(ListView l, View v, int position, long id){
//        if (mIsSelecting) {
//            selecting(position);
//            mSongAdapter.notifyDataSetChanged();
//        }else
//            mInteractionListener.startSelectedSong(position);
//    }

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
        CustomDividerItemDecoration itemDecor = new CustomDividerItemDecoration(mActivity, mLayoutManager.getOrientation());
        itemDecor.setDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.playlist_divider));
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
                mInteractionListener.removeSelectedSongs(mPositions);
            }
        });

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        getListView().setOnItemLongClickListener(
//                new AdapterView.OnItemLongClickListener() {
//                    @Override
//                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                        mIsSelecting = true;
//                        mPlaylistButton.setEnabled(false);
//                        mPlaylistSongAddButton.setEnabled(true);
//                        if (!mPlaylistName.getText().equals(getString(R.string.all_songs))) {
//                            mPlaylistSongRemoveButton.setEnabled(true);
//                        }
//                        selecting(position);
//                        mSongAdapter.notifyDataSetChanged();
//                        return true;
//                    }
//                }
//        );

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPlaylistFragmentInteractionListener) {
            mInteractionListener = (OnPlaylistFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mInteractionListener = null;
    }

    public void updateUI(int pos){
        mSongAdapter.updatePosition(pos);
    }

    public void changeCursor(Cursor newCursor) {
        if(mSongAdapter == null) {
            mSongAdapter = new SongAdapter(null,0);
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
        mSongAdapter.notifyDataSetChanged();
    }

    private class SongAdapter extends RecyclerView.Adapter<SongAdapter.PlaylistViewHolder> {

        private int mCurrent;
        private Cursor mCursor;



        class PlaylistViewHolder extends RecyclerView.ViewHolder{
            TextView songArtist;
            TextView songTitle;

            PlaylistViewHolder(View itemView) {
                super(itemView);
                songArtist = itemView.findViewById(R.id.songArtist);
                songTitle = itemView.findViewById(R.id.songTitle);            }
        }

        SongAdapter(Cursor c,int position) {
            mCurrent = position;
            mCursor = c;
        }

        @NonNull
        @Override
        public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mActivity).inflate(R.layout.item_playlist,parent,false);

            return new PlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.songArtist.setSelected(false);
            holder.songTitle.setSelected(false);
            if (mCursor.getPosition()== mCurrent) {
                holder.itemView.setBackgroundResource(R.drawable.list_select_bg);
                holder.songArtist.setSelected(true);
                holder.songTitle.setSelected(true);
            }else
                holder.itemView.setBackgroundResource(R.drawable.list_bg);
            if(mPositions.contains(mCursor.getPosition()))
                holder.itemView.setBackgroundResource(R.drawable.list_edit_bg);
            holder.songArtist.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            holder.songTitle.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));

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



//        @Override
//        public View newView(Context context, Cursor cursor, ViewGroup parent) {
//            return LayoutInflater.from(getContext()).inflate(R.layout.item_playlist,parent,false);
//        }
//
//        @Override
//        public void bindView(View view, Context context, Cursor cursor) {
//            TextView songArtist = view.findViewById(R.id.songArtist);
//            TextView songTitle = view.findViewById(R.id.songTitle);
//            songArtist.setSelected(false);
//            songTitle.setSelected(false);
//            if (cursor.getPosition()== mCurrent) {
//                view.setBackgroundResource(R.drawable.list_select_bg);
//                songArtist.setSelected(true);
//                songTitle.setSelected(true);
//            }else
//                view.setBackgroundResource(R.drawable.list_bg);
//            if(mPositions.contains(cursor.getPosition()))
//                view.setBackgroundResource(R.drawable.list_edit_bg);
//            songArtist.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
//            songTitle.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
//        }

        void updatePosition(int pos){
            mCurrent =pos;
            notifyDataSetChanged();
        }

    }

}
