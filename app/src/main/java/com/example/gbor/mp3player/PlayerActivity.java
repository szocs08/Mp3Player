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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends FragmentActivity implements
        PlayerFragment.OnFragmentInteractionListener,
        PlayListFragment.OnFragmentInteractionListener,
        MediaPlayer.OnCompletionListener
{

    private MediaPlayer mp;

    private PlayerFragment playerFragment = new PlayerFragment();
    private PlayListFragment playListFragment = new PlayListFragment();

    private SongsManager songsManager;
    private int songIndex;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);




        mp = new MediaPlayer();
        songsManager = new SongsManager();

        mp.setOnCompletionListener(this);

        PlayerPagerAdapter pagerAdapter = new PlayerPagerAdapter(getSupportFragmentManager(),
                songsManager.getPlaylist(),songIndex,playerFragment,playListFragment);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);


    }

    @Override
    public void play() {
        playerFragment.updatePlayButton(mp.isPlaying());
        if(mp.isPlaying()){
            mp.pause();
        }else{
            mp.start();
        }

    }

    @Override
    public void next() {
        if(songIndex < songsManager.getPlaylist().size()-1){
            songIndex++;
            playerFragment.updateUI(songIndex);
        }else{
            songIndex =0;
            playerFragment.updateUI(songIndex);
        }
    }

    @Override
    public void previous() {
        if(songIndex > 0){
            songIndex--;
            playerFragment.updateUI(songIndex);
        }else{
            songIndex =songsManager.getPlaylist().size()-1;
            playerFragment.updateUI(songIndex);
        }
    }

    @Override
    public void repeat() {
        playerFragment.updateRepeatButton(isRepeat);
        if(isRepeat){
            isRepeat = false;
        }else{
            isRepeat = true;
            isShuffle = false;
        }

    }

    @Override
    public void shuffle() {
        playerFragment.updateShuffleButton(isShuffle);
        if(isShuffle){
            isShuffle = false;
        }else{
            isShuffle = true;

            isRepeat = false;

        }
    }

    @Override
    public void update() {
        playerFragment.timerUpdate(mp.getDuration(),mp.getCurrentPosition());
    }

    @Override
    public void startSong(int position) {
        playSong(position);


    }

    public void playSong(int songindex) {
        try{
            mp.reset();
            mp.setDataSource(songsManager.getPlaylist().get(songindex));
            mp.prepare();
            mp.start();
            //playerFragment.updateUI(songindex);
        }catch (IllegalArgumentException|IOException |IllegalStateException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(isRepeat){
            songIndex = songIndex;
        } else if(isShuffle){
            Random rnd = new Random();
            songIndex = rnd.nextInt((songsManager.getPlaylist().size() - 1) - 1);
        } else {
            if(songIndex < songsManager.getPlaylist().size()-1){
                songIndex++;

            }else{
                songIndex =0;

            }
        }
        playSong(songIndex);
    }


    public static class PlayerPagerAdapter extends FragmentPagerAdapter{

        ArrayList<String> playlist;
        int songindex;

        public PlayerPagerAdapter(FragmentManager fm, ArrayList<String> playlist, int songIndex,
                                  PlayerFragment playerFragment,PlayListFragment playListFragment){
            super(fm);
            this.playlist = playlist;
            this.songindex = songIndex;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putStringArrayList("playlist",playlist);
            args.putInt("index",songindex);
            switch (position){
                case 0:
                    PlayerFragment playerFragment = new PlayerFragment();
                    playerFragment.setArguments(args);
                    return playerFragment;

                default:
                    PlayListFragment playListFragment = new PlayListFragment();
                    playListFragment.setArguments(args);
                    return new PlayListFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
