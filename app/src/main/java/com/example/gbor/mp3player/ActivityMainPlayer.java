package com.example.gbor.mp3player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.example.gbor.mp3player.R.id.pager;

public class ActivityMainPlayer extends FragmentActivity implements
        FragmentPlayer.OnPlayerFragmentInteractionListener,
        FragmentPlaylist.OnListFragmentInteractionListener,
        MediaPlayer.OnCompletionListener,
        FragmentOptions.OnOptionsFragmentInteractionListener,
        LoaderCallbacks<Cursor>{

    private static final int FOLDER_CHOOSING_REQUEST = 1;
    private static final int SONG_QUERY = 1;

    private static final String SETTINGS_FILE = "com.example.gbor.mp3player.settings";

    private MediaPlayer mp;

    private FragmentPlayer fragmentPlayer = new FragmentPlayer();
    private FragmentPlaylist fragmentPlaylist = new FragmentPlaylist();
    private FragmentOptions fragmentOptions = new FragmentOptions();

    private SharedPreferences settings;
    private int songIndex;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<String> playlist;
    private String path ;
    private PlayerPagerAdapter pagerAdapter;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        setContentView(R.layout.main_layout);
        songIndex = 0;
        path=settings.getString("path",Environment.getExternalStorageDirectory().toString());
        ViewPager viewPager = findViewById(pager);
        getSupportLoaderManager().initLoader(SONG_QUERY,null,this);
        initialize();
        pagerAdapter = new PlayerPagerAdapter(getSupportFragmentManager(),
                playlist, songIndex, fragmentPlayer, fragmentPlaylist, fragmentOptions, path);
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
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("path",path);
                    editor.apply();
                    mp = new MediaPlayer();
                    playlist = SongManager.getPlaylist(this);
                    getSupportLoaderManager().restartLoader(SONG_QUERY,null,this);
                    mp.setOnCompletionListener(this);
                    initialize();
                    songIndex = 0;
                    fragmentOptions.updateUI(path);
                    fragmentPlaylist.updateUI(playlist);
                    fragmentPlayer.updateUI(playlist);
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
        if (playlist.isEmpty())
            fragmentPlayer.timerUpdate(0, 0);
        else
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

    private void playSong(int songIndex) {
        try {
            fragmentPlayer.updatePlayButton(mp.isPlaying());
            mp.reset();
            cursor.moveToPosition(songIndex);
            String asd=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            mp.setDataSource(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            mp.prepare();
            mp.start();
            fragmentPlaylist.updateUI(songIndex);
            fragmentPlayer.updateUI(songIndex);

        } catch (IllegalArgumentException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void initialize(){
        playlist = SongManager.getPlaylist(this);
        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        if(!playlist.isEmpty()){
            try {
                if(cursor!=null){
                    int asd = cursor.getCount();
                    cursor.moveToFirst();
                    mp.setDataSource(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    mp.prepare();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
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

    @NonNull
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);

        String[] songProj = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String songSelect = MediaStore.Audio.Media.DATA + " like ?";
        String[] selectArgs = new String[]{settings.getString("path", Environment.getExternalStorageDirectory().toString()) + "%"};
        return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songProj,
                songSelect,
                selectArgs,
                MediaStore.Audio.Media.DISPLAY_NAME);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Cursor cursor) {
        this.cursor=cursor;
        initialize();
        if(pagerAdapter!=null && cursor!=null)
            pagerAdapter.swapCursor(cursor);
        else
            Log.v("asdasdasd","OnLoadFinished: mAdapter is null");
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    private static class PlayerPagerAdapter extends FragmentPagerAdapter {

        ArrayList<String> playlist;
        int songIndex;
        String path;
        FragmentPlayer fragmentPlayer;
        FragmentPlaylist fragmentPlaylist;
        FragmentOptions fragmentOptions;
        Cursor cursor;

        PlayerPagerAdapter(FragmentManager fm, ArrayList<String> playlist, int songIndex,
                           FragmentPlayer fragmentPlayer, FragmentPlaylist fragmentPlaylist,
                           FragmentOptions fragmentOptions, String path) {
            super(fm);
            this.playlist = playlist;
            this.songIndex = songIndex;
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
                    args.putInt("index", songIndex);
                    fragmentPlaylist.setArguments(args);
                    return fragmentPlaylist;

            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        Cursor swapCursor(Cursor newCursor) {
            if (newCursor == cursor) {
                return null;
            }
            Cursor oldCursor = cursor;
            cursor = newCursor;
            if (newCursor != null) {
                fragmentPlayer.swapCursor(cursor);
            }
            return oldCursor;
        }
    }

}
