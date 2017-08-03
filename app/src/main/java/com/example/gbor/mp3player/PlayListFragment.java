package com.example.gbor.mp3player;

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


public class PlayListFragment extends ListFragment {

    private class SongAdapter extends ArrayAdapter<HashMap<String, String>> {

        private final ArrayList<HashMap<String, String>> songData;
        private final Activity context;


        public SongAdapter(Activity context, ArrayList<HashMap<String, String>> songData) {
            super(context, R.layout.playlist_item, songData);
            this.context = context;
            this.songData = songData;


        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = context.getLayoutInflater();
            View v = li.inflate(R.layout.playlist_item, null, true);
            TextView songArtist = (TextView) v.findViewById(R.id.songArtist);
            TextView songTitle = (TextView) v.findViewById(R.id.songTitle);

            songArtist.setText(songData.get(position).get("songArtist"));
            songTitle.setText(songData.get(position).get("songTitle"));

            return v;
        }
    }

    private OnFragmentInteractionListener interactionListener;

    public interface OnFragmentInteractionListener {
        void startSong(int position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        interactionListener.startSong(position);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        ArrayList<HashMap<String, String>> songListData = new ArrayList<>();
        HashMap<String, String> songData;

        for (String song : getArguments().getStringArrayList("playlist")) {
            mmr.setDataSource(song);
            songData = new HashMap<>();
            songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            songListData.add(songData);
        }
        Context context = getActivity();
        if (context instanceof OptionsFragment.OnOptionsFragmentInteractionListener) {
            interactionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(getActivity().toString()
                    + " must implement OnFragmentInteractionListener");
        }

        setListAdapter(new SongAdapter(getActivity(), songListData));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            interactionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }


}
