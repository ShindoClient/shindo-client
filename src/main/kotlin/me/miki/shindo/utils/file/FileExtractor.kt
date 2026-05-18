package me.miki.shindo.utils.file

import me.miki.shindo.logger.ShindoLogger
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*
import java.util.zip.ZipInputStream

object FileExtractor {
    private fun un7zip(
        sevenZFile: File,
        destDir: File,
    ) {
        if (!destDir.exists()) destDir.mkdirs()

        try {
            SevenZFile(sevenZFile).use { sevenZ ->
                val buffer = ByteArray(8192)
                var entry = sevenZ.nextEntry
                while (entry != null) {
                    val newFile = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        File(newFile.parent).mkdirs()
                        FileOutputStream(newFile).use { out ->
                            var count: Int
                            while (sevenZ.read(buffer).also { count = it } > 0) {
                                out.write(buffer, 0, count)
                            }
                        }
                    }
                    entry = sevenZ.nextEntry
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("An error occurred while extracting 7z file: ${sevenZFile.name}", e)
        }
    }

    @JvmStatic
    fun unzip(
        file: File,
        dest: File,
    ) {
        try {
            ZipInputStream(Files.newInputStream(file.toPath())).use { zis ->
                var ze = zis.nextEntry
                while (ze != null) {
                    val f = File(dest, ze.name)
                    if (ze.isDirectory) {
                        f.mkdirs()
                    } else {
                        FileOutputStream(f).use { fos ->
                            val buffer = ByteArray(1024)
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }
                    }
                    ze = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("An error occurred while extracting zip file: ${file.name}", e)
        }
    }

    @JvmStatic
    fun extract(
        file: File,
        dest: File,
    ) {
        val name = file.name.lowercase(Locale.ROOT)
        try {
            when {
                name.endsWith(".zip") -> unzip(file, dest)
                name.endsWith(".7z") -> un7zip(file, dest)
                else -> ShindoLogger.warn("Tipo de arquivo não suportado: $name")
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to extract file: ${file.name}", e)
        }
    }
}
