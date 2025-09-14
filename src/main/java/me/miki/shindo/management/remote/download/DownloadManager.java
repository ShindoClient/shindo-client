package me.miki.shindo.management.remote.download;


import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.remote.download.file.DownloadFile;
import me.miki.shindo.management.remote.download.file.DownloadZipFile;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.file.DirectoryUtils;
import me.miki.shindo.utils.file.FileExtractor;
import me.miki.shindo.utils.network.HttpUtils;

import java.io.File;
import java.util.ArrayList;

public class DownloadManager {


    private final ArrayList<DownloadFile> downloadFiles = new ArrayList<DownloadFile>();

    private boolean downloaded;

    public DownloadManager() {

        FileManager fileManager = Shindo.getInstance().getFileManager();

        downloaded = false;


        Multithreading.runAsync(this::startDownloads);
    }

    private void startDownloads() {

        for (DownloadFile df : downloadFiles) {

            if (!df.getOutputDir().exists()) {
                df.getOutputDir().mkdirs();
            }

            if (df instanceof DownloadZipFile) {

                DownloadZipFile dzf = (DownloadZipFile) df;

                if (DirectoryUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {

                    File outputFile = new File(dzf.getOutputDir(), dzf.getFileName());

                    HttpUtils.downloadFile(dzf.getUrl(), outputFile);
                    FileExtractor.unzip(outputFile, dzf.getOutputDir());
                    outputFile.delete();
                }
            } else {

                File outputFile = new File(df.getOutputDir(), df.getFileName());

                if (outputFile.length() != df.getSize()) {
                    HttpUtils.downloadFile(df.getUrl(), outputFile);
                }
            }
        }

        checkFiles();
    }

    private void checkFiles() {

        for (DownloadFile df : downloadFiles) {

            if (df instanceof DownloadZipFile) {

                DownloadZipFile dzf = (DownloadZipFile) df;

                if (DirectoryUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {
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
}