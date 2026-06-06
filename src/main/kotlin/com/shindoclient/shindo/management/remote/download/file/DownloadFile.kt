package com.shindoclient.shindo.management.remote.download.file

import java.io.File

open class DownloadFile(
    val url: String,
    val fileName: String,
    val outputDir: File,
    val size: Long,
)
