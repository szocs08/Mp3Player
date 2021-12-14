package hu.application.gbor.mp3player;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

class Song {
    static private final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    private String title;
    private String artist;
    private String album;
    private String data;
    private int duration;

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getData() {
        return data;
    }

    public int getDuration() {
        return duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Bitmap getAlbumArt(){
        mediaMetadataRetriever.setDataSource(data);
        if (mediaMetadataRetriever.getEmbeddedPicture()!=null)
            return BitmapFactory.decodeByteArray(mediaMetadataRetriever.getEmbeddedPicture(),0,mediaMetadataRetriever.getEmbeddedPicture().length);
        else
            return null;
    }

}
