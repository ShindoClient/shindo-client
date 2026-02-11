package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class ImageDisplayMod :
    HUDMod(TranslateText.IMAGE_DISPLAY, TranslateText.IMAGE_DISPLAY_DESCRIPTION, LegacyIcon.MOD_IMAGE_DISPLAY) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.RADIUS,
        min = 2.0,
        max = 64.0,
        current = 6.0,
        step = 1.0
    )
    private val radiusSetting = 6

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0, max = 1.0, current = 1.0)
    private val alphaSetting = 1.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHADOW)
    private val shadowSetting = false

    @Property(type = PropertyType.IMAGE, translate = TranslateText.IMAGE)
    private val imageFile: File? = null

    private var image: BufferedImage? = null
    private var prevImage: File? = null

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        nvg!!.setupAndDraw(Runnable { this.drawNanoVG() })
    }

    private fun drawNanoVG() {
        if (imageFile != null && imageFile != prevImage) {
            prevImage = imageFile
            try {
                image = ImageIO.read(imageFile)
            } catch (e: IOException) {
                ShindoLogger.error("Error reading image file: " + imageFile.absolutePath, e)
            }
        }

        if (image != null) {
            var width = image!!.width
            var height = image!!.height

            if (width > 500 || height > 500) {
                if ((width < 1000 || height < 1000)) {
                    width = width / 2
                    height = height / 2
                }

                if ((width > 1000 || height > 1000)) {
                    width = width / 3
                    height = height / 3
                }
            }

            if (shadowSetting) {
                this.drawShadow(0f, 0f, width.toFloat(), height.toFloat(), radiusSetting.toFloat())
            }

            this.drawRoundedImage(
                imageFile!!,
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                radiusSetting.toFloat(),
                alphaSetting.toFloat()
            )

            this.setWidth(width)
            this.setHeight(height)
        }
    }
}




