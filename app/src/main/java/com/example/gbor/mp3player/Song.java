package com.example.gbor.mp3player;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.content.ContextCompat;

public class Song {

    private String path;
    private String artist;
    private String title;
    private String album;
    private Bitmap albumImage;

    public Song(String path){
        this.path = path;
        init();

        /*songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        if(songData.get("songArtist")==null)
            songData.put("songArtist", "");

        songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        if(songData.get("songTitle")==null)
            songData.put("songTitle", "");

        songData.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        if(songData.get("songAlbum")==null)
            songData.put("songAlbum", "");

        return songData;*/
    }

    private void init(){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this.path);
        artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        if(mediaMetadataRetriever.getEmbeddedPicture() != null)
            albumImage = BitmapFactory.decodeByteArray(mediaMetadataRetriever.getEmbeddedPicture(),
                    0, mediaMetadataRetriever.getEmbeddedPicture().length);
        else
            albumImage = null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        init();
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public Bitmap getAlbumImage() {
        return albumImage;
    }
}
