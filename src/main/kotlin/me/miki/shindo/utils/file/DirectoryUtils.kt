package me.miki.shindo.utils.file

import java.io.File

object DirectoryUtils {
    @JvmStatic
    fun deleteDirectory(directory: File) {
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
            directory.delete()
        }
    }

    @JvmStatic
    fun getDirectorySize(directory: File): Long {
        var size: Long = 0
        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    size +=
                        if (file.isFile) {
                            file.length()
                        } else if (file.isDirectory) {
                            getDirectorySize(file)
                        } else {
                            0
                        }
                }
            }
        } else if (directory.isFile) {
            size = directory.length()
        }
        return size
    }
}
