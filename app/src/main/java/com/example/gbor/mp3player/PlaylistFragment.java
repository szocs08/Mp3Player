package com.example.gbor.mp3player;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class PlaylistFragment extends ListFragment{


    private OnPlaylistFragmentInteractionListener mInteractionListener;
    private SongAdapter mSongAdapter;
    private Activity mActivity;
    private TextView mPlaylistName;
    private TreeMap<String,Boolean> mItemSelectionMap = new TreeMap<>();
    private List<Integer> mPositions = new ArrayList<>();
    public enum DialogTypes {
        SWITCHING,
        ADDING
    }



    public interface OnPlaylistFragmentInteractionListener {
        void startSelectedSong(int position);
        void longClick(int positon);
    }

    public void updateUI(int pos){
        mSongAdapter.updatePosition(pos);
    }

    public void changeCursor(Cursor newCursor) {
        if(mSongAdapter ==null) {
            mSongAdapter = new SongAdapter(getActivity(), null,0);
            setListAdapter(mSongAdapter);
        }
        if (newCursor != null) {
            mSongAdapter.changeCursor(newCursor);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
//        v.setSelected(true);
//        l.invalidate();
        mInteractionListener.startSelectedSong(position);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            mActivity = getActivity();
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mPlaylistName = view.findViewById(R.id.playlist_name);
        ImageButton playlistEditButton = view.findViewById(R.id.playlist_button);
        ImageButton playlistSongAddButton = view.findViewById(R.id.playlist_add_button);
        playlistEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DialogTypes.SWITCHING);
            }
        });
        playlistSongAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DialogTypes.ADDING);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        mPositions.add(position);
                        mSongAdapter.notifyDataSetChanged();
                        return true;
                    }
                }
        );

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

    public void showDialog(DialogTypes type) {
        FragmentManager manager = mActivity.getFragmentManager();
        Bundle arg = new Bundle();
        arg.putSerializable("type",type);
        PlaylistDialogFragment dialog = new PlaylistDialogFragment();
        dialog.setArguments(arg);
        dialog.show(manager,"PlaylistSelectionDialog");

    }

    public void changeName(String name){
        mPlaylistName.setText(name);
    }

    private class SongAdapter extends CursorAdapter {

        private int mCurrent;

        SongAdapter(Context context, Cursor c,int position) {
            super(context, c, 0);
            mCurrent = position;
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(getContext()).inflate(R.layout.item_playlist,parent,false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView songArtist = view.findViewById(R.id.songArtist);
            TextView songTitle = view.findViewById(R.id.songTitle);
            songArtist.setSelected(false);
            songTitle.setSelected(false);
            if (cursor.getPosition()== mCurrent) {
                view.setBackgroundResource(R.drawable.list_select_bg);
                songArtist.setSelected(true);
                songTitle.setSelected(true);
            }else if(mPositions.contains(cursor.getPosition()))
                view.setBackgroundResource(R.drawable.list_edit_bg);
            else
                view.setBackgroundResource(R.drawable.list_bg);

            songArtist.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            songTitle.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        }

        void updatePosition(int pos){
            mCurrent =pos;
            notifyDataSetChanged();
        }

    }

}
