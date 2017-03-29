package com.example.gbor.mp3player;

import android.app.ListActivity;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayListActivity extends ListActivity {

    public ArrayList<String> songList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        ArrayList<HashMap<String,String>> songListData = new ArrayList<HashMap<String, String>>();
        HashMap<String,String> songData;
        SongsManager plm = new SongsManager();

        songList = plm.getPlaylist();

        for (String song: songList) {
            mmr.setDataSource(song);
            songData = new HashMap<String,String>();
            songData.put("songArtist",mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            songData.put("songTitle",mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            songListData.add(songData);
        }


        ListAdapter adapter = new SimpleAdapter(this, songListData,
                R.layout.playlist_item,new String[]{"songTitle"}, new int[] {R.id.songTitle});

        setListAdapter(adapter);

        ListView lv = getListView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int songIndex = position;
                Toast.makeText(getApplicationContext(),"asdfg",Toast.LENGTH_SHORT).show();
                //Intent in = new Intent(getApplicationContext(),PlayerActivity.class);

               // in.putExtra("songIndex", songIndex);
                //setResult(100,in);
                //finish();
            }
        });
    }
}
