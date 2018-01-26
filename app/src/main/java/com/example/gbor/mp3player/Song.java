package com.example.gbor.mp3player;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

public class Song {

    private String path;
    private String artist;
    private String title;
    private String album;
    private Bitmap albumImage;

    public Song(String path){
        this.path = path;
        init();
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

    String getArtist() {
        return artist;
    }

    String getTitle() {
        return title;
    }

    String getAlbum() {
        return album;
    }

    Bitmap getAlbumImage() {
        return albumImage;
    }
}
