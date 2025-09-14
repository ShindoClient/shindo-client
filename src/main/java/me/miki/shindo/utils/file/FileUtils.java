package me.miki.shindo.utils.file;

import me.miki.shindo.utils.file.filter.image.PngFileFilter;
import me.miki.shindo.utils.file.filter.sound.WavFileFilter;
import net.minecraft.util.Util;
import org.lwjgl.Sys;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class FileUtils {

    public static void copyFile(File sourceFile, File destFile) throws IOException {

        try (FileInputStream input = new FileInputStream(sourceFile); FileOutputStream output = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        }
    }

    public static File selectImageFile() {

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileFilter(new PngFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    public static File selectSoundFile() {

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileFilter(new WavFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }


    public static String getBaseName(String fileName) {

        if (fileName == null) {
            return "null";
        }

        int point = fileName.lastIndexOf(".");

        if (point != -1) {
            return fileName.substring(0, point);
        }

        return fileName;
    }

    public static String getBaseName(File file) {
        return getBaseName(file.getName());
    }

    public static String getExtension(String fileName) {

        if (fileName == null) {
            return null;
        }

        int lastIndexOf = fileName.lastIndexOf(".");

        if (lastIndexOf == -1) {
            return "null";
        }

        return fileName.substring(lastIndexOf + 1);
    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    public static boolean isAudioFile(String fileName) {

        if (fileName == null) {
            return false;
        }

        String ext = getExtension(fileName);

        return ext.equals("mp3") || ext.equals("wav") || ext.equals("ogg");
    }

    public static boolean isAudioFile(File file) {
        return isAudioFile(file.getName());
    }

    public static boolean isImageFile(String fileName) {

        if (fileName == null) {
            return false;
        }

        String ext = getExtension(fileName);

        return ext.equals("jpeg") || ext.equals("png") || ext.equals("jpg");
    }

    public static boolean isImageFile(File file) {
        return isImageFile(file.getName());
    }

    /*
     * This is from the GuiScreenResourcePacks class
     * Copyright mojang
     *
     * This code accepts a path and tries to open it in the file browser
     */
    public static void openFolderAtPath(File folder) {
        String absolutePath = folder.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX) {
            try {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", absolutePath});
                return;
            } catch (IOException ignored) {
            }
        } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            try {
                Runtime.getRuntime().exec(String.format("cmd.exe /C start \"Open file\" \"%s\"", absolutePath));
                return;
            } catch (IOException ignored) {
            }
        }

        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, folder.toURI());
        } catch (Throwable throwable) {
            Sys.openURL("file://" + absolutePath);
        }

    }
}
