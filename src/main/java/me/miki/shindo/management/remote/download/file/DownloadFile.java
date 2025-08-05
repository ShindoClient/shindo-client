package me.miki.shindo.management.remote.download.file;

import java.io.File;

public class DownloadFile {

    private final String url;
    private final String fileName;
    private final File outputDir;
    private final long size;
    private volatile long downloadedBytes;

    public DownloadFile(String url, String fileName, File outputDir, long size) {
        this.url = url;
        this.fileName = fileName;
        this.outputDir = outputDir;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public long getSize() {
        return size;
    }


    public synchronized void addDownloadedBytes(long bytes) {
        this.downloadedBytes += bytes;
    }

    public synchronized long getDownloadedBytes() {
        return downloadedBytes;
    }

    public String getFileName() {
        return new File(getUrl()).getName(); // importante para buscar pelo nome no .part
    }
}
