package me.miki.shindo.utils.file.filter;

import me.miki.shindo.utils.file.FileUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SoundFileFilter extends FileFilter {

    @Override
    public boolean accept(File file) {

        if (file.isDirectory()) {
            return true;
        }

        String extension = FileUtils.getExtension(file);

        return extension != null && extension.equalsIgnoreCase("wav");
    }

    @Override
    public String getDescription() {
        return "Sounds (*.wav)";
    }
}
