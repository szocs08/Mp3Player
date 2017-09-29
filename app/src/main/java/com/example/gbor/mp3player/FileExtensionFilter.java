package com.example.gbor.mp3player;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by szgab on 2017.09.29..
 */

public class FileExtensionFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".mp3");
    }
}
