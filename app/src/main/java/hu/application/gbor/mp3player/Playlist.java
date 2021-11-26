package hu.application.gbor.mp3player;

import java.util.ArrayList;

class Playlist {

    private ArrayList<Song> songs = new ArrayList<>();
    private String name;
    private boolean isPlaying;


    private int currentSongIndex;

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public Song getCurrentSong() {
        return songs.get(currentSongIndex);
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public void setCurrentSongIndex(int currentSongIndex) {
        this.currentSongIndex = currentSongIndex;
    }
}
