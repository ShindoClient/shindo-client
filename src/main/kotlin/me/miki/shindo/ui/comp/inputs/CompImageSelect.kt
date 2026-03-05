package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.ImageSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.io.File
import java.io.IOException

class CompImageSelect : Comp {
    private val imageSetting: ImageSetting

    constructor(x: Float, y: Float, imageSetting: ImageSetting) : super(x, y) {
        this.imageSetting = imageSetting
        setWidth(16F)
        setHeight(16F)
    }

    constructor(imageSetting: ImageSetting) : super(0f, 0f) {
        this.imageSetting = imageSetting
        setWidth(16F)
        setHeight(16F)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accentColor = accent
        val paletteColors = palette

        val name = imageSetting.getImage()?.name ?: me.miki.shindo.management.language.TranslateText.NONE.getText()
        val label = nvgInstance.getLimitText(name, 8.5f, Fonts.REGULAR, 96f)
        val labelWidth = nvgInstance.getTextWidth(label, 8.5f, Fonts.REGULAR)
        val x = getX()
        val y = getY()
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, 16f, 16f)

        nvgInstance.drawRoundedRect(
            x - labelWidth - 8f,
            y,
            labelWidth + 6f,
            16f,
            4f,
            ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 176)
        )
        nvgInstance.drawText(
            label,
            x - labelWidth - 5f,
            y + 4f,
            paletteColors.getFontColor(ColorType.DARK),
            8.5f,
            Fonts.REGULAR
        )
        nvgInstance.drawGradientRoundedRect(
            x,
            y,
            16f,
            16f,
            4f,
            ColorUtils.applyAlpha(accentColor.getColor1(), if (hovered) 210 else 168),
            ColorUtils.applyAlpha(accentColor.getColor2(), if (hovered) 228 else 182)
        )
        nvgInstance.drawOutlineRoundedRect(
            x,
            y,
            16f,
            16f,
            4f,
            1f,
            ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), if (hovered) 160 else 118)
        )
        nvgInstance.drawCenteredText(
            LegacyIcon.FOLDER,
            x + 8,
            y + 2.5f,
            paletteColors.getFontColor(ColorType.DARK),
            10f,
            Fonts.LEGACYICON
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), 16f, 16f) && mouseButton == 0) {
            TaskExecutor.runAsync(ThreadPoolType.IO) {
                val image = FileUtils.selectImageFile()
                if (image != null) {
                    val fileManager = Shindo.getInstance().fileManager
                    val cacheDir = File(fileManager.cacheDir, "custom-image")
                    fileManager.createDir(cacheDir)

                    val newImage = File(cacheDir, image.name)
                    try {
                        FileUtils.copyFile(image, newImage)
                    } catch (_: IOException) {
                    }

                    imageSetting.setImage(newImage)
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
