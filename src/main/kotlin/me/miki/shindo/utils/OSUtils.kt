package me.miki.shindo.utils

object OSUtils {

    private val osName = System.getProperty("os.name").lowercase()

    @JvmField
    val windows: Boolean = osName.contains("windows")

    @JvmField
    val linux: Boolean = osName.contains("linux")

    @JvmField
    val mac: Boolean = osName.contains("mac")

    @JvmField
    val unix: Boolean = linux || mac

    @JvmStatic
    fun getPlatform(): String {
        return when {
            windows -> "Windows"
            linux -> "Linux"
            mac -> "Mac"
            else -> "Unknown"
        }
    }
}
