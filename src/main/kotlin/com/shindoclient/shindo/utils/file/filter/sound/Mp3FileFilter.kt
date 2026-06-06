package com.shindoclient.shindo.utils.file.filter.sound

import com.shindoclient.shindo.utils.file.FileUtils
import java.io.File
import javax.swing.filechooser.FileFilter

class Mp3FileFilter : FileFilter() {
    override fun accept(file: File?): Boolean {
        if (file == null) return false
        if (file.isDirectory) return true
        val extension = FileUtils.getExtension(file)
        return extension != null && extension.equals("mp3", ignoreCase = true)
    }

    override fun getDescription(): String = "MP3 Audio (*.mp3)"
}
