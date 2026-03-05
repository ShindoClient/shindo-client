package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.impl.KeybindSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard

class CompKeybind : Comp {

    private val setting: KeybindSetting
    private var width: Float
    var isBinding: Boolean = false
        private set

    constructor(x: Float, y: Float, width: Float, setting: KeybindSetting) : super(x, y) {
        this.setting = setting
        this.width = width
        setWidth(width)
        setHeight(16F)
    }

    constructor(width: Float, setting: KeybindSetting) : super(0f, 0f) {
        this.setting = setting
        this.width = width
        setWidth(width)
        setHeight(16F)
    }

    override fun getWidth(): Float = width
    override fun setWidth(width: Float) {
        this.width = width
        super.setWidth(width)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accentColor = accent
        val paletteColors = palette

        val info = if (isBinding) "Binding..." else Keyboard.getKeyName(setting.getKeyCode())
        val hovered = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), width, 16f)

        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            width,
            16f,
            4f,
            ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 214)
        )
        nvgInstance.drawGradientRoundedRect(
            getX(),
            getY(),
            width,
            16f,
            4f,
            ColorUtils.applyAlpha(accentColor.getColor1(), if (isBinding) 185 else if (hovered) 128 else 98),
            ColorUtils.applyAlpha(accentColor.getColor2(), if (isBinding) 205 else if (hovered) 152 else 112)
        )
        nvgInstance.drawOutlineRoundedRect(
            getX(),
            getY(),
            width,
            16f,
            4f,
            1f,
            ColorUtils.applyAlpha(
                paletteColors.getFontColor(ColorType.NORMAL),
                if (isBinding) 172 else if (hovered) 132 else 104
            )
        )

        nvgInstance.drawCenteredText(
            info,
            getX() + width / 2,
            getY() + 5f,
            paletteColors.getFontColor(ColorType.DARK),
            8f,
            Fonts.REGULAR
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), width, 16f) && mouseButton == 0) {
            isBinding = !isBinding
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (isBinding) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                setting.setKeyCode(Keyboard.KEY_NONE)
                isBinding = false
                return
            }

            setting.setKeyCode(keyCode)
            isBinding = false
        }

        super.keyTyped(typedChar, keyCode)
    }
}
