/*

fragment
facebook
options
playlist save/load

 */

package com.example.gbor.mp3player;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.example.gbor.mp3player.R.id.pager;

public class ActivityMainPlayer extends FragmentActivity implements
        FragmentPlayer.OnPlayerFragmentInteractionListener,
        FragmentPlaylist.OnListFragmentInteractionListener,
        MediaPlayer.OnCompletionListener,
        FragmentOptions.OnOptionsFragmentInteractionListener{

    private MediaPlayer mp;

    private FragmentPlayer fragmentPlayer = new FragmentPlayer();
    private FragmentPlaylist fragmentPlaylist = new FragmentPlaylist();
    private FragmentOptions fragmentOptions = new FragmentOptions();

    private int songIndex;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<String> playlist;
    private static final int FOLDER_CHOOSING_REQUEST = 1;
    private String path ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        songIndex = 0;
        path=getString(R.string.default_folder);
        mp = new MediaPlayer();
        playlist = SongManager.getPlaylist(path);
        mp.setOnCompletionListener(this);
        try {
            mp.setDataSource(playlist.get(0));
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PlayerPagerAdapter pagerAdapter = new PlayerPagerAdapter(getSupportFragmentManager(),
                playlist, songIndex, fragmentPlayer, fragmentPlaylist, fragmentOptions, path);

        ViewPager viewPager = (ViewPager) findViewById(pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);


    }

    @Override
    public void optionOperations(int position) {
        if(position==0){
            Intent intent = new Intent(this,ActivityDirectoryChooser.class);
            intent.putExtra("path",path);
            startActivityForResult(intent,FOLDER_CHOOSING_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case FOLDER_CHOOSING_REQUEST:
                if(resultCode==RESULT_OK){


                    path=data.getDataString();
                    if(!SongManager.hasMP3(path))
                        Toast.makeText(this,"asdsadasdsadsadas",Toast.LENGTH_LONG).show();
                    else {
                        mp = new MediaPlayer();
                        playlist = SongManager.getPlaylist(path);
                        mp.setOnCompletionListener(this);
                        try {
                            mp.setDataSource(playlist.get(0));
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        songIndex = 0;
                        fragmentOptions.updateUI(path);
                        fragmentPlaylist.updateUI(playlist);
                        fragmentPlayer.updateUI(playlist);
                    }
                }
                break;

        }
    }

    @Override
    public void play() {
        fragmentPlayer.updatePlayButton(mp.isPlaying());
        if (mp.isPlaying()) {
            mp.pause();
        } else {
            mp.start();
        }

    }

    @Override
    public void next() {
        if (isShuffle) {
            Random rnd = new Random();
            songIndex = rnd.nextInt((playlist.size() - 1) - 1);
        } else if (songIndex < playlist.size() - 1) {
            songIndex++;
        } else {
            songIndex = 0;
        }

        if (songIndex == 0 && !isRepeat) {
            songIndex = playlist.size() - 1;
            Toast.makeText(this, R.string.no_repeat_end, Toast.LENGTH_SHORT).show();

        } else {
            playSong(songIndex);
        }
    }

    @Override
    public void previous() {
        if (isShuffle) {
            Random rnd = new Random();
            songIndex = rnd.nextInt((playlist.size() - 1) - 1);
        } else if (songIndex > 0) {
            songIndex--;
        } else {
            songIndex = playlist.size() - 1;
        }
        if (songIndex == playlist.size() - 1 && !isRepeat) {
            songIndex = 0;
            Toast.makeText(this, R.string.no_repeat_start, Toast.LENGTH_SHORT).show();

        } else {
            playSong(songIndex);
        }
    }

    @Override
    public void repeat() {
        fragmentPlayer.updateRepeatButton(isRepeat);
        if (isRepeat) {
            isRepeat = false;
        } else {
            isRepeat = true;
            isShuffle = false;
        }

    }

    @Override
    public void shuffle() {
        fragmentPlayer.updateShuffleButton(isShuffle);
        if (isShuffle) {
            isShuffle = false;
        } else {
            isShuffle = true;

            isRepeat = false;

        }
    }

    @Override
    public void update() {
        fragmentPlayer.timerUpdate(mp.getDuration(), mp.getCurrentPosition());
    }

    @Override
    public void seek(int progress) {
        mp.seekTo(Utilities.progressToTimer(progress, mp.getDuration()));
    }

    @Override
    public void startSong(int position) {
        songIndex = position;
        playSong(songIndex);


    }

    public void playSong(int songIndex) {
        try {
            fragmentPlayer.updatePlayButton(mp.isPlaying());
            mp.reset();
            mp.setDataSource(playlist.get(songIndex));
            mp.prepare();
            mp.start();
            fragmentPlaylist.updateUI(songIndex);
            fragmentPlayer.updateUI(songIndex);

        } catch (IllegalArgumentException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!isRepeat) {
            if (songIndex < playlist.size() - 1) {
                songIndex++;

            } else {
                songIndex = 0;

            }
        } else if (isShuffle) {
            Random rnd = new Random();
            songIndex = rnd.nextInt((playlist.size() - 1) - 1);
        }
        playSong(songIndex);
    }


    private static class PlayerPagerAdapter extends FragmentPagerAdapter {

        ArrayList<String> playlist;
        int songindex;
        String path;
        FragmentPlayer fragmentPlayer;
        FragmentPlaylist fragmentPlaylist;
        FragmentOptions fragmentOptions;

        PlayerPagerAdapter(FragmentManager fm, ArrayList<String> playlist, int songIndex,
                           FragmentPlayer fragmentPlayer, FragmentPlaylist fragmentPlaylist,
                           FragmentOptions fragmentOptions, String path) {
            super(fm);
            this.playlist = playlist;
            this.songindex = songIndex;
            this.fragmentPlayer = fragmentPlayer;
            this.fragmentPlaylist = fragmentPlaylist;
            this.fragmentOptions = fragmentOptions;
            this.path = path;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putStringArrayList("playlist", playlist);

            switch (position) {
                case 0:
                    args.putString("path",path);
                    fragmentOptions.setArguments(args);
                    return fragmentOptions;

                case 1:
                    fragmentPlayer.setArguments(args);
                    return fragmentPlayer;

                default:
                    args.putInt("index", songindex);
                    fragmentPlaylist.setArguments(args);
                    return fragmentPlaylist;

            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
