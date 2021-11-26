package hu.application.gbor.mp3player;

import static com.example.gbor.mp3player.R.id.action_context_bar;
import static com.example.gbor.mp3player.R.id.pager;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gbor.mp3player.R;

import java.util.ArrayList;
import java.util.List;


public class MainPlayerActivity extends FragmentActivity implements
        PlayerFragment.OnPlayerFragmentInteractionListener,
        PlaylistFragment.OnPlaylistFragmentInteractionListener,
        MediaPlayer.OnCompletionListener,
        OptionsFragment.OnOptionsFragmentInteractionListener,
        PlaylistDialogFragment.OnPlaylistDialogFragmentInteractionListener{

    //Új mappa választásának kódja
    private static final int FOLDER_CHOOSING_REQUEST = 1;
    //Az összes dalt tartalmazó lista kódja
    private static final int ALL_SONGS = -1;
    //Az olvasási engedély kérés kódja
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 18;

    //A beállításokat tartalmazó fájl neve
    private static final String SETTINGS_FILE = "hu.application.gbor.mp3player.Settings";
    //A lejátszási listákat tartalmazó fájl neve
    private static final String PLAYLIST_FILE = "hu.application.gbor.mp3player.Playlist";

    //A lejátszást végző mediaplayer
    private MediaPlayer mMediaPlayer;
    //A már felhasznált lejátszási listák kódját tartalmazó lista
    private final List<Integer> mUsedIDs = new ArrayList<>();

//    //A lejátszó felhasználói felülete
//    private PlayerFragment mPlayerFragment;
//    //A lejátszási lista felhasználói felülete
//    private PlaylistFragment mPlaylistFragment;
//    //A beállítások felhasználói felülete
//    private OptionsFragment mOptionsFragment;

    //A beállítások tartalmazó fájl
    private SharedPreferences mSettings;
    //A lejátszási listákat tartalmazó fájl
    private SharedPreferences mPlaylistFile;

    //A lejátszott zene sorszáma
    private int mSongIndex;
    //A lista kevert lejátszása
    private boolean mIsShuffle = false;
    //A lista ismétlődő lejátszása
    private boolean mIsRepeat = false;
    //A lejátszási lista váltása
    private boolean mIsSwitching = false;
    //A lejátszási lista törölve lett
    private boolean mIsRemoved = false;
    //Az aktuálisan játszott zene elérési útja
    private String mPath;
    //A lejátszó felhasználói felületének megjelenítésére
    private PlayerPagerAdapter mPagerAdapter;
    //A program fő felületének megjelenítésére szolgál
    private ViewPager2 mViewPager;
    //Az aktuális lejátszási lista kódja
    private int mPlaylistID = ALL_SONGS;

    /**
     * A program létrejöttekor futó metódus
     * @param savedInstanceState a régebbi futtatását tartalmazza
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_player);
        mSettings = getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        mPlaylistFile = getSharedPreferences(PLAYLIST_FILE, Context.MODE_PRIVATE);
        mPath = mSettings.getString("path",Environment.getExternalStorageDirectory().toString());
        mViewPager = findViewById(pager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
        //A UI megjelenítéséhez szükséges adatok
        PlayerViewModel mPlayerViewModel;
        mPlayerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
//        mPlayerViewModel.getPlaylist().observe(this, playlist -> {
//            mPagerAdapter = new PlayerPagerAdapter(this,playlist);
//            mViewPager.setAdapter(mPagerAdapter);
//            mViewPager.setCurrentItem(2);
//        });
        mPagerAdapter = new PlayerPagerAdapter(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(2);


    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    /**
     * A beállításokat végző metódus
     * @param position A kattintot lista elem pozíciója
     */
    @Override
    public void optionOperations(int position) {
        if(position==0){
            Intent intent = new Intent(this,DirectoryChooserActivity.class);
            intent.putExtra("path", mPath);
            startActivityForResult(intent,FOLDER_CHOOSING_REQUEST);
        }

    }

    /**
     * Az indított activity bezárása esetén lefutó metódus
     * @param requestCode Az activity indításának célját jelzi
     * @param resultCode A bezárás módját adja meg
     * @param data Az activity által vissza adott adat
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FOLDER_CHOOSING_REQUEST) {
            if (resultCode == RESULT_OK) {
                mPath = data.getDataString();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("path", mPath);
                editor.apply();
                mIsSwitching = true;
                if (mMediaPlayer != null)
                    mMediaPlayer.stop();
                mSongIndex = 0;
//                mOptionsFragment.updateUI(mPath);
            }
        }
    }

    /**
     * A play gomb megnyomása esetén lefutó metódus
     */
    @Override
    public void playButton() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
//        mPlayerFragment.updatePlayButton(mMediaPlayer.isPlaying());

    }

    /**
     * A next gomb megnyomása esetén lefutó metódus
     */
    @Override
    public void nextButton() {
//        if (mIsShuffle) {
//            Random rnd = new Random();
//            mSongIndex = rnd.nextInt((mCursor.getCount() - 1) - 1);
//        } else if (mSongIndex < mCursor.getCount() - 1) {
//            mSongIndex++;
//        } else {
//            mSongIndex = 0;
//        }
//
//        if (mSongIndex == 0 && !mIsRepeat) {
//            mSongIndex = mCursor.getCount() - 1;
//            Toast.makeText(this, R.string.no_repeat_end, Toast.LENGTH_SHORT).show();
//
//        } else {
//            playSong(mSongIndex);
//        }
    }

    /**
     * A previous gomb megnyomása esetén lefutó metódus
     */
    @Override
    public void previousButton() {
//        if (mIsShuffle) {
//            Random rnd = new Random();
//            mSongIndex = rnd.nextInt((mCursor.getCount() - 1) - 1);
//        } else if (mSongIndex > 0) {
//            mSongIndex--;
//        } else {
//            mSongIndex = mCursor.getCount() - 1;
//        }
//        if (mSongIndex == mCursor.getCount() - 1 && !mIsRepeat) {
//            mSongIndex = 0;
//            Toast.makeText(this, R.string.no_repeat_start, Toast.LENGTH_SHORT).show();
//
//        } else {
//            playSong(mSongIndex);
//        }
    }

    /**
     * A repeat gomb megnyomása esetén lefutó metódus
     */
    @Override
    public void repeatButton() {
//        mPlayerFragment.updateRepeatButton(mIsRepeat);
        if (mIsRepeat) {
            mIsRepeat = false;
        } else {
            mIsRepeat = true;
            mIsShuffle = false;
        }

    }

    /**
     * A shuffle gomb megnyomása esetén lefutó metódus
     */
    @Override
    public void shuffleButton() {
//        mPlayerFragment.updateShuffleButton(mIsShuffle);
        if (mIsShuffle) {
            mIsShuffle = false;
        } else {
            mIsShuffle = true;

            mIsRepeat = false;

        }
    }

    /**
     * A folyamat jelző frissítését végző metódus
     */
    @Override
    public void updateProgressBar() {
//        if (mCursor.getCount()==0)
//            mPlayerFragment.timerUpdate(0, 0);
//        else
//            mPlayerFragment.timerUpdate(mMediaPlayer.getDuration(), mMediaPlayer.getCurrentPosition());
    }

    /**
     * A zene tekerésért felelős metódus
     * @param progress az idő ahova ugrani szeretnénk
     */
    @Override
    public void seekButtonMovement(int progress) {
        mMediaPlayer.seekTo(Utilities.progressToTimer(progress, mMediaPlayer.getDuration()));
    }

    /**
     * A kapott pozíción lévő zene lejátszása lejátszási listáról
     * @param position a lejátszandó zene pozíciója
     */
    @Override
    public void startSelectedSong(int position) {
        mSongIndex = position;
        playSong(mSongIndex);


    }

    /**
     * A zene lejátszást elindító metódus
     * @param songIndex
     */
    private void playSong(int songIndex) {
//        try {
//            mPlayerFragment.updatePlayButton(mMediaPlayer.isPlaying());
//            mMediaPlayer.reset();
//            mCursor.moveToPosition(songIndex);
//            mMediaPlayer.setDataSource(mPlayerViewModel.getPlaylist().getValue().getSongs().get(0).getData());
//            mMediaPlayer.prepare();
//            mMediaPlayer.start();
//            mPlaylistFragment.updateUI(songIndex);
//            mPlayerFragment.updateUI(songIndex);
//        } catch (IllegalArgumentException | IOException | IllegalStateException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * A kezdeti beállítást végző metódus
     */
    private void initialie(){
        mSongIndex = 0;
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }else {
            mMediaPlayer.release();
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnCompletionListener(this);
//        try {
//            if(mCursor !=null && mCursor.getCount()!=0){
//                mCursor.moveToFirst();
//                mMediaPlayer.setDataSource(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
//                mMediaPlayer.prepare();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    void updateUI(){

    }

    /**
     * A lejátszási listák törlésére szolgáló kiválasztás létrehozása
     * @param names A törölni kívánt listák neveit tartalmazó lista
     * @return A törléshez használandó kiválasztás
     */
    @NonNull
    private String playlistDeletionSelection(List<String> names){
        StringBuilder buffer = new StringBuilder();
        buffer.append(MediaStore.Audio.Playlists._ID).append(" IN (");
//        for (String name: names) {
//            buffer.append(mPlaylistFile.getInt(name,-1)).append(", ");
//            if (mSongIndex == mPlaylistFile.getInt(name,-1)) {
//                getSupportLoaderManager().restartLoader(ALL_SONGS, null, this);
//                mPlaylistFragment.changeName(getString(R.string.all_songs));
//            }
//        }
        buffer.delete(buffer.length()-2,buffer.length());
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * A zenék törlésére szolgáló kiválasztás létrehozása
     * @param positions A törölni kívánt zenék pozícióját tartalmazó lista
     * @return A törléshez használandó kiválasztás
     */
    @NonNull
    private String songDeletionSelection(List<Integer> positions){
        StringBuilder buffer = new StringBuilder();
        buffer.append(MediaStore.Audio.Playlists.Members._ID).append(" IN (");
//        for (int position: positions) {
//            if (mSongIndex > position)
//                mSongIndex--;
//            mCursor.moveToPosition(position);
//            buffer.append(mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID))).append(", ");
//        }
        buffer.delete(buffer.length()-2,buffer.length());
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * A lejátszott zene végére érése esetén hívott metódus
     * @param mp A lejátszást végző mediaplayer
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
//        if (!mIsRepeat) {
//            if (mSongIndex < mCursor.getCount() - 1) {
//                mSongIndex++;
//
//            } else {
//                mSongIndex = 0;
//
//            }
//        } else if (mIsShuffle) {
//            Random rnd = new Random();
//            mSongIndex = rnd.nextInt((mCursor.getCount() - 1) - 1);
//        }

        if(mIsSwitching)
            mIsSwitching =false;
        else
            playSong(mSongIndex);
    }

//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        mUsedIDs.add(id);
//        if (id == ALL_SONGS) {
//            String[] songProj = {MediaStore.Audio.Media._ID,
//                    MediaStore.Audio.Media.DATA,
//                    MediaStore.Audio.Media.TITLE,
//                    MediaStore.Audio.Media.ARTIST,
//                    MediaStore.Audio.Media.ALBUM,
//                    MediaStore.Audio.Media.DATA
//            };
//
//            String songSelect = MediaStore.Audio.Media.DATA + " like ?";
//            String[] selectArgs = new String[]{mPath + "%"};
//
//            return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                    songProj,
//                    songSelect,
//                    selectArgs,
//                    MediaStore.Audio.Media.ARTIST);
//
//        }
//        String[] songProj = {MediaStore.Audio.Playlists.Members._ID,
//                MediaStore.Audio.Playlists.Members.DATA,
//                MediaStore.Audio.Playlists.Members.TITLE,
//                MediaStore.Audio.Playlists.Members.ARTIST,
//                MediaStore.Audio.Playlists.Members.ALBUM,
//                MediaStore.Audio.Playlists.Members.DATA,
//                MediaStore.Audio.Playlists.Members.PLAY_ORDER
//        };
//
//        return new CursorLoader(this, MediaStore.Audio.Playlists.Members.getContentUri("external",id),
//                songProj,
//                null,
//                null,
//                MediaStore.Audio.Playlists.Members.PLAY_ORDER);
//
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader loader, Cursor cursor) {
//        if (!mIsRemoved || loader.getId() == ALL_SONGS) {
//            this.mCursor = cursor;
//            if (mMediaPlayer == null || mIsSwitching) {
//                initialize();
//            }
//            if(mViewPager.getAdapter()==null) {
//                mViewPager.setCurrentItem(0);
//            }
//            if(mPagerAdapter !=null && cursor!=null){
//                mPagerAdapter.changeCursor(cursor);
//                if (mIsSwitching) {
//                    mPlayerFragment.updateUI(0);
//                    mPlayerFragment.updatePlayButton(mMediaPlayer.isPlaying());
//                    mIsSwitching = false;
//                }else{
//                    mPlayerFragment.updateUI(mSongIndex);
//                    mPlayerFragment.updatePlayButton(mMediaPlayer.isPlaying());
//                }
//
//            }
//        }else {
//            mIsRemoved=false;
//        }
//
//
//    }

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
//        mPlaylistFragment.changeName(name);
        mPlaylistID = mPlaylistFile.getInt(name,ALL_SONGS);
//        if (mUsedIDs.contains(mPlaylistID))
//            try {
//                getSupportLoaderManager().restartLoader(mPlaylistID,null,this);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        else {
//            getSupportLoaderManager().initLoader(mPlaylistID, null, this);
//        }
    }

    @Override
    public void addSelectedSongs(String name) {
        ContentValues contentValues = new ContentValues();
//        List<Integer> positions = new ArrayList<>(mPlaylistFragment.getSelectedSongs());
//        Collections.sort(positions);
//        mPlaylistFragment.resetPlaylistUI();
        int id = mPlaylistFile.getInt(name,-1);
        int order = 1;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",id);
        Cursor cursor = getContentResolver().query(uri,null,null,null,
                MediaStore.Audio.Playlists.Members.PLAY_ORDER + " DESC ");
//        if (cursor != null){
//            if (cursor.moveToFirst()){
//                order = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER)) + 1;
//            }
//        }
//        for(int position : positions){
//            mCursor.moveToPosition(position);
//            contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,
//                    mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
//            contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,order++);
//            getContentResolver().insert(uri,contentValues);
//        }
        if (cursor != null){
            cursor.close();
        }
//        Toast.makeText(this,"Added "+ positions.size() +" song(s) to "+ name,Toast.LENGTH_SHORT).show();
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
//            mPlaylistFragment.resetPlaylistUI();
        }

    }

    @Override
    public void removeSelectedSong(int position) {
//        if(mPlaylistID != -1){
//            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",mPlaylistID);
//            if (position == mSongIndex)
//                if (mMediaPlayer.isPlaying()) {
//                    mMediaPlayer.stop();
//                    mSongIndex = 0;
//                }
//            mCursor.moveToPosition(position);
//            int indexID = mCursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID);
//            int positionID = mCursor.getInt(indexID);
//            String whereString = MediaStore.Audio.Playlists.Members._ID + " = ?";
//            String[] whereArgs = new String[]{Integer.toString(positionID)};
//            getContentResolver().delete(uri,whereString,whereArgs);
//        }
    }

    @Override
    public void swapSongPositions(int from, int to) {
//        int indexPlayOrder = mCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.PLAY_ORDER);
//        int indexID = mCursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID);
//
//        mCursor.moveToPosition(from);
//        int fromPlayOrder = mCursor.getInt(indexPlayOrder);
//        int fromID = mCursor.getInt(indexID);
//
//        mCursor.moveToPosition(to);
//        int toPlayOrder = mCursor.getInt(indexPlayOrder);
//        int toID = mCursor.getInt(indexID);
//
//        ContentValues updateValues = new ContentValues();
//        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",mPlaylistID);
//        String whereString = MediaStore.Audio.Playlists.Members._ID + " = ?";
//
//        updateValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,fromPlayOrder);
//        String[] whereArgs = new String[]{Integer.toString(toID)};
//        int success = getContentResolver().update(uri,updateValues,whereString,whereArgs);
//
//        updateValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,toPlayOrder);
//        whereArgs = new String[]{Integer.toString(fromID)};
//        getContentResolver().update(uri, updateValues, whereString, whereArgs);

    }

    private static class PlayerPagerAdapter extends FragmentStateAdapter {

        int mSongIndex;
        String mPath;
        PlayerFragment mPlayerFragment;
        PlaylistFragment mPlaylistFragment;
        OptionsFragment mOptionsFragment;

        public PlayerPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int position) {
            Bundle args = new Bundle();

            switch (position) {
                case 0:
                    if (mOptionsFragment == null)
                        mOptionsFragment = new OptionsFragment();
                    args.putString("path",mPath);
                    mOptionsFragment.setArguments(args);
                    return mOptionsFragment;

                case 1:
                    if (mPlayerFragment == null)
                        mPlayerFragment = new PlayerFragment();
                    args.putInt("index", mSongIndex);
                    mPlayerFragment.setArguments(args);
                    return mPlayerFragment;

                default:
                    if (mPlaylistFragment == null)
                        mPlaylistFragment = new PlaylistFragment();
                    args.putInt("index", mSongIndex);
                    mPlaylistFragment.setArguments(args);
                    return mPlaylistFragment;

            }
        }


        @Override
        public int getItemCount() {
            return 3;
        }
    }

}
