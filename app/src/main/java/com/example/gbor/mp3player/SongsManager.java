package com.example.gbor.mp3player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by Gábor on 2017.03.21..
 */

public class SongsManager {


    private String Media_path;

    private ArrayList<String> songList = new ArrayList<String>();

    public SongsManager(String path) {
        Media_path = path;
    }

    public void setMedia_path(String media_path) {
        Media_path = media_path;
    }

    public ArrayList<String> getPlaylist() {
        File home = new File(Media_path);
        int a = home.listFiles(new FileExtensionFilter()).length;
        if (home.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                songList.add(file.getPath());
            }
        }

        return songList;
    }

}

