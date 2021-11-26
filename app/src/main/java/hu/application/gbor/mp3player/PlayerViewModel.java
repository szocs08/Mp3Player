package hu.application.gbor.mp3player;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerViewModel extends AndroidViewModel {
    private MutableLiveData<Playlist> playlist;


    public PlayerViewModel(Application application){
        super(application);
    }

    MutableLiveData<Playlist> getPlaylist(){
        if (playlist == null) {
            playlist = new MutableLiveData<>();
            loadPlaylist();
        }

        return playlist;
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
