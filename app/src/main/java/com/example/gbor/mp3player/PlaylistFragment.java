package com.example.gbor.mp3player;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class PlaylistFragment extends ListFragment {

    private OnListFragmentInteractionListener mInteractionListener;
    private SongAdapter mSongAdapter;

    public interface OnListFragmentInteractionListener {
        void startSelectedSong(int position);
    }

    public void updateUI(int pos){
        mSongAdapter.updatePosition(pos);
        mSongAdapter.notifyDataSetChanged();
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        v.setSelected(true);
        l.invalidate();
        mInteractionListener.startSelectedSong(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListFragmentInteractionListener) {
            mInteractionListener = (OnListFragmentInteractionListener) context;
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
            songArtist.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            songTitle.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            if (cursor.getPosition()== mCurrent) {
                view.setBackgroundResource(R.drawable.list_select_bg);
                songArtist.setSelected(true);
                songTitle.setSelected(true);
            }else {
                view.setBackgroundResource(R.drawable.list_bg);
                songArtist.setSelected(false);
                songTitle.setSelected(false);
            }

        }

        void updatePosition(int pos){
            mCurrent =pos;
            notifyDataSetChanged();
        }

    }

}
