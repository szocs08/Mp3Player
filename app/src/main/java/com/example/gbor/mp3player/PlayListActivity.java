package com.example.gbor.mp3player;

import android.app.ListActivity;
import android.content.Intent;
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

    public ArrayList<HashMap<String,String>> songList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        ArrayList<HashMap<String,String>> songListData = new ArrayList<HashMap<String, String>>();

        SongsManager plm = new SongsManager();

        songList = plm.getPlaylist();

        for (HashMap<String,String> song: songList) {
            songListData.add(song);
        }
        Toast.makeText(getApplicationContext(),songListData.get(0).get("songTitle"),Toast.LENGTH_SHORT).show();


        ListAdapter adapter = new SimpleAdapter(this, songListData,
                R.layout.playlist_item,new String[]{"songTitle"}, new int[] {R.id.song_title});

        setListAdapter(adapter);

        ListView lv = getListView();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int songIndex = position;

                Intent in = new Intent(getApplicationContext(),PlayerActivity.class);

                in.putExtra("songIndex", songIndex);
                setResult(100,in);
                finish();
            }
        });
    }
}
