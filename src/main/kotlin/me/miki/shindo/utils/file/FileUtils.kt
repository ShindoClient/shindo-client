package me.miki.shindo.utils.file

import me.miki.shindo.utils.file.filter.image.PngFileFilter
import me.miki.shindo.utils.file.filter.sound.WavFileFilter
import net.minecraft.util.Util
import org.lwjgl.Sys
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import javax.swing.JFileChooser

object FileUtils {

    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destFile: File) {
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(1024)
                var length: Int
                while (input.read(buffer).also { length = it } > 0) {
                    output.write(buffer, 0, length)
                }
            }
        }
    }

    @JvmStatic
    fun selectImageFile(): File? = chooseFile(PngFileFilter())

    @JvmStatic
    fun selectSoundFile(): File? = chooseFile(WavFileFilter())

    private fun chooseFile(filter: javax.swing.filechooser.FileFilter): File? {
        val chooser = JFileChooser()
        chooser.fileFilter = filter
        chooser.isAcceptAllFileFilterUsed = false
        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    }

    @JvmStatic
    fun getBaseName(fileName: String?): String {
        if (fileName == null) return "null"
        val point = fileName.lastIndexOf(".")
        return if (point != -1) fileName.substring(0, point) else fileName
    }

    @JvmStatic
    fun getBaseName(file: File): String = getBaseName(file.name)

    @JvmStatic
    fun getExtension(fileName: String?): String? {
        if (fileName == null) return null
        val last = fileName.lastIndexOf(".")
        if (last == -1) return "null"
        return fileName.substring(last + 1)
    }

    @JvmStatic
    fun getExtension(file: File): String? = getExtension(file.name)

    @JvmStatic
    fun isAudioFile(fileName: String?): Boolean {
        val ext = getExtension(fileName) ?: return false
        return ext.equals("mp3", true) || ext.equals("wav", true) || ext.equals("ogg", true)
    }

    @JvmStatic
    fun isAudioFile(file: File): Boolean = isAudioFile(file.name)

    @JvmStatic
    fun isImageFile(fileName: String?): Boolean {
        val ext = getExtension(fileName) ?: return false
        return ext.equals("jpeg", true) || ext.equals("png", true) || ext.equals("jpg", true)
    }

    @JvmStatic
    fun isImageFile(file: File): Boolean = isImageFile(file.name)

    @JvmStatic
    fun openFolderAtPath(folder: File) {
        val absolutePath = folder.absolutePath
        when (Util.getOSType()) {
            Util.EnumOS.OSX -> try {
                Runtime.getRuntime().exec(arrayOf("/usr/bin/open", absolutePath)); return
            } catch (_: IOException) {
            }

            Util.EnumOS.WINDOWS -> try {
                Runtime.getRuntime().exec(String.format("cmd.exe /C start \"Open file\" \"%s\"", absolutePath)); return
            } catch (_: IOException) {
            }

            else -> {}
        }

        try {
            val desktopClass = Class.forName("java.awt.Desktop")
            val desktop = desktopClass.getMethod("getDesktop").invoke(null)
            desktopClass.getMethod("browse", URI::class.java).invoke(desktop, folder.toURI())
        } catch (_: Throwable) {
            Sys.openURL("file://$absolutePath")
        }
    }
}
