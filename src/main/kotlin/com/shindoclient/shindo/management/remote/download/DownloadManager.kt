package com.shindoclient.shindo.management.remote.download

import com.shindoclient.shindo.management.remote.download.file.DownloadFile
import com.shindoclient.shindo.management.remote.download.file.DownloadZipFile
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import com.shindoclient.shindo.utils.file.DirectoryUtils
import com.shindoclient.shindo.utils.file.FileExtractor
import com.shindoclient.shindo.utils.network.HttpUtils
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
                    if (DirectoryUtils.getDirectorySize(df.outputDir) != df.unzippedSize) {
                        val outputFile = File(df.outputDir, df.fileName)
                        HttpUtils.downloadFile(df.url, outputFile)
                        FileExtractor.unzip(outputFile, df.outputDir)
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
                    if (DirectoryUtils.getDirectorySize(df.outputDir) != df.unzippedSize) {
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
