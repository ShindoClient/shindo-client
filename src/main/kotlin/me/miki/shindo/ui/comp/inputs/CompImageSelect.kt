package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.ImageSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color
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
        val nameWidth = nvgInstance.getTextWidth(name, 9f, Fonts.REGULAR)

        nvgInstance.drawGradientRoundedRect(getX(), getY(), 16f, 16f, 4f, accentColor.getColor1(), accentColor.getColor2())
        nvgInstance.drawText(
                name,
                getX() - nameWidth - 5,
                getY() + 4,
                paletteColors.getFontColor(ColorType.DARK),
                9f,
                Fonts.REGULAR
        )
        nvgInstance.drawCenteredText(LegacyIcon.FOLDER, getX() + 8, getY() + 2.5f, Color.WHITE, 10f, Fonts.LEGACYICON)

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