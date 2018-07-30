package com.example.gbor.mp3player;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.gbor.mp3player.R.id.pager;


public class MainPlayerActivity extends FragmentActivity implements
        PlayerFragment.OnPlayerFragmentInteractionListener,
        PlaylistFragment.OnPlaylistFragmentInteractionListener,
        MediaPlayer.OnCompletionListener,
        OptionsFragment.OnOptionsFragmentInteractionListener,
        PlaylistDialogFragment.OnPlaylistDialogFragmentInteractionListener,
        LoaderCallbacks<Cursor>{

    private static final int FOLDER_CHOOSING_REQUEST = 1;
    private static final int ALL_SONGS = 1;

    private static final String SETTINGS_FILE = "com.example.gbor.mp3player.Settings";
    private static final String PLAYLIST_FILE = "com.example.gbor.mp3player.Playlist";


    private MediaPlayer mMediaPlayer;
    private List<Integer> mUsedIDs = new ArrayList<>();

    private final PlayerFragment mPlayerFragment = new PlayerFragment();
    private final PlaylistFragment mPlaylistFragment = new PlaylistFragment();
    private final OptionsFragment mOptionsFragment = new OptionsFragment();

    private SharedPreferences mSettings;
    private SharedPreferences mPlaylistFile;

    private int mSongIndex;
    private boolean mIsShuffle = false;
    private boolean mIsRepeat = false;
    private boolean mIsSwitching = false;
    private String mPath;
    private PlayerPagerAdapter mPagerAdapter;
    private Cursor mCursor;
    private ViewPager mViewPager;
    private int mPlaylistID = ALL_SONGS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        mPlaylistFile = getSharedPreferences(PLAYLIST_FILE, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main_player);
        mSongIndex = 0;
        mPath = mSettings.getString("path",Environment.getExternalStorageDirectory().toString());
        mViewPager = findViewById(pager);
        mPagerAdapter = new PlayerPagerAdapter(getSupportFragmentManager(),
                mSongIndex, mPlayerFragment, mPlaylistFragment, mOptionsFragment, mPath);
        getSupportLoaderManager().initLoader(ALL_SONGS,null,this);
        mUsedIDs.add(ALL_SONGS);

    }

    @Override
    public void optionOperations(int position) {
        if(position==0){
            Intent intent = new Intent(this,DirectoryChooserActivity.class);
            intent.putExtra("path", mPath);
            startActivityForResult(intent,FOLDER_CHOOSING_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case FOLDER_CHOOSING_REQUEST:
                if(resultCode==RESULT_OK){
                    mPath =data.getDataString();
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("path", mPath);
                    editor.apply();
                    mIsSwitching =true;
                    mMediaPlayer.stop();
                    getSupportLoaderManager().restartLoader(ALL_SONGS,null,this);
                    mSongIndex = 0;
                    mOptionsFragment.updateUI(mPath);
                }

                break;

        }
    }

    @Override
    public void playButton() {
        mPlayerFragment.updatePlayButton(mMediaPlayer.isPlaying());
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }

    }

    @Override
    public void nextButton() {
        if (mIsShuffle) {
            Random rnd = new Random();
            mSongIndex = rnd.nextInt((mCursor.getCount() - 1) - 1);
        } else if (mSongIndex < mCursor.getCount() - 1) {
            mSongIndex++;
        } else {
            mSongIndex = 0;
        }

        if (mSongIndex == 0 && !mIsRepeat) {
            mSongIndex = mCursor.getCount() - 1;
            Toast.makeText(this, R.string.no_repeat_end, Toast.LENGTH_SHORT).show();

        } else {
            playSong(mSongIndex);
        }
    }

    @Override
    public void previousButton() {
        if (mIsShuffle) {
            Random rnd = new Random();
            mSongIndex = rnd.nextInt((mCursor.getCount() - 1) - 1);
        } else if (mSongIndex > 0) {
            mSongIndex--;
        } else {
            mSongIndex = mCursor.getCount() - 1;
        }
        if (mSongIndex == mCursor.getCount() - 1 && !mIsRepeat) {
            mSongIndex = 0;
            Toast.makeText(this, R.string.no_repeat_start, Toast.LENGTH_SHORT).show();

        } else {
            playSong(mSongIndex);
        }
    }

    @Override
    public void repeatButton() {
        mPlayerFragment.updateRepeatButton(mIsRepeat);
        if (mIsRepeat) {
            mIsRepeat = false;
        } else {
            mIsRepeat = true;
            mIsShuffle = false;
        }

    }

    @Override
    public void shuffleButton() {
        mPlayerFragment.updateShuffleButton(mIsShuffle);
        if (mIsShuffle) {
            mIsShuffle = false;
        } else {
            mIsShuffle = true;

            mIsRepeat = false;

        }
    }

    @Override
    public void updateButton() {
        if (mCursor.getCount()==0)
            mPlayerFragment.timerUpdate(0, 0);
        else
            mPlayerFragment.timerUpdate(mMediaPlayer.getDuration(), mMediaPlayer.getCurrentPosition());
    }

    @Override
    public void seekButtonMovement(int progress) {
        mMediaPlayer.seekTo(Utilities.progressToTimer(progress, mMediaPlayer.getDuration()));
    }

    @Override
    public void startSelectedSong(int position) {
        mSongIndex = position;
        playSong(mSongIndex);


    }

    private void playSong(int songIndex) {
        try {
            mPlayerFragment.updatePlayButton(mMediaPlayer.isPlaying());
            mMediaPlayer.reset();
            mCursor.moveToPosition(songIndex);
            mMediaPlayer.setDataSource(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mPlaylistFragment.updateUI(songIndex);
            mPlayerFragment.updateUI(songIndex);
        } catch (IllegalArgumentException | IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void longClick(int positon) {
        Toast.makeText(this,String.valueOf(positon),Toast.LENGTH_LONG).show();
    }

    private void initialize(){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        try {
            if(mCursor !=null && mCursor.getCount()!=0){
                mCursor.moveToFirst();
                mMediaPlayer.setDataSource(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                mMediaPlayer.prepare();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @NonNull
    private String playlistDeletionSelection(List<String> names){
        StringBuilder buffer = new StringBuilder();
        buffer.append(MediaStore.Audio.Playlists._ID).append(" IN (");
        for (String name: names) {
            buffer.append(mPlaylistFile.getInt(name,-1)).append(", ");
            if (mPlaylistID == mPlaylistFile.getInt(name,-1)) {
                getSupportLoaderManager().restartLoader(ALL_SONGS, null, this);
                mPlaylistFragment.changeName(getString(R.string.all_songs));
            }
        }
        buffer.delete(buffer.length()-2,buffer.length());
        buffer.append(")");
        return buffer.toString();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!mIsRepeat) {
            if (mSongIndex < mCursor.getCount() - 1) {
                mSongIndex++;

            } else {
                mSongIndex = 0;

            }
        } else if (mIsShuffle) {
            Random rnd = new Random();
            mSongIndex = rnd.nextInt((mCursor.getCount() - 1) - 1);
        }

        if(mIsSwitching)
            mIsSwitching =false;
        else
            playSong(mSongIndex);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ALL_SONGS) {
            String[] songProj = {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATA
            };

            String songSelect = MediaStore.Audio.Media.DATA + " like ?";
            String[] selectArgs = new String[]{mPath + "%"};
            return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songProj,
                    songSelect,
                    selectArgs,
                    MediaStore.Audio.Media.ARTIST);
        }
        String[] songProj = {MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Playlists.Members.DATA,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.ALBUM,
                MediaStore.Audio.Playlists.Members.DATA
        };

        return new CursorLoader(this, MediaStore.Audio.Playlists.Members.getContentUri("external",id),
                songProj,
                null,
                null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Cursor cursor) {
        this.mCursor =cursor;
        initialize();
        if(mViewPager.getAdapter()==null) {
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(2);
        }
        if(mPagerAdapter !=null && cursor!=null){
            mPagerAdapter.changeCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {

    }

    @Override
    public void playlistRemoveButton(List<String> names) {
        SharedPreferences.Editor editor = mPlaylistFile.edit();
        for (String name: names) {
            editor.remove(name);
        }
        getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,playlistDeletionSelection(names),null);
        editor.apply();

    }

    @Override
    public void playlistAddButton(String name) {
        SharedPreferences.Editor editor = mPlaylistFile.edit();
        ContentValues contentValues = new ContentValues();
        int playlistId = -1;
        contentValues.put(MediaStore.Audio.Playlists.NAME, name);
        contentValues.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        contentValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        Uri uri = getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri != null) {
            String[] path= uri.getPath().split("/");
            playlistId = Integer.parseInt(path[path.length - 1]);
        }
        uri = MediaStore.Audio.Playlists.Members.getContentUri("external",playlistId);
        long i = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));
        contentValues.clear();
        contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,i);
        contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,0);
        getContentResolver().insert(uri,contentValues);
        editor.putInt(name, playlistId);
        editor.apply();

    }

    @Override
    public void playlistSelection(String name) {
        mPlaylistFragment.changeName(name);
        mPlaylistID = mPlaylistFile.getInt(name,ALL_SONGS);
        if (mUsedIDs.contains(mPlaylistID))
            getSupportLoaderManager().restartLoader(mPlaylistID,null,this);
        else {
            getSupportLoaderManager().initLoader(mPlaylistID, null, this);
            mUsedIDs.add(mPlaylistID);
        }
    }

    @Override
    public void addSelectedSongs(String name) {

    }

    private static class PlayerPagerAdapter extends FragmentPagerAdapter {

        final int mSongIndex;
        final String mPath;
        final PlayerFragment mPlayerFragment;
        final PlaylistFragment mPlaylistFragment;
        final OptionsFragment mOptionsFragment;
        Cursor mCursor;

        PlayerPagerAdapter(FragmentManager fm, int songIndex,
                           PlayerFragment playerFragment, PlaylistFragment playlistFragment,
                           OptionsFragment optionsFragment, String path) {
            super(fm);
            mSongIndex = songIndex;
            mPlayerFragment = playerFragment;
            mPlaylistFragment = playlistFragment;
            mOptionsFragment = optionsFragment;
            mPath = path;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();

            switch (position) {
                case 0:
                    args.putString("path",mPath);
                    mOptionsFragment.setArguments(args);
                    return mOptionsFragment;

                case 1:
                    args.putInt("index", mSongIndex);
                    mPlayerFragment.setArguments(args);
                    return mPlayerFragment;

                default:
                    args.putInt("index", mSongIndex);
                    mPlaylistFragment.setArguments(args);
                    return mPlaylistFragment;

            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        void changeCursor(Cursor newCursor) {
            mCursor = newCursor;
            if (newCursor != null) {
                mPlayerFragment.changeCursor(mCursor);
                mPlaylistFragment.changeCursor(mCursor);
            }
        }
    }

}
