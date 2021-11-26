package hu.application.gbor.mp3player;

import android.database.Cursor;

import java.util.ArrayList;

class PlaylistBuilder {
    public static ArrayList<Song> cursorToPlaylist(Cursor cursor){
        ArrayList<Song> songs = new ArrayList<>();
        while (cursor.moveToNext()){
            songs.add(SongBuilder.cursorToSong(cursor));
        }
        return songs;
    }

    static class SongBuilder{
        public static Song cursorToSong(Cursor cursor){
            Song song = new Song();
            song.setData(cursor.getString(0));
            song.setTitle(cursor.getString(1));
            song.setArtist(cursor.getString(2));
            song.setAlbum(cursor.getString(3));
            song.setDuration(cursor.getInt(4));
            return song;
        }
    }
}
