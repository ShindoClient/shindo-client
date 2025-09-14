package me.miki.shindo.utils.file.filter.sound;

import me.miki.shindo.utils.file.FileUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class Mp3FileFilter extends FileFilter {

    @Override
    public boolean accept(File file) {

        if (file.isDirectory()) {
            return true;
        }

        String extension = FileUtils.getExtension(file);

        return extension != null && extension.equalsIgnoreCase("mp3");
    }

    @Override
    public String getDescription() {
        return "Sounds (*.mp3)";
    }
}
