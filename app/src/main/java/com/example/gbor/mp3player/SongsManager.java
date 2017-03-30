package com.example.gbor.mp3player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * Created by Gábor on 2017.03.21..
 */

public class SongsManager {
    final String MEDIA_PATH = new String("/sdcard/");

    private ArrayList<String> songList = new ArrayList<String>();

    public SongsManager(){

    }

    public ArrayList<String> getPlaylist(){
        File home = new File(MEDIA_PATH);
        int a = home.listFiles(new FileExtensionFilter()).length;
        if(home.listFiles(new FileExtensionFilter()).length > 0){
            for (File file:home.listFiles(new FileExtensionFilter())) {
                songList.add(file.getPath());
            }
        }

        return songList;
    }

    class FileExtensionFilter implements FilenameFilter{
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".mp3");
        }
    }
}

