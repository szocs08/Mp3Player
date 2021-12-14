package hu.application.gbor.mp3player;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerViewModel extends AndroidViewModel {

    private static final String PLAYLIST_FILE = "hu.application.gbor.mp3player.Playlist";

    private MutableLiveData<Playlist> playlist;
    private MutableLiveData<ArrayList<PlaylistFile>> playlistFileList;
    private final SharedPreferences playlistFile;

    public PlayerViewModel(Application application){
        super(application);
         playlistFile = getApplication().getSharedPreferences(
                PLAYLIST_FILE, Context.MODE_PRIVATE);
    }

    public MutableLiveData<ArrayList<PlaylistFile>> getPlaylistFileList() {
        if (playlistFileList == null) {
            playlistFileList = new MutableLiveData<>();
            loadPlaylistFiles();
        }
        return playlistFileList;
    }

    public MutableLiveData<Playlist> getPlaylist(){
        if (playlist == null) {
            playlist = new MutableLiveData<>();
            loadPlaylist();
        }


        return playlist;
    }

    private void loadPlaylistFiles(){
        Map<String, ?> allEntries = playlistFile.getAll();
        ArrayList<PlaylistFile> tempList = new ArrayList<>();
        PlaylistFile tempPlaylistFile = new PlaylistFile();
        tempPlaylistFile.setmName("All songs");
        tempList.add(tempPlaylistFile);
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            tempPlaylistFile = new PlaylistFile();
            tempPlaylistFile.setmName(entry.getKey());
            tempPlaylistFile.setmPath(entry.getValue().toString());
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            tempList.add(tempPlaylistFile);
        }
        playlistFileList.setValue(tempList);

    }

    private void addToPlaylistFile(){
        SharedPreferences.Editor editor = playlistFile.edit();
        for (int i = 0; i < 5; i++) {
            editor.putInt("name"+i,i);

        }
        editor.apply();
    }


    private void loadPlaylist(){
        String[] songProj = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            };

            String songSelect = MediaStore.Audio.Media.IS_MUSIC + " != 0";
//            String[] selectArgs = new String[]{mPath + "%"};
        Cursor cursor = getApplication().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,songProj,songSelect,null,null);
        Playlist temp = new Playlist();
        if (cursor!= null)
            temp.setSongs(PlaylistBuilder.cursorToPlaylist(cursor));
        playlist.setValue(temp);
    }
}
