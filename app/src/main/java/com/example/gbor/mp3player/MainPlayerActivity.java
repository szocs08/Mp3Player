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
import java.util.Arrays;
import java.util.Collections;
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
    private static final int ALL_SONGS = -1;

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
    private boolean mIsRemoved = false;
    private String mPath;
    private PlayerPagerAdapter mPagerAdapter;
    private Cursor mCursor;
    private ViewPager mViewPager;
    private int mPlaylistID = ALL_SONGS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_player);
        mSettings = getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        mPlaylistFile = getSharedPreferences(PLAYLIST_FILE, Context.MODE_PRIVATE);
        mPath = mSettings.getString("path",Environment.getExternalStorageDirectory().toString());
        mViewPager = findViewById(pager);
        mPagerAdapter = new PlayerPagerAdapter(getSupportFragmentManager(),
                mSongIndex, mPlayerFragment, mPlaylistFragment, mOptionsFragment, mPath);
        getSupportLoaderManager().initLoader(ALL_SONGS,null,this);

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
    public void updateProgressBar() {
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

    private void initialize(){
        mSongIndex = 0;
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }else {
            mMediaPlayer.release();
            mMediaPlayer = new MediaPlayer();
        }
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
            if (mSongIndex == mPlaylistFile.getInt(name,-1)) {
                getSupportLoaderManager().restartLoader(ALL_SONGS, null, this);
                mPlaylistFragment.changeName(getString(R.string.all_songs));
            }
        }
        buffer.delete(buffer.length()-2,buffer.length());
        buffer.append(")");
        return buffer.toString();
    }

    @NonNull
    private String songDeletionSelection(List<Integer> positions){
        StringBuilder buffer = new StringBuilder();
        buffer.append(MediaStore.Audio.Playlists.Members._ID).append(" IN (");
        for (int position: positions) {
            if (mSongIndex > position)
                mSongIndex--;
            mCursor.moveToPosition(position);
            buffer.append(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID))).append(", ");
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
        mUsedIDs.add(id);
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
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Cursor cursor) {
        if (!mIsRemoved || loader.getId() == ALL_SONGS) {
            this.mCursor = cursor;
            if (mMediaPlayer == null || mIsSwitching) {
                initialize();
            }
            if(mViewPager.getAdapter()==null) {
                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.setCurrentItem(0);
            }
            if(mPagerAdapter !=null && cursor!=null){
                mPagerAdapter.changeCursor(cursor);
                if (mIsSwitching) {
                    mPlayerFragment.updateUI(0);
                    mIsSwitching = false;
                }else{
                    mPlaylistFragment.updateUI(mSongIndex);
                }

            }
        }else {
            mIsRemoved=false;
        }


    }

    @Override
    public void playlistRemoveButton(List<String> names) {
        SharedPreferences.Editor editor = mPlaylistFile.edit();
        int id;
        for (String name: names) {
            id = mPlaylistFile.getInt(name,-2);
            if (id == mPlaylistID)
                mIsRemoved = true;
            mUsedIDs.remove((Integer) id);
            editor.remove(name);
        }
        if (mIsRemoved)
            playlistSelection(getString(R.string.all_songs));
        getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,playlistDeletionSelection(names),null);
        editor.apply();

    }

    @Override
    public String playlistAddButton(String name) {
        SharedPreferences.Editor editor = mPlaylistFile.edit();
        if(mPlaylistFile.contains(name)){
            String newName;
            for (int i = 1;; i++) {
                newName = name + "(" + i + ")";
                if(!mPlaylistFile.contains(newName)) {
                    name = newName;
                    break;
                }
            }
        }
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
        editor.putInt(name, playlistId);
        editor.apply();
        return name;
    }

    @Override
    public void playlistSelection(String name) {
        mIsSwitching = true;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mPlaylistFragment.changeName(name);
        mPlaylistID = mPlaylistFile.getInt(name,ALL_SONGS);
        if (mUsedIDs.contains(mPlaylistID))
            try {
                getSupportLoaderManager().restartLoader(mPlaylistID,null,this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        else {
            getSupportLoaderManager().initLoader(mPlaylistID, null, this);
        }
    }

    @Override
    public void addSelectedSongs(String name) {
        ContentValues contentValues = new ContentValues();
        List<Integer> positions = new ArrayList<>(mPlaylistFragment.getSelectedSongs());
        Collections.sort(positions);
        mPlaylistFragment.resetPlaylistUI();
        int id = mPlaylistFile.getInt(name,-1);
        int order = 1;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",id);
        Cursor cursor = getContentResolver().query(uri,null,null,null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER + " DESC ");
        if (cursor != null){
            if (cursor.moveToFirst()){
                order = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER)) + 1;
            }
        }
        for(int position : positions){
            mCursor.moveToPosition(position);
            contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,order++);
            getContentResolver().insert(uri,contentValues);
        }
        if (cursor != null){
            cursor.close();
        }
        Toast.makeText(this,"Added "+String.valueOf(positions.size())+" song(s) to "+ name,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeSelectedSongs(List<Integer> positions) {
        if(mPlaylistID != -1){
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",mPlaylistID);
            if (positions.contains(mSongIndex))
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mSongIndex = 0;
                }
            getContentResolver().delete(uri,songDeletionSelection(positions),null);
            mPlaylistFragment.resetPlaylistUI();
        }

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
