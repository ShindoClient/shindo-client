package me.miki.shindo.utils

import me.miki.shindo.logger.ShindoLogger
import net.minecraft.util.Util
import java.awt.Image
import javax.imageio.ImageIO

object MacOSUtils {
    @JvmStatic
    fun setDockIcon(path: String) {
        if (Util.getOSType() != Util.EnumOS.OSX) return

        val iconStream = MacOSUtils::class.java.getResourceAsStream(path)
        if (iconStream != null) {
            try {
                val appClass = Class.forName("com.apple.eawt.Application")
                val application = appClass.getMethod("getApplication").invoke(null)
                val iconImage = ImageIO.read(iconStream)
                appClass.getMethod("setDockIconImage", Image::class.java).invoke(application, iconImage)
            } catch (e: Exception) {
                ShindoLogger.error("[ MacOS Utils ] Error setting dock icon: ${e.message}")
            }
        } else {
            ShindoLogger.error("[ MacOS Utils ] Icon file could not be found")
        }
    }
}
