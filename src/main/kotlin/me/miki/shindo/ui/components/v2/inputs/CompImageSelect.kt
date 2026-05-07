package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.ImageSetting
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.Multithreading.runAsync
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

        val name =
            if (imageSetting.getImage() == null) TranslateText.NONE.getText() else imageSetting.getImage()!!.getName()
        val nameWidth = nvgInstance.getTextWidth(name, 9f, Fonts.REGULAR)

        nvgInstance.drawGradientRoundedRect(
            this.getX(),
            this.getY(),
            16f,
            16f,
            4f,
            accentColor.getColor1(),
            accentColor.getColor2()
        )
        nvgInstance.drawText(
            name,
            this.getX() - nameWidth - 5,
            this.getY() + 4,
            paletteColors.getFontColor(ColorType.DARK),
            9f,
            Fonts.REGULAR
        )
        nvgInstance.drawCenteredText(LegacyIcon.FOLDER, this.getX() + 8, this.getY() + 2.5f, Color.WHITE, 10f, Fonts.LEGACYICON)

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16f, 16f) && mouseButton == 0) {
            runAsync(Runnable {
                val image = FileUtils.selectImageFile()
                if (image != null) {
                    val fileManager: FileManager = Shindo.getInstance().fileManager
                    val cacheDir = File(fileManager.cacheDir, "custom-image")

                    fileManager.createDir(cacheDir)

                    val newImage = File(cacheDir, image.getName())

                    try {
                        FileUtils.copyFile(image, newImage)
                    } catch (e: IOException) {
                    }

                    imageSetting.setImage(newImage)
                }
            })
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
