package me.miki.shindo.utils.file.filter;

import me.miki.shindo.utils.file.FileUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class PngFileFilter extends FileFilter {

    @Override
    public boolean accept(File file) {

        if (file.isDirectory()) {
            return true;
        }

        String extension = FileUtils.getExtension(file);

        return extension != null && extension.equalsIgnoreCase("png");
    }

    @Override
    public String getDescription() {
        return "Png Images (*.png)";
    }
}
