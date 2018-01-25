package com.example.gbor.mp3player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;



class SongManager {



    static ArrayList<String> getPlaylist(String Media_path) {
        ArrayList<String> songList = new ArrayList<>();
        File home = new File(Media_path);
        FileExtensionFilter fileExtensionFilter = new FileExtensionFilter();
        if (home.listFiles(fileExtensionFilter).length > 0) {
            for (File file : home.listFiles(fileExtensionFilter)) {
                songList.add(file.getPath());
            }
        }

        Collections.sort(songList);
        return songList;
    }

    static boolean hasMP3(String path){
        return new File(path).listFiles(new FileExtensionFilter()).length > 0;
    }

    static class FileExtensionFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".mp3");
        }
    }

}

