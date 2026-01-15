package me.miki.shindo.ui.frame.component

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

/**
 * Componente de header do frame.
 * Exibe o título e botão de close.
 */
class FrameHeader(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 400f,
    title: String = ""
) : Comp(x, y) {
    
    private var titleText: String = title
    private var closable: Boolean = true
    private var onCloseCallback: (() -> Unit)? = null
    private var headerHeight: Float = 32f
    private var closeButtonSize: Float = 20f
    private var closeButtonPadding: Float = 6f
    
    // Cache
    private var _nvg: NanoVGManager? = null
    private var _palette: me.miki.shindo.management.color.palette.ColorPalette? = null
    
    private val nvg: NanoVGManager
        get() = _nvg ?: Shindo.getInstance().nanoVGManager!!.also { _nvg = it }
    
    private val palette: me.miki.shindo.management.color.palette.ColorPalette
        get() = _palette ?: Shindo.getInstance().colorManager.palette.also { _palette = it }
    
    init {
        setWidth(width)
        setHeight(headerHeight)
    }
    
    fun setTitle(title: String): FrameHeader {
        this.titleText = title
        return this
    }
    
    fun setClosable(closable: Boolean): FrameHeader {
        this.closable = closable
        return this
    }
    
    fun setOnClose(callback: (() -> Unit)?): FrameHeader {
        this.onCloseCallback = callback
        return this
    }
    
    fun setHeaderHeight(height: Float): FrameHeader {
        this.headerHeight = height
        setHeight(height)
        return this
    }
    
    fun getTitle(): String = titleText
    fun isClosable(): Boolean = closable
    
    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        
        val nvgInstance = nvg
        val paletteColors = palette
        
        // Desenha fundo do header
        val headerBg = ColorUtils.applyAlpha(
            paletteColors.getBackgroundColor(ColorType.MID),
            250
        )
        nvgInstance.drawRoundedRectVarying(
            getX(),
            getY(),
            getWidth(),
            getHeight(),
            12f, // top-left
            12f, // top-right
            0f,  // bottom-right
            0f,  // bottom-left
            headerBg
        )
        
        // Desenha título
        val titleX = getX() + 14f
        val titleY = getY() + getHeight() / 2f
        nvgInstance.drawText(
            titleText,
            titleX,
            titleY,
            paletteColors.getFontColor(ColorType.NORMAL),
            10.5f,
            Fonts.MEDIUM
        )
        
        // Desenha botão de close se for closable
        if (closable) {
            drawCloseButton(nvgInstance, paletteColors, mouseX, mouseY)
        }
    }
    
    private fun drawCloseButton(
        nvg: NanoVGManager,
        palette: me.miki.shindo.management.color.palette.ColorPalette,
        mouseX: Int,
        mouseY: Int
    ) {
        val closeX = getX() + getWidth() - closeButtonSize - closeButtonPadding
        val closeY = getY() + (getHeight() - closeButtonSize) / 2f
        val hovered = MouseUtils.isInside(mouseX, mouseY, closeX, closeY, closeButtonSize, closeButtonSize)
        
        val bgColor = if (hovered) {
            ColorUtils.applyAlpha(
                palette.getBackgroundColor(ColorType.NORMAL),
                200
            )
        } else {
            ColorUtils.applyAlpha(
                palette.getBackgroundColor(ColorType.NORMAL),
                150
            )
        }
        
        nvg.drawRoundedRect(closeX, closeY, closeButtonSize, closeButtonSize, 4f, bgColor)
        
        // Ícone de close
        val iconSize = 10f
        val iconX = closeX + (closeButtonSize - iconSize) / 2f
        val iconY = closeY + (closeButtonSize - iconSize) / 2f
        nvg.drawText(
            LegacyIcon.X_CIRCLE,
            iconX,
            iconY,
            if (hovered) Color.WHITE else ColorUtils.applyAlpha(Color.WHITE, 200),
            iconSize,
            Fonts.LEGACYICON
        )
    }
    
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }
        
        if (closable) {
            val closeX = getX() + getWidth() - closeButtonSize - closeButtonPadding
            val closeY = getY() + (getHeight() - closeButtonSize) / 2f
            
            if (MouseUtils.isInside(mouseX, mouseY, closeX, closeY, closeButtonSize, closeButtonSize)) {
                onCloseCallback?.invoke()
                return
            }
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}
