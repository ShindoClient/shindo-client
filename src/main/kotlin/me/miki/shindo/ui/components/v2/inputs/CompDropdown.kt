package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.combo.Option
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.style.CompControlVariant
import me.miki.shindo.ui.components.v2.style.CompStyleResolver
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils

class CompDropdown : Component {

    private val setting: ComboSetting
    private var width: Float
    private var open: Boolean = false
    private var openUp: Boolean = false

    private val openAnimation = SimpleAnimation()
    private val hoverAnimation = SimpleAnimation()
    private val optionHoverAnimations = HashMap<Int, SimpleAnimation>()

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

    private val targetDropdownHeight: Float
        get() = LIST_PADDING * 2f + getOptionCount().coerceAtLeast(0) * OPTION_HEIGHT

    private fun getOptionCount(): Int = setting.getOptions().size

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val paletteColors = palette
        val accent = accent

        val x = getX()
        val y = getY()
        val controlHeight = CONTROL_HEIGHT
        val optionCount = getOptionCount()

        val controlHovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, controlHeight)
        hoverAnimation.setAnimation(if (controlHovered) 1.0f else 0.0f, 14.0)
        openAnimation.setAnimation(if (open && optionCount > 0) 1.0f else 0.0f, 16.0)

        val targetHeight = targetDropdownHeight
        val currentHeight = targetHeight * openAnimation.getValue()
        super.setWidth(width)
        super.setHeight(controlHeight + if (currentHeight > 0f) currentHeight + LIST_GAP else 0f)

        val baseBg = CompStyleResolver.resolveControlBase(CompControlVariant.SECONDARY, paletteColors, accent)
        val hoverBg = CompStyleResolver.resolveControlHover(CompControlVariant.SECONDARY, paletteColors, accent)
        val controlBg = ColorUtils.interpolateColor(baseBg, hoverBg, hoverAnimation.getValue().toDouble())
        val outlineColor = ColorUtils.interpolateColor(
            ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 54),
            ColorUtils.applyAlpha(accent.getColor1(), 112),
            (hoverAnimation.getValue() * 0.9f + openAnimation.getValue() * 0.6f).toDouble().coerceAtMost(1.0)
        )

        nvgInstance.drawRoundedRect(x, y, width, controlHeight, 6f, controlBg)
        nvgInstance.drawOutlineRoundedRect(x, y, width, controlHeight, 6f, 1f, outlineColor)

        val selected = setting.getOption()
        val selectedText = selected?.name ?: "-"
        val text = nvgInstance.getLimitText(
            selectedText,
            8.5f,
            Fonts.MEDIUM,
            (width - 28f).coerceAtLeast(24f)
        )
        val textHeight = nvgInstance.getTextHeight(text, 8.5f, Fonts.MEDIUM)
        nvgInstance.drawText(
            text,
            x + 8f,
            y + controlHeight / 2f - textHeight / 2f + 0.5f,
            paletteColors.getFontColor(ColorType.DARK),
            8.5f,
            Fonts.MEDIUM
        )

        val downAlpha = ((1f - openAnimation.getValue()) * 255f).toInt().coerceIn(0, 255)
        val upAlpha = (openAnimation.getValue() * 255f).toInt().coerceIn(0, 255)
        nvgInstance.drawText(
            LegacyIcon.CHEVRON_DOWN,
            x + width - 14f,
            y + 4f,
            paletteColors.getFontColor(ColorType.DARK, downAlpha),
            10f,
            Fonts.LEGACYICON
        )
        nvgInstance.drawText(
            LegacyIcon.CHEVRON_UP,
            x + width - 14f,
            y + 4f,
            paletteColors.getFontColor(ColorType.DARK, upAlpha),
            10f,
            Fonts.LEGACYICON
        )

        if (currentHeight > 0.5f && optionCount > 0) {
            val listX = x
            val listY = if (openUp) y - currentHeight - LIST_GAP else y + controlHeight + LIST_GAP

            nvgInstance.drawRoundedRect(
                listX,
                listY,
                width,
                currentHeight,
                6f,
                ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.DARK), 236)
            )
            nvgInstance.drawOutlineRoundedRect(
                listX,
                listY,
                width,
                currentHeight,
                6f,
                1f,
                ColorUtils.applyAlpha(accent.getColor1(), (86 * openAnimation.getValue()).toInt())
            )

            nvgInstance.save()
            nvgInstance.scissor(listX + 1f, listY + 1f, width - 2f, (currentHeight - 2f).coerceAtLeast(0f))

            val options: List<Option> = setting.getOptions()
            val selectedOption = setting.getOption()
            for (i in options.indices) {
                val option = options[i]
                val optionY = listY + LIST_PADDING + i * OPTION_HEIGHT
                val optionHeight = OPTION_HEIGHT - 2f
                val hovered = MouseUtils.isInside(mouseX, mouseY, listX + 2f, optionY, width - 4f, optionHeight)
                val hoverAnim = optionHoverAnimations.getOrPut(i) { SimpleAnimation() }
                hoverAnim.setAnimation(if (hovered) 1.0f else 0.0f, 14.0)

                if (option == selectedOption) {
                    nvgInstance.drawRoundedRect(
                        listX + 2f,
                        optionY,
                        width - 4f,
                        optionHeight,
                        4f,
                        ColorUtils.applyAlpha(accent.getColor1(), 88)
                    )
                } else if (hoverAnim.getValue() > 0.01f) {
                    nvgInstance.drawRoundedRect(
                        listX + 2f,
                        optionY,
                        width - 4f,
                        optionHeight,
                        4f,
                        ColorUtils.applyAlpha(
                            paletteColors.getBackgroundColor(ColorType.NORMAL),
                            (hoverAnim.getValue() * 178f).toInt()
                        )
                    )
                }

                val textColor = if (option == selectedOption) {
                    paletteColors.getFontColor(ColorType.DARK)
                } else {
                    paletteColors.getFontColor(ColorType.NORMAL)
                }
                val optionText =
                    nvgInstance.getLimitText(option.name, 8f, Fonts.REGULAR, (width - 20f).coerceAtLeast(20f))
                val optionTextHeight = nvgInstance.getTextHeight(optionText, 8f, Fonts.REGULAR)
                nvgInstance.drawText(
                    optionText,
                    listX + 8f,
                    optionY + optionHeight / 2f - optionTextHeight / 2f,
                    textColor,
                    8f,
                    Fonts.REGULAR
                )
            }

            nvgInstance.restore()
        } else if (!open) {
            optionHoverAnimations.clear()
        }

        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        val controlHeight = CONTROL_HEIGHT
        val listHeight = targetDropdownHeight * openAnimation.getValue()
        val listY = if (openUp) getY() - listHeight - LIST_GAP else getY() + controlHeight + LIST_GAP

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), width, controlHeight)) {
            open = !open
            return
        }

        if (open && listHeight > 1f && MouseUtils.isInside(mouseX, mouseY, getX(), listY, width, listHeight)) {
            selectOptionAt(mouseX, mouseY, listY)
            open = false
            return
        }

        open = false
    }

    private fun selectOptionAt(mouseX: Int, mouseY: Int, listY: Float) {
        val options = setting.getOptions()
        if (options.isEmpty()) return
        if (!MouseUtils.isInside(mouseX, mouseY, getX(), listY, width, targetDropdownHeight)) return

        val relativeY = mouseY - (listY + LIST_PADDING)
        val index = (relativeY / OPTION_HEIGHT).toInt()
        if (index in options.indices) {
            setting.setOption(options[index])
        }
    }

    companion object {
        private const val CONTROL_HEIGHT = 20f
        private const val OPTION_HEIGHT = 18f
        private const val LIST_PADDING = 4f
        private const val LIST_GAP = 4f
    }
}
