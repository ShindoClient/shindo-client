package me.miki.shindo.utils.file.filter.image

import me.miki.shindo.utils.file.FileUtils
import java.io.File
import javax.swing.filechooser.FileFilter

class PngFileFilter : FileFilter() {
    override fun accept(file: File?): Boolean {
        if (file == null) return false
        if (file.isDirectory) return true
        val extension = FileUtils.getExtension(file)
        return extension != null && extension.equals("png", ignoreCase = true)
    }

    override fun getDescription(): String = "Png Images (*.png)"
}
