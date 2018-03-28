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

        @NonNull @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater li = context.getLayoutInflater();
            ViewHolder holder;
            if (convertView==null){
                holder = new ViewHolder();
                convertView = li.inflate(R.layout.playlist_item, parent, false);
                holder.songArtist = (TextView) convertView.findViewById(R.id.songArtist);
                holder.songTitle = (TextView) convertView.findViewById(R.id.songTitle);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            notifyDataSetChanged();
            holder.songArtist.setText(songData.get(position).get("songArtist"));
            holder.songTitle.setText(songData.get(position).get("songTitle"));

            if (position==current) {
                convertView.setBackgroundResource(R.drawable.list_select_bg);
                holder.songArtist.setSelected(true);
                holder.songTitle.setSelected(true);
            }else {
                convertView.setBackgroundResource(R.drawable.list_bg);
            }
            return convertView;
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
        Song song;
        ArrayList<HashMap<String, String>> songListData = new ArrayList<>();
        HashMap<String, String> songData;

        for (String entry : playlist) {
            song = new Song(entry);
            songData = new HashMap<>();
            if(song.getArtist() == null || song.getArtist() == null)
                songData.put("songArtist", SongManager.getFileName(song.getPath()));
            else {
                songData.put("songArtist", song.getArtist());
                songData.put("songTitle", song.getTitle());
            }
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

    static class ViewHolder{
        TextView songArtist;
        TextView songTitle;
    }


}
