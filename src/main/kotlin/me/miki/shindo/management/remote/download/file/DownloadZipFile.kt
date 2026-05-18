package me.miki.shindo.management.remote.download.file

import java.io.File

class DownloadZipFile(
    url: String,
    fileName: String,
    outputDir: File,
    size: Long,
    val unzippedSize: Long,
) : DownloadFile(url, fileName, outputDir, size)
