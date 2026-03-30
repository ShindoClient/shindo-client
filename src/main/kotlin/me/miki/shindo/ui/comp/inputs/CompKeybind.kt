package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.impl.KeybindSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard
import java.awt.Color


class CompKeybind : Comp {

    private val setting: KeybindSetting
    private var width: Float
    private var binding = false

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

        val info = if (binding) "Binding..." else Keyboard.getKeyName(setting.getKeyCode())

        nvgInstance.drawGradientRoundedRect(
            this.getX(),
            this.getY(),
            width,
            16f,
            4f,
            accentColor.getColor1(),
            accentColor.getColor2()
        )

        nvgInstance.drawCenteredText(info, this.getX() + (width / 2), this.getY() + 5f, Color(255, 255, 255), 8f, Fonts.REGULAR)

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), width, 16f) && mouseButton == 0) {
            binding = !binding;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if(binding) {

            if(keyCode == Keyboard.KEY_ESCAPE) {
                setting.setKeyCode(Keyboard.KEY_NONE);
                binding = false;
                return;
            }

            setting.setKeyCode(keyCode);
            binding = false;
        }

        super.keyTyped(typedChar, keyCode)
    }

    fun isBinding(): Boolean {
        return binding
    }
}
