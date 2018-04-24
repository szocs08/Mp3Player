package com.example.gbor.mp3player;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
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
import java.util.List;


public class FragmentPlaylist extends ListFragment {

    private class SongAdapter extends ArrayAdapter<HashMap<String, String>> {

//        private final ArrayList<String> songList;
        private final Activity context;
        private int current;
        private List<HashMap<String, String>> songList;
        private Cursor cursor;

        SongAdapter(Activity context, ArrayList<HashMap<String, String>> songList, int currentlyPlaying) {
            super(context, R.layout.playlist_item, songList);
            this.current=currentlyPlaying;
            this.context = context;
            this.songList = songList;


        }

        void updatePosition(int pos){
            current=pos;
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == cursor) {
                return null;
            }
            Cursor oldCursor = cursor;
            cursor = newCursor;
            if (newCursor != null) {
                notifyDataSetChanged();
            }
            return oldCursor;
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
            holder.songArtist.setText(songList.get(position).get("artist"));
            holder.songTitle.setText(songList.get(position).get("title"));

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
        ArrayList<HashMap<String, String>> songListData = new ArrayList<>();
        HashMap<String, String> songData;
        HashMap<String, String> songDataTemp;
        for (String id : playlist) {
            songData = new HashMap<>();
            songDataTemp = SongManager.getSongPlaylistData(getContext(),id);
            if(songDataTemp.get("artist") == null
                    || songDataTemp.get("title") == null){
                songData.put("artist",songDataTemp.get("displayName"));
                songData.put("title","");
            }else{
                songData.put("artist",songDataTemp.get("artist"));
                songData.put("title",songDataTemp.get("title"));
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
