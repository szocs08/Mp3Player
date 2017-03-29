/*
fragment
list view
facebook
options
playlist save/load

 */

package com.example.gbor.mp3player;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PlayerActivity extends FragmentActivity implements MediaPlayer.OnCompletionListener,
        SeekBar.OnSeekBarChangeListener{

    private ImageButton btnPlay;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnShuffle;
    private ImageButton btnRepeat;
    private Button btnPlaylist;
    private SeekBar progressSekbar;
    private TextView songTitleLabel;
    private TextView songArtistLabel;
    private TextView songAlbumLabel;
    private TextView currentTimeLabel;
    private TextView totalTimeLabel;

    private MediaPlayer mp;

    private Handler mHandler = new Handler();
    private SongsManager songsManager;
    private int songIndex;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String,String>> songList = new ArrayList<HashMap<String, String>>();
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);

        HashMap<String,String> songData = new HashMap<String,String>();

        btnPlay = (ImageButton)findViewById(R.id.play_button);
        btnPrevious = (ImageButton)findViewById(R.id.previous_button);
        btnNext = (ImageButton)findViewById(R.id.next_button);
        btnShuffle = (ImageButton)findViewById(R.id.shuffle_button);
        btnRepeat = (ImageButton)findViewById(R.id.repeat_button);
        btnPlaylist = (Button)findViewById(R.id.button_playlist);
        progressSekbar = (SeekBar)findViewById(R.id.seek_bar);
        songTitleLabel = (TextView)findViewById(R.id.song_title);
        songArtistLabel = (TextView)findViewById(R.id.song_artist);
        songAlbumLabel = (TextView)findViewById(R.id.song_album);
        currentTimeLabel = (TextView)findViewById(R.id.current_time);
        totalTimeLabel = (TextView)findViewById(R.id.end_time);

        mp = new MediaPlayer();
        songsManager = new SongsManager();

        progressSekbar.setOnSeekBarChangeListener(this);
        mp.setOnCompletionListener(this);

        for (String song: songsManager.getPlaylist()) {
            mmr.setDataSource(song);
            songData.put("songArtist",mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            songData.put("songTitle",mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            songList.add(songData);
        }


        btnPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),PlayListActivity.class);
                startActivityForResult(i,100);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songIndex < songList.size()-1){
                    songIndex++;
                    playSong(songIndex);
                }else{
                    songIndex =0;
                    playSong(songIndex);
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songIndex > 0){
                    songIndex--;
                    playSong(songIndex);
                }else{
                    songIndex =songList.size()-1;
                    playSong(songIndex);
                }
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(),"Repeat is OFF",Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.repeat_button);
                }else{
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(),"Repeat is ON",Toast.LENGTH_SHORT).show();

                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.img_repeat_pressed);
                    btnShuffle.setImageResource(R.drawable.shuffle_button);
                }
            }
        });


        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(),"Shuffle is OFF",Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.shuffle_button);
                }else{
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(),"Shuffle is ON",Toast.LENGTH_SHORT).show();

                    isRepeat = false;
                    btnRepeat.setImageResource(R.drawable.repeat_button);
                    btnShuffle.setImageResource(R.drawable.img_shuffle_pressed);
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.pause();
                    btnPlay.setImageResource(R.drawable.play_button);
                }else{
                    mp.start();
                    btnPlay.setImageResource(R.drawable.pause_button);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==100){
            songIndex = data.getExtras().getInt("SongIndex");

            playSong(songIndex);
        }
    }

    public void playSong(int songindex) {
        try{
            mp.reset();
            mp.setDataSource(songList.get(songindex).get("songPath"));
            mp.prepare();
            mp.start();

            String songTitle = songList.get(songindex).get("songTitle");
            songTitleLabel.setText(songTitle);

            btnPlay.setImageResource(R.drawable.img_pause);

            progressSekbar.setProgress(0);
            progressSekbar.setMax(100);

            updateProgressBar();
        }catch (IllegalArgumentException|IOException|IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask,100);
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            currentTimeLabel.setText(""+Utilities.milliSecondsToTimer(currentDuration));
            totalTimeLabel.setText(""+Utilities.milliSecondsToTimer(totalDuration));

            int progress = Utilities.getProgressPercentage(currentDuration,totalDuration);

            progressSekbar.setProgress(progress);

            mHandler.postDelayed(this,100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mp.seekTo(Utilities.progressToTimer(seekBar.getProgress(),mp.getDuration()));

        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(isRepeat){
            songIndex = songIndex;
        } else if(isShuffle){
            Random rnd = new Random();
            songIndex = rnd.nextInt((songList.size() - 1) - 1);
        } else {
            if(songIndex < songList.size()-1){
                songIndex++;

            }else{
                songIndex =0;

            }
        }
        playSong(songIndex);
    }
}
