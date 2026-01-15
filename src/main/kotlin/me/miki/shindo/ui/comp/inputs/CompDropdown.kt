package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.combo.Option
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import java.awt.Color

class CompDropdown : Comp {

    private val setting: ComboSetting
    private var width: Float
    private var open: Boolean = false
    private var openUp: Boolean = false

    constructor(x: Float, y: Float, width: Float, setting: ComboSetting) : super(x, y) {
        this.setting = setting
        this.width = width
        super.setWidth(width)
        super.setHeight(CONTROL_HEIGHT)
    }

    constructor(width: Float, setting: ComboSetting) : this(0f, 0f, width, setting)

    override fun getWidth(): Float = width

    override fun setWidth(width: Float) {
        this.width = width
        super.setWidth(width)
    }
    fun setOpenUp(openUp: Boolean) {
        this.openUp = openUp
    }

    val controlHeight: Float
        get() = CONTROL_HEIGHT

    fun isOpen(): Boolean = open

    fun setOpen(open: Boolean) {
        this.open = open
    }

    val dropdownHeight: Float
        get() = if (open) LIST_PADDING * 2f + getOptionCount().coerceAtLeast(0) * OPTION_HEIGHT else 0f

    private fun getOptionCount(): Int = setting.getOptions().size ?: 0

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val accent = accent
        val paletteColors = palette

        val controlHeight = CONTROL_HEIGHT
        val dropdownHeight = dropdownHeight

        super.setWidth(width)
        super.setHeight(controlHeight + dropdownHeight)

        val x = getX()
        val y = getY()

        nvgInstance.drawGradientRoundedRect(x, y, width, controlHeight, 5f, accent.color1, accent.color2)

        val label = setting.getOption()!!.name ?: "None"
        nvgInstance.drawText(label, x + 8f, y + 6f, Color.WHITE, 8.5f, Fonts.MEDIUM)

        val arrow = if (open) LegacyIcon.CHEVRON_UP else LegacyIcon.CHEVRON_DOWN
        nvgInstance.drawText(arrow, x + width - 16f, y + 4f, Color.WHITE, 10f, Fonts.LEGACYICON)

        if (open && getOptionCount() > 0) {
            val listX = x
            val listHeight = dropdownHeight
            val listY = if (openUp) y - listHeight - 4f else y + controlHeight + 4f

            nvgInstance.drawRoundedRect(
                listX,
                listY,
                width,
                listHeight,
                5f,
                ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.DARK), 240)
            )

            val options: List<Option> = setting.getOptions()
            for (i in options.indices) {
                val option = options[i]
                val optionY = listY + LIST_PADDING + i * OPTION_HEIGHT
                val optionHeight = OPTION_HEIGHT - 2f
                val hovered = MouseUtils.isInside(mouseX, mouseY, listX + 2f, optionY, width - 4f, optionHeight)
                if (hovered) {
                    nvgInstance.drawRoundedRect(
                        listX + 2f,
                        optionY,
                        width - 4f,
                        optionHeight,
                        4f,
                        ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 220)
                    )
                }

                val textColor = if (option == setting.getOption()) {
                    paletteColors.getFontColor(ColorType.DARK)
                } else {
                    paletteColors.getFontColor(ColorType.NORMAL)
                }
                nvgInstance.drawText(option.name, listX + 8f, optionY + 5f, textColor, 8f, Fonts.REGULAR)
            }
        }

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        val controlHeight = CONTROL_HEIGHT
        val listX = getX()
        val listHeight = dropdownHeight
        val listY = if (openUp) getY() - listHeight - 4f else getY() + controlHeight + 4f
        val listWidth = width

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), width, controlHeight)) {
            open = !open
            return
        }

        if (open && MouseUtils.isInside(mouseX, mouseY, listX, listY, listWidth, listHeight)) {
            selectOptionAt(mouseX, mouseY)
            open = false
            return
        }

        open = false
    }

    private fun selectOptionAt(mouseX: Int, mouseY: Int) {
        val options: List<Option> = setting.getOptions()
        if (options.isEmpty()) return

        val optionX = getX() + 2f
        val listHeight = dropdownHeight
        var optionY = (if (openUp) getY() - listHeight - 4f else getY() + CONTROL_HEIGHT + 4f) + LIST_PADDING

        for (option in options) {
            if (MouseUtils.isInside(mouseX, mouseY, optionX, optionY, width - 4f, OPTION_HEIGHT - 2f)) {
                setting.setOption(option)
                break
            }
            optionY += OPTION_HEIGHT
        }
    }

    companion object {
        private const val CONTROL_HEIGHT = 20F
        private const val OPTION_HEIGHT = 18F
        private const val LIST_PADDING = 4F
    }
}
