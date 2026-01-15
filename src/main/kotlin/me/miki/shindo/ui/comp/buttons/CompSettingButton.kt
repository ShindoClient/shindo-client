package me.miki.shindo.ui.comp.buttons

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.templates.CompInteractive
import me.miki.shindo.utils.ColorUtils
import java.awt.Color

/**
 * Componente compartilhado tipo card usado em telas de configurações para renderizar título,
 * descrição e controle opcional à direita (slider, toggle, keybind, etc.).
 * 
 * Agora usa CompInteractive para melhor gerenciamento de interações.
 */
class CompSettingButton : CompInteractive {
    private val titleSupplier: () -> String
    private val descriptionSupplier: () -> String

    private var statusSupplier: (() -> String)? = null
    private var statusColorSupplier: (() -> Color)? = null
    private var trailingComp: Comp? = null

    private var paddingLeft: Float = 16f
    private var paddingRight: Float = 18f
    private var paddingVertical: Float = 15f
    private var drawShadow: Boolean = true
    private var shadowStrength: Float = 6f
    private var shadowRadius: Float = 8f
    
    constructor(x: Float, y: Float, width: Float, titleSupplier: () -> String, descriptionSupplier: () -> String) :
        super(x, y) {
        this.titleSupplier = titleSupplier
        this.descriptionSupplier = descriptionSupplier
        setWidth(width);
        setHeight(DEFAULT_HEIGHT);
    }

    constructor(width: Float, titleSupplier: () -> String, descriptionSupplier: () -> String) :
        this(0f, 0f, width, titleSupplier, descriptionSupplier)

    fun onClickAction(onClick: () -> Unit): CompSettingButton {
        this.onClick = onClick
        return this
    }
    
    @Deprecated("Use onClickAction instead", ReplaceWith("onClickAction(onClick)"))
    fun onClick(onClick: () -> Unit): CompSettingButton = onClickAction(onClick)

    fun trailing(comp: Comp): CompSettingButton {
        this.trailingComp = comp
        return this
    }

    fun status(textSupplier: () -> String, colorSupplier: () -> Color): CompSettingButton {
        this.statusSupplier = textSupplier
        this.statusColorSupplier = colorSupplier
        return this
    }

    fun setPaddingLeft(paddingLeft: Float)         { this.paddingLeft = paddingLeft         }
    fun setPaddingRight(paddingRight: Float)       { this.paddingRight = paddingRight       }
    fun setPaddingVertical(paddingVertical: Float) { this.paddingVertical = paddingVertical }
    fun setDrawShadow(drawShadow: Boolean)         { this.drawShadow = drawShadow           }
    fun setShadowStrength(shadowStrength: Float)   { this.shadowStrength = shadowStrength   }
    fun setShadowRadius(shadowRadius: Float)       { this.shadowRadius = shadowRadius       }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accentColors = accent

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()

        val base = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), if (hovered) 210 else 188)
        val overlayStart = ColorUtils.applyAlpha(accentColors.color1, if (hovered) 62 else 38)
        val overlayEnd = ColorUtils.applyAlpha(accentColors.color2, if (hovered) 62 else 38)

        if (drawShadow) {
            nvgInstance.drawShadow(x, y, width, height, shadowRadius, shadowStrength.toInt())
        }

        nvgInstance.drawRoundedRect(x, y, width, height, DEFAULT_RADIUS, base)
        nvgInstance.drawGradientRoundedRect(x, y, width, height, DEFAULT_RADIUS, overlayStart, overlayEnd)

        var availableTextWidth = width - paddingLeft - paddingRight
        trailingComp?.let {
            availableTextWidth -= it.getWidth().coerceAtLeast(0f)
            availableTextWidth -= 12f
        }

        val titleY = y + paddingVertical - 4f
        val descriptionY = titleY + 13f

        val title = nvgInstance.getLimitText(titleSupplier.invoke(), TEXT_TITLE_SIZE, Fonts.MEDIUM, availableTextWidth.coerceAtLeast(48f))
        var description = descriptionSupplier.invoke()
        description = if (!"null".equals(description, ignoreCase = true)) {
            nvgInstance.getLimitText(description, TEXT_DESCRIPTION_SIZE, Fonts.REGULAR, availableTextWidth.coerceAtLeast(48f))
        } else {
            ""
        }

        nvgInstance.drawText(title, x + paddingLeft, titleY, paletteColors.getFontColor(ColorType.DARK), TEXT_TITLE_SIZE, Fonts.MEDIUM)
        if (description.isNotEmpty()) {
            nvgInstance.drawText(description, x + paddingLeft, descriptionY, paletteColors.getFontColor(ColorType.NORMAL), TEXT_DESCRIPTION_SIZE, Fonts.REGULAR)
        }

        if (statusSupplier != null && statusColorSupplier != null) {
            val status = statusSupplier?.invoke()
            if (!status.isNullOrEmpty()) {
                val statusY = y + height - paddingVertical + 4f
                nvgInstance.drawText(status, x + paddingLeft, statusY, statusColorSupplier!!.invoke(), TEXT_STATUS_SIZE, Fonts.MEDIUM)
            }
        }

        trailingComp?.let {
            val trailingX = x + width - paddingRight - it.getWidth()
            val trailingY = y + (height - it.getHeight()) / 2f
            it.setX(trailingX)
            it.setY(trailingY)
            it.draw(mouseX, mouseY, partialTicks)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible()) return

        var insideTrailing = false
        trailingComp?.let {
            insideTrailing = isHovered(mouseX, mouseY) && 
                mouseX >= it.getX() && mouseX <= it.getX() + it.getWidth() &&
                mouseY >= it.getY() && mouseY <= it.getY() + it.getHeight()
        }

        if (!insideTrailing && mouseButton == 0) {
            trailingComp?.mouseClicked(mouseX, mouseY, mouseButton)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible()) return
        trailingComp?.mouseReleased(mouseX, mouseY, mouseButton)
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!isVisible()) return
        trailingComp?.keyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    companion object {
        private const val DEFAULT_HEIGHT = 52F
        private const val DEFAULT_RADIUS = 9F
        private const val TEXT_TITLE_SIZE = 10.5F
        private const val TEXT_DESCRIPTION_SIZE = 8.5F
        private const val TEXT_STATUS_SIZE = 8.5F
    }
}