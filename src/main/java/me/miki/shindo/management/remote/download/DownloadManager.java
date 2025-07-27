package me.miki.shindo.management.remote.download;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.remote.download.file.DownloadFile;
import me.miki.shindo.management.remote.download.file.DownloadZipFile;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.file.FileUtils;
import me.miki.shindo.utils.network.HttpUtils;

import java.io.File;
import java.util.ArrayList;


public class DownloadManager {

    private final ArrayList<DownloadFile> downloadFiles = new ArrayList<DownloadFile>();

    private boolean downloaded;

    public DownloadManager() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        downloaded = false;
        downloadFiles.add(new DownloadFile("https://github.com/ShindoClient/RESOURCES/releases/download/latest/ytdlp.exe",
                "ytdlp.exe", new File(fileManager.getExternalDir(), "ytdlp"), 18182514));
        downloadFiles.add(new DownloadZipFile("https://github.com/ShindoClient/RESOURCES/releases/download/latest/ffmpeg.zip",
                "ffmpeg.zip", new File(fileManager.getExternalDir(), "ffmpeg"), 148280843, 413897555));

        Multithreading.runAsync(() -> startDownloads());
    }

    private void startDownloads() {

        for (DownloadFile df : downloadFiles) {

            if (!df.getOutputDir().exists()) {
                df.getOutputDir().mkdirs();
            }

            if (df instanceof DownloadZipFile) {

                DownloadZipFile dzf = (DownloadZipFile) df;

                if (FileUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {

                    File outputFile = new File(dzf.getOutputDir(), dzf.getFileName());

                    HttpUtils.downloadFile(dzf.getUrl(), outputFile, dzf.getSize());
                    FileUtils.unzip(outputFile, dzf.getOutputDir());
                    outputFile.delete();
                }
            } else {

                File outputFile = new File(df.getOutputDir(), df.getFileName());

                if (outputFile.length() != df.getSize()) {
                    HttpUtils.downloadFile(df.getUrl(), outputFile, df.getSize());
                }
            }
        }

        checkFiles();
    }

    private void checkFiles() {

        for (DownloadFile df : downloadFiles) {

            if (df instanceof DownloadZipFile) {

                DownloadZipFile dzf = (DownloadZipFile) df;

                if (FileUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {
                    startDownloads();
                }
            } else {

                File outputFile = new File(df.getOutputDir(), df.getFileName());

                if (outputFile.length() != df.getSize()) {
                    startDownloads();
                }
            }
        }

        downloaded = true;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public DownloadFile getDownloadByFile(String fileName) {
        for (DownloadFile file : downloadFiles) {
            if (file.getFileName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    public long getTotalSize() {
        return downloadFiles.stream().mapToLong(DownloadFile::getSize).sum();
    }

    public long getTotalDownloaded() {
        return downloadFiles.stream().mapToLong(DownloadFile::getDownloadedBytes).sum();
    }

    public float getProgress() {
        long total = getTotalSize();
        if (total == 0) return 0f;
        return (float) getTotalDownloaded() / total;
    }
}