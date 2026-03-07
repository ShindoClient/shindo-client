package me.miki.shindo.gui.mainmenu.widget

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.gui.widget.WidgetButtonBase
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.types.Color
import org.lwjgl.nanovg.NanoVG


class WidgetPlayButton(text: TranslateText, x: Float, y: Float, width: Float, height: Float, onClick: Runnable) : WidgetButtonBase(x, y, width, height, onClick) {
    private val text: TranslateText
    private val COLOR: Color = Color(0x78E0E0E0)

    override fun render(renderer: NanoVGManager, mouseX: Float, mouseY: Float) {
        super.render(renderer, mouseX, mouseY)
        if (isHovered) hoverAnimation.forceFinish()
        COLOR.setAlpha(0.3f + hoverAnimation.linearValue * 0.2f)
        Blur.drawBlur(getBounds(), 4.5f)
        renderer.drawRoundedRect(getBounds(), 4.5f, COLOR.toARGB())
        renderer.drawBlurredText(
            text.getText(),
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f,
            -0x1000000,
            12f,
            9.5f,
            NanoVG.NVG_ALIGN_CENTER or NanoVG.NVG_ALIGN_MIDDLE,
            Fonts.SEMIBOLD
        )
        renderer.drawCenteredText(
            text.getText(),
            getX() + getWidth() / 2f,
            getY() + getHeight() / 2f,
            -1,
            9.5f,
            Fonts.SEMIBOLD
        )
    }

    init {
        this.text = text
    }
}