package me.miki.shindo.utils

import me.miki.shindo.logger.ShindoLogger
import java.util.*

object OSUtils {
    private val osName = System.getProperty("os.name").lowercase(Locale.ROOT)

    @JvmField
    val windows: Boolean = osName.contains("windows")

    @JvmField
    val linux: Boolean = osName.contains("linux")

    @JvmField
    val mac: Boolean = osName.contains("mac")

    @JvmField
    val unix: Boolean = linux || mac

    @JvmStatic
    fun getPlatform(): String =
        when {
            windows -> "Windows"
            linux -> "Linux"
            mac -> "Mac"
            else -> "Unknown"
        }

    @JvmStatic
    fun runWindowsBrowser(uri: String) {
        Runtime.getRuntime().exec(
            arrayOf("rundll32", "url.dll,FileProtocolHandler", uri),
        )
    }

    @JvmStatic
    fun runMacBrowser(uri: String) {
        Runtime.getRuntime().exec(
            arrayOf("open", uri),
        )
    }

    @JvmStatic
    fun runLinuxBrowser(uri: String): Boolean {
        val browsers =
            arrayOf(
                "xdg-open",
                "gio",
                "gnome-open",
                "kde-open",
                "kde-open5",
            )

        for (i in browsers.indices) {
            try {
                val browser = browsers[i]

                if (browser == "gio") {
                    Runtime.getRuntime().exec(arrayOf(browser, "open", uri))
                } else {
                    Runtime.getRuntime().exec(arrayOf(browser, uri))
                }

                return true
            } catch (e: Exception) {
                ShindoLogger.error(e.message!!)
            }
        }

        return false
    }
}
