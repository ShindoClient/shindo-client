package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.SoundSetting
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color
import java.io.File
import java.io.IOException

class CompSoundSelect : Component {
    private val soundSetting: SoundSetting

    constructor(x: Float, y: Float, soundSetting: SoundSetting) : super(x, y) {
        this.soundSetting = soundSetting
        setWidth(16F)
        setHeight(16F)
    }

    constructor(soundSetting: SoundSetting) : super(0f, 0f) {
        this.soundSetting = soundSetting
        setWidth(16F)
        setHeight(16F)
    }

    override fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val nvgInstance = nvg
        val accentColor = accent
        val paletteColors = palette

        val name =
            if (soundSetting.getSound() == null) TranslateText.NONE.getText() else soundSetting.getSound()!!.getName()
        val nameWidth = nvgInstance.getTextWidth(name, 9f, Fonts.REGULAR)

        nvgInstance.drawGradientRoundedRect(
            this.getX(),
            this.getY(),
            16f,
            16f,
            4f,
            accentColor.getColor1(),
            accentColor.getColor2(),
        )
        nvgInstance.drawText(
            name,
            this.getX() - nameWidth - 5,
            this.getY() + 4,
            paletteColors.getFontColor(ColorType.DARK),
            9f,
            Fonts.REGULAR,
        )
        nvgInstance.drawCenteredText(
            LegacyIcon.FOLDER,
            this.getX() + 8,
            this.getY() + 2.5f,
            Color.WHITE,
            10f,
            Fonts.LEGACYICON,
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), 16f, 16f) && mouseButton == 0) {
            TaskExecutor.runAsync(ThreadPoolType.IO) {
                val sound = FileUtils.selectSoundFile()
                if (sound != null) {
                    val fileManager = Shindo.getInstance().getFileManager()
                    val cacheDir = File(fileManager.cacheDir, "custom-sound")
                    fileManager.createDir(cacheDir)

                    val newImage = File(cacheDir, sound.name)
                    try {
                        FileUtils.copyFile(sound, newImage)
                    } catch (_: IOException) {
                    }

                    soundSetting.setSound(newImage)
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
