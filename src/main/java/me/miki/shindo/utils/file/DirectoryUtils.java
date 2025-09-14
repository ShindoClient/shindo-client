package me.miki.shindo.utils.file;

import java.io.File;

public class DirectoryUtils {

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }

            directory.delete();
        }
    }

    public static long getDirectorySize(File directory) {

        long size = 0;

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    }
                }
            }
        } else if (directory.isFile()) {
            size = directory.length();
        }

        return size;
    }
}
