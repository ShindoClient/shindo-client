package me.miki.shindo.management.remote.download.file;

import java.io.File;

public class DownloadFile {

    private final String url;
    private final String fileName;
    private final File outputDir;
    private final long size;

    public DownloadFile(String url, String fileName, File outputDir, long size) {
        this.url = url;
        this.fileName = fileName;
        this.outputDir = outputDir;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public long getSize() {
        return size;
    }
}
