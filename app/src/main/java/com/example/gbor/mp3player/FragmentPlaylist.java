package com.example.gbor.mp3player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class FragmentPlaylist extends ListFragment {

    private class SongAdapter extends ArrayAdapter<HashMap<String, String>> {

        private final ArrayList<HashMap<String, String>> songData;
        private final Activity context;
        private int current;


        SongAdapter(Activity context, ArrayList<HashMap<String, String>> songData, int currentlyPlaying) {
            super(context, R.layout.playlist_item, songData);
            this.current=currentlyPlaying;
            this.context = context;
            this.songData = songData;


        }

        void updatePosition(int pos){
            current=pos;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = context.getLayoutInflater();
            @SuppressLint("InflateParams") View v = li.inflate(R.layout.playlist_item, null, true);
            TextView songArtist = (TextView) v.findViewById(R.id.songArtist);
            TextView songTitle = (TextView) v.findViewById(R.id.songTitle);
            notifyDataSetChanged();
            songArtist.setText(songData.get(position).get("songArtist"));
            songTitle.setText(songData.get(position).get("songTitle"));

            if (position==current) {
                v.setBackgroundResource(R.drawable.list_select_bg);
                songArtist.setSelected(true);
                songTitle.setSelected(true);
            }else {
                v.setBackgroundResource(R.drawable.list_bg);
            }
            return v;
        }
    }

    private OnListFragmentInteractionListener interactionListener;
    private SongAdapter songAdapter;


    public interface OnListFragmentInteractionListener {
        void startSong(int position);
    }

    public void updateUI(int pos){
        songAdapter.updatePosition(pos);
        songAdapter.notifyDataSetChanged();
    }

    public void updateUI(ArrayList<String> playlist){
        initialize(playlist);
        songAdapter.notifyDataSetChanged();
    }

    private void initialize(ArrayList<String> playlist){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        ArrayList<HashMap<String, String>> songListData = new ArrayList<>();
        HashMap<String, String> songData;

        for (String song : playlist) {
            mmr.setDataSource(song);
            songData = new HashMap<>();
            songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            songListData.add(songData);
        }
        songAdapter = new SongAdapter(getActivity(), songListData,getArguments().getInt("index"));
        setListAdapter(songAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        v.setSelected(true);
        l.invalidate();
        interactionListener.startSong(position);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize(getArguments().getStringArrayList("playlist"));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            interactionListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }


}
