package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard
import java.awt.Color

class CompAutoTextKeybind(
    width: Float,
    private val getKeyCode: () -> Int,
    private val setKeyCode: (Int) -> Unit
) : Comp(0f, 0f) {

    private var widthInternal = width
    private var binding = false

    fun isBinding(): Boolean = binding

    init {
        setWidth(width)
        setHeight(16f)
    }

    fun setPosition(x: Float, y: Float) {
        setX(x)
        setY(y)
    }

    override fun getWidth(): Float = widthInternal

    override fun setWidth(width: Float) {
        widthInternal = width
        super.setWidth(width)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val keyName = if (binding) "..." else Keyboard.getKeyName(getKeyCode.invoke())
        val nvgInstance = nvg

        nvgInstance.drawRoundedRect(
            getX(),
            getY(),
            widthInternal,
            16f,
            4f,
            Color(255, 255, 255, 30)
        )
        nvgInstance.drawCenteredText(
            keyName,
            getX() + widthInternal / 2f,
            getY() + 5f,
            Color.WHITE,
            8f,
            Fonts.REGULAR
        )

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), widthInternal, 16f)) {
            binding = !binding
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!binding) {
            super.keyTyped(typedChar, keyCode)
            return
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            setKeyCode.invoke(Keyboard.KEY_NONE)
            binding = false
            return
        }

        setKeyCode.invoke(keyCode)
        binding = false
        super.keyTyped(typedChar, keyCode)
    }
}
