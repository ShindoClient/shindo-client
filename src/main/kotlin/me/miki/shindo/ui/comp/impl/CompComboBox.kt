package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.MathUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

class CompComboBox : Comp {
    private val changeAnimation = SimpleAnimation()
    private val setting: ComboSetting
    private var width: Float

    private var changeDirection: Int

    constructor(x: Float, y: Float, width: Float, setting: ComboSetting) : super(x, y) {
        this.width = width
        this.setting = setting
        this.changeDirection = 1
        this.changeAnimation.value = 1f
        setWidth(width)
        super.setHeight(16f)
    }

    constructor(width: Float, setting: ComboSetting) : super(0f, 0f) {
        this.width = width
        this.setting = setting
        this.changeDirection = 1
        this.changeAnimation.value = 1f
        setWidth(width)
        super.setHeight(16f)
    }

    override fun getWidth(): Float = width

    override fun setWidth(width: Float) {
        this.width = width
        super.setWidth(width)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accentColor = accent

        changeAnimation.setAnimation(changeDirection.toFloat(), 16.0)

        nvgInstance.drawGradientRoundedRect(getX(), getY(), width, 16f, 4f, accentColor.color1, accentColor.color2)

        nvgInstance.drawCenteredText(
            setting.getOption()!!.name,
            getX() + width / 2 + (changeDirection - changeAnimation.value) * 22,
            getY() + 5f,
            Color(255, 255, 255, MathUtils.abs((changeAnimation.value * 255).toDouble()).toInt()),
            8f,
            Fonts.REGULAR
        )

        nvgInstance.drawText("<", getX() + 4, getY() + 4f, Color.WHITE, 10f, Fonts.REGULAR)
        nvgInstance.drawText(">", getX() + width - 10, getY() + 4f, Color.WHITE, 10f, Fonts.REGULAR)

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val max = setting.getOptions().size
        var modeIndex = setting.getOptions().indexOf(setting.getOption())

        if (mouseButton == 0) {
            if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), 16f, 16f)) {
                changeAnimation.value = 0f
                modeIndex = if (modeIndex > 0) modeIndex - 1 else max - 1
                changeDirection = 1
                setting.setOption(setting.getOptions()[modeIndex])
            }

            if (MouseUtils.isInside(mouseX, mouseY, getX() + width - 16, getY(), 16f, 16f)) {
                changeAnimation.value = 0f
                modeIndex = if (modeIndex < max - 1) modeIndex + 1 else 0
                changeDirection = -1
                setting.setOption(setting.getOptions()[modeIndex])
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
