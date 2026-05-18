package me.miki.shindo.management.remote.download

import me.miki.shindo.management.remote.download.file.DownloadFile
import me.miki.shindo.management.remote.download.file.DownloadZipFile
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.DirectoryUtils
import me.miki.shindo.utils.file.FileExtractor
import me.miki.shindo.utils.network.HttpUtils
import java.io.File

class DownloadManager {
    private val downloadFiles = ArrayList<DownloadFile>()
    private var downloaded = false

    init {
        TaskExecutor.runAsync(ThreadPoolType.NETWORK) { startDownloads() }
    }

    private fun startDownloads() {
        for (df in downloadFiles) {
            if (!df.outputDir.exists()) df.outputDir.mkdirs()
            when (df) {
                is DownloadZipFile -> {
                    val dzf = df
                    if (DirectoryUtils.getDirectorySize(dzf.outputDir) != dzf.unzippedSize) {
                        val outputFile = File(dzf.outputDir, dzf.fileName)
                        HttpUtils.downloadFile(dzf.url, outputFile)
                        FileExtractor.unzip(outputFile, dzf.outputDir)
                        outputFile.delete()
                    }
                }

                else -> {
                    val outputFile = File(df.outputDir, df.fileName)
                    if (outputFile.length() != df.size) HttpUtils.downloadFile(df.url, outputFile)
                }
            }
        }
        checkFiles()
    }

    private fun checkFiles() {
        for (df in downloadFiles) {
            when (df) {
                is DownloadZipFile -> {
                    val dzf = df
                    if (DirectoryUtils.getDirectorySize(dzf.outputDir) != dzf.unzippedSize) {
                        startDownloads()
                        return
                    }
                }

                else -> {
                    val outputFile = File(df.outputDir, df.fileName)
                    if (outputFile.length() != df.size) {
                        startDownloads()
                        return
                    }
                }
            }
        }
        downloaded = true
    }

    fun isDownloaded(): Boolean = downloaded
}
