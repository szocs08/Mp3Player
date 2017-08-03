/*

fragment
facebook
options
playlist save/load

 */

package com.example.gbor.mp3player;

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

public class PlayerActivity extends FragmentActivity implements
        PlayerFragment.OnFragmentInteractionListener,
        PlayListFragment.OnFragmentInteractionListener,
        MediaPlayer.OnCompletionListener,
    OptionsFragment.OnOptionsFragmentInteractionListener{

    private MediaPlayer mp;

    private PlayerFragment playerFragment = new PlayerFragment();
    private PlayListFragment playListFragment = new PlayListFragment();
    private OptionsFragment optionsFragment = new OptionsFragment();

    private int songIndex;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<String> playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        songIndex = 0;


        mp = new MediaPlayer();
        SongsManager songsManager = new SongsManager(getString(R.string.default_folder));
        playlist = songsManager.getPlaylist();
        mp.setOnCompletionListener(this);
        try {
            mp.setDataSource(playlist.get(0));
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PlayerPagerAdapter pagerAdapter = new PlayerPagerAdapter(getSupportFragmentManager(),
                playlist, songIndex, playerFragment, playListFragment,optionsFragment);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        //viewPager.setCurrentItem(1);


    }

    @Override
    public void asd(int position) {

    }

    @Override
    public void play() {
        playerFragment.updatePlayButton(mp.isPlaying());
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
        playerFragment.updateRepeatButton(isRepeat);
        if (isRepeat) {
            isRepeat = false;
        } else {
            isRepeat = true;
            isShuffle = false;
        }

    }

    @Override
    public void shuffle() {
        playerFragment.updateShuffleButton(isShuffle);
        if (isShuffle) {
            isShuffle = false;
        } else {
            isShuffle = true;

            isRepeat = false;

        }
    }

    @Override
    public void update() {
        playerFragment.timerUpdate(mp.getDuration(), mp.getCurrentPosition());
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

    public void playSong(int songindex) {
        try {
            mp.reset();
            mp.setDataSource(playlist.get(songindex));
            mp.prepare();
            mp.start();
            playerFragment.updateUI(songindex);
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
        PlayerFragment playerFragment;
        PlayListFragment playListFragment;
        OptionsFragment optionsFragment;

        public PlayerPagerAdapter(FragmentManager fm, ArrayList<String> playlist, int songIndex,
                                  PlayerFragment playerFragment, PlayListFragment playListFragment,
                                  OptionsFragment optionsFragment) {
            super(fm);
            this.playlist = playlist;
            this.songindex = songIndex;
            this.playerFragment = playerFragment;
            this.playListFragment = playListFragment;
            this.optionsFragment = optionsFragment;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putStringArrayList("playlist", playlist);

            switch (position) {
                case 1:
                    optionsFragment.setArguments(args);
                    return optionsFragment;

                case 0:
                    playerFragment.setArguments(args);
                    return playerFragment;

                default:
                    args.putInt("index", songindex);
                    playListFragment.setArguments(args);
                    return playListFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
