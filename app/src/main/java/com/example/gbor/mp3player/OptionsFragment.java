package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Context;
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

public class OptionsFragment extends ListFragment {


    private class OptionsAdapter extends ArrayAdapter<HashMap<String, String>> {

        private final ArrayList<HashMap<String, String>> songData;
        private final Activity context;


        public OptionsAdapter(Activity context, ArrayList<HashMap<String, String>> songData) {
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

    private OnOptionsFragmentInteractionListener interactionListener;


    public interface OnOptionsFragmentInteractionListener {
        void asd(int position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        interactionListener.asd(position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.options_list,android.R.layout.simple_list_item_1);
        setListAdapter(arrayAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.options, container, false);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOptionsFragmentInteractionListener) {
            interactionListener = (OnOptionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOptionsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }



}
