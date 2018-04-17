package com.example.gbor.mp3player;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;


class SongManager {

    private static final String SETTINGS_FILE = "com.example.gbor.mp3player.settings";


    static ArrayList<String> getPlaylist(Context context) {
        ArrayList<String> songList = new ArrayList<>();
        SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        try{

            String[] songProj = {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATA,
            };

            String songSelect = MediaStore.Audio.Media.DATA + " like ?";
            String[] selectArgs =  new String[]{settings.getString("path", Environment.getExternalStorageDirectory().toString())+"%"};
            Cursor songCursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songProj,
                    songSelect,
                    selectArgs,
                    MediaStore.Audio.Media.ARTIST+", "+MediaStore.Audio.Media.TITLE);
            if (songCursor != null) {
                songCursor.moveToFirst();
                do{
                    int dataColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int idColumn = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    songList.add(songCursor.getString(idColumn));
                    Log.i("SONGS", songCursor.getString(dataColumn));


                }while (songCursor.moveToNext());

                songCursor.close();
            } else {
                Log.e("CURSOR","Cursor load failed");
            }
        }catch(NullPointerException exception){
            exception.printStackTrace();
        }
        return songList;
    }

    public static String getPath(Context context,String id){
        String returnValue;
        String[] songProj = {MediaStore.Audio.Media.DATA};
        String songSelect = MediaStore.Audio.Media._ID + " = ?";
        String[] selectArgs =  new String[]{id};
        Cursor songCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songProj,
                songSelect,
                selectArgs,
                MediaStore.Audio.Media._ID);
        if (songCursor != null) {
            songCursor.moveToFirst();

            int dataColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            returnValue = songCursor.getString(dataColumn);

            songCursor.close();
        } else {
            returnValue=null;
            Log.e("CURSOR","Cursor load failed");
        }
        return returnValue;
    }

    public static HashMap<String,String> getSongData(Context context, String id){
        HashMap<String,String> returnValue = new HashMap<>();
        String[] songProj = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ALBUM_ID};
        String songSelect = MediaStore.Audio.Media._ID + " = ?";
        String[] selectArgs =  new String[]{id};
        Cursor songCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songProj,
                songSelect,
                selectArgs,
                MediaStore.Audio.Media._ID);
        if (songCursor != null) {
            songCursor.moveToFirst();

            int artistColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int titleColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int albumColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int displayColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int albumIDColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            returnValue.put("artist",songCursor.getString(artistColumn));
            returnValue.put("title",songCursor.getString(titleColumn));
            returnValue.put("album",songCursor.getString(albumColumn));
            returnValue.put("displayName",songCursor.getString(displayColumn));
            returnValue.put("albumID",songCursor.getString(albumIDColumn));
            songCursor.close();
        } else {
            returnValue=null;
            Log.e("CURSOR","Cursor load failed");
        }
        return returnValue;
    }

    public static String getAlbumThumbnail(Context context, String id){
        String returnValue = null;
        if (id!=null){
            String[] songProj = new String[]{MediaStore.Audio.Albums.ALBUM_ART};
            String songSelect = MediaStore.Audio.Albums._ID + " = ?";
            String[] selectArgs =  new String[]{id};
            Cursor albumCursor = context.getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    songProj,
                    songSelect,
                    selectArgs,
                    null);
            if (albumCursor != null) {
                albumCursor.moveToFirst();
                returnValue = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                albumCursor.close();
            } else {
                returnValue=null;
                Log.e("CURSOR","Cursor load failed");
            }
        }
        return returnValue;
    }


    static boolean hasMP3(Context context, String path){
        Boolean returnValue = false;
        String[] songProj = {MediaStore.Audio.Media.ARTIST};
        String songSelect = MediaStore.Audio.Media.DATA + " like ?";
        String[] selectArgs =  new String[]{path};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songProj,
                songSelect,
                selectArgs,
                null);
        if (cursor != null) {
            returnValue = cursor.getCount() > 0;
            cursor.close();
        }

        return returnValue ;
    }

    public static HashMap<String,String> getSongPlaylistData(Context context, String id){
        HashMap<String,String> returnValue = new HashMap<>();
        String[] songProj = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,};
        String songSelect = MediaStore.Audio.Media._ID + " = ?";
        String[] selectArgs =  new String[]{id};
        Cursor songCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songProj,
                songSelect,
                selectArgs,
                null);
        if (songCursor != null) {
            songCursor.moveToFirst();

            int artistColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int titleColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int displayColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);

            returnValue.put("artist",songCursor.getString(artistColumn));
            returnValue.put("title",songCursor.getString(titleColumn));
            returnValue.put("displayName",songCursor.getString(displayColumn));
            songCursor.close();
        } else {
            returnValue=null;
            Log.e("CURSOR","Cursor load failed");
        }
        return returnValue;
    }

}

