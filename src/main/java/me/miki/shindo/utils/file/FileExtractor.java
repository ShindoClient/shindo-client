package me.miki.shindo.utils.file;

import me.miki.shindo.logger.ShindoLogger;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileExtractor {

    private static void un7zip(File sevenZFile, File destDir) {
        if (!destDir.exists()) destDir.mkdirs();

        try (SevenZFile sevenZ = new SevenZFile(sevenZFile)) {
            SevenZArchiveEntry entry;
            byte[] buffer = new byte[8192];

            while ((entry = sevenZ.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream out = new FileOutputStream(newFile)) {
                        int count;
                        while ((count = sevenZ.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                        }
                    }
                }
            }
        } catch (IOException e) {
            ShindoLogger.error("An error occurred while extracting 7z file: " + sevenZFile.getName(), e);
        }
    }

    public static void unzip(final File file, final File dest) {
        try {
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(file.toPath()));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                final File f = new File(dest, ze.getName());
                if (ze.isDirectory()) {
                    f.mkdirs();
                } else {
                    final FileOutputStream fos = new FileOutputStream(f);
                    final byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            zis.close();
        } catch (Exception e) {
            ShindoLogger.error("An error occurred while extracting zip file: " + file.getName(), e);
        }
    }

    public static void extract(File file, File dest) {
        String name = file.getName().toLowerCase();

        try {
            if (name.endsWith(".zip")) {
                unzip(file, dest);
            } else if (name.endsWith(".7z")) {
                un7zip(file, dest);
            } else {
                System.out.println("[WARN] Tipo de arquivo não suportado: " + name);
            }
        } catch (Exception e) {
            ShindoLogger.error("Failed to extract file: " + file.getName(), e);
        }
    }

}
