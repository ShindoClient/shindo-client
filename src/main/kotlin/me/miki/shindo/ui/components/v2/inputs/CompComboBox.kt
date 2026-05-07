package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.MathUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color


class CompComboBox : Component {
    private val changeAnimation = SimpleAnimation()
    private val setting: ComboSetting
    private var width: Float
    private var changeDirection: Int

    constructor(x: Float, y: Float, width: Float, setting: ComboSetting) : super(x, y) {
        this.width = width
        this.setting = setting
        this.changeDirection = 1
        this.changeAnimation.setValue(1f)
        setWidth(width)
        super.setHeight(CONTROL_HEIGHT)
    }

    constructor(width: Float, setting: ComboSetting) : super(0f, 0f) {
        this.width = width
        this.setting = setting
        this.changeDirection = 1
        this.changeAnimation.setValue(1f)
        setWidth(width)
        super.setHeight(CONTROL_HEIGHT)
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

        nvgInstance.drawGradientRoundedRect(
            this.getX(),
            this.getY(),
            width,
            16f,
            4f,
            accentColor.getColor1(),
            accentColor.getColor2()
        )

        nvgInstance.drawCenteredText(
            setting.getOption()!!.name,
            this.getX() + (width / 2f) + ((changeDirection - changeAnimation.getValue()) * 22f),
            this.getY() + 5f,
            Color(255, 255, 255, (MathUtils.abs(changeAnimation.getValue() * 255)).toInt()),
            8f,
            Fonts.REGULAR
        )

        nvgInstance.drawText("<", this.getX() + 4, this.getY() + 4f, Color.WHITE, 10f, Fonts.REGULAR)
        nvgInstance.drawText(">", this.getX() + width - 10, this.getY() + 4f, Color.WHITE, 10f, Fonts.REGULAR)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val max = setting.getOptions().size
        var modeIndex = setting.getOptions().indexOf(setting.getOption())

        if (mouseButton == 0) {
            if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16f, 16f)) {
                changeAnimation.setValue(0f)

                if (modeIndex > 0) {
                    modeIndex--
                } else {
                    modeIndex = max - 1
                }

                changeDirection = 1
                setting.setOption(setting.getOptions()[modeIndex])
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + width - 16f, this.getY(), 16f, 16f)) {
                changeAnimation.setValue(0f)

                if (modeIndex < max - 1) {
                    modeIndex++
                } else {
                    modeIndex = 0
                }

                changeDirection = -1
                setting.setOption(setting.getOptions()[modeIndex])
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    companion object {
        private const val CONTROL_HEIGHT = 16f
        private const val BUTTON_SIZE = 16f
    }
}
