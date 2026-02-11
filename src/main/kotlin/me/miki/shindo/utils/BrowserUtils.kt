package me.miki.shindo.utils

import net.minecraft.util.Util
import org.lwjgl.Sys
import java.awt.Desktop
import java.io.IOException
import java.net.URI
object BrowserUtils {

    @JvmStatic
    fun openUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }

        val uri = try {
            URI.create(url.trim())
        } catch (_: IllegalArgumentException) {
            return false
        }

        if (tryDesktopBrowse(uri)) return true
        if (tryNativeOpen(uri.toString())) return true
        return tryLwjglFallback(uri.toString())
    }

    private fun tryDesktopBrowse(uri: URI): Boolean {
        if (!Desktop.isDesktopSupported()) return false

        return try {
            val desktop = Desktop.getDesktop()
            if (!desktop.isSupported(Desktop.Action.BROWSE)) return false
            desktop.browse(uri)
            true
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun tryNativeOpen(url: String): Boolean {
        return try {
            when (Util.getOSType()) {
                Util.EnumOS.OSX -> {
                    Runtime.getRuntime().exec(arrayOf("/usr/bin/open", url))
                    true
                }

                Util.EnumOS.WINDOWS -> {
                    Runtime.getRuntime().exec(arrayOf("cmd.exe", "/C", "start", "\"\"", url))
                    true
                }

                else -> false
            }
        } catch (_: IOException) {
            false
        }
    }

    private fun tryLwjglFallback(url: String): Boolean {
        return try {
            Sys.openURL(url)
            true
        } catch (_: Exception) {
            false
        }
    }
}
