package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.mods.ModManager
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.SearchUtils
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class ModuleCategory(parent: GuiModMenu) : Category(parent, TranslateText.MODULE, LegacyIcon.ARCHIVE, true, true) {

    private val settingScroll = Scroll()
    private val settingsPanel = SettingsPanel()
    private val moduleCardCache = ArrayList<ModuleCard>()
    private val categoryChips = ArrayList<FilterChip>()
    private val noColour = Color(0, 0, 0, 0)
    private var currentCategory: ModCategory = ModCategory.ALL
    private var openSetting = false
    private var settingAnimation: Animation = SmoothStepAnimation(260, 1.0)
    private var currentMod: Mod? = null
    private var moduleContentHeight = 0f

    override fun initGui() {
        currentCategory = ModCategory.ALL
        openSetting = false
        settingAnimation = SmoothStepAnimation(260, 1.0)
        settingAnimation.setValue(1.0)
        settingsPanel.clear()
    }

    override fun initCategory() {
        scroll.resetAll()
        openSetting = false
        settingAnimation = SmoothStepAnimation(260, 1.0)
        settingAnimation.setValue(1.0)
        settingsPanel.clear()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val modManager: ModManager = instance.modManager
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.palette
        val accentColor: AccentColor = colorManager.currentColor

        val scrollValue = scroll.getValue()
        val moduleColumns = resolveModuleColumns()
        val cardStyle = getCardStyle(moduleColumns)

        settingAnimation.setDirection(if (openSetting) Direction.BACKWARDS else Direction.FORWARDS)

        if (settingAnimation.isDone(Direction.FORWARDS)) {
            setCanClose(true)
            currentMod = null
            settingsPanel.clear()
        }

        nvg.save()
        nvg.translate(-(600f - (settingAnimation.getValue().toFloat() * 600f)), 0f)

        // Draw mod scene
        nvg.save()
        nvg.translate(0f, scrollValue)

        val chipBlockOffset = drawCategoryChips(nvg, palette, accentColor, scrollValue, mouseX, mouseY)
        rebuildModuleCards(modManager, chipBlockOffset, moduleColumns, false)

        for (card in moduleCardCache) {
            if (card.y + scrollValue + card.height > 0 && card.y + scrollValue < getHeight()) {
                val cardY = getY() + card.y
                    val style = cardStyle
                    val iconX = card.x + style.leftPadding
                    val iconY = cardY + (card.height - style.iconSize) / 2f

                    val hasSettings = modManager.getSettingsByMod(card.mod) != null
                    val toggleWidth = LIST_TOGGLE_WIDTH
                    val toggleHeight = LIST_TOGGLE_HEIGHT
                    val toggleX = card.x + card.width - style.settingsPadding - toggleWidth
                    val toggleY = cardY + (card.height - toggleHeight) / 2f
                    val settingsX = toggleX - LIST_TOGGLE_GAP - style.settingsSize
                    val settingsY = cardY + (card.height - style.settingsSize) / 2f
                    card.hasSettings = hasSettings
                    card.settingsX = settingsX
                    card.settingsY = settingsY + scrollValue
                    card.settingsSize = style.settingsSize
                    card.toggleX = toggleX
                    card.toggleY = toggleY + scrollValue
                    card.toggleWidth = toggleWidth
                    card.toggleHeight = toggleHeight

                    val textSpacing = if (moduleColumns == 3) 8f else 10f
                    val textX = iconX + style.iconSize + textSpacing
                    val textRight = if (hasSettings) {
                        settingsX - LIST_TOGGLE_GAP
                    } else {
                        toggleX - LIST_TOGGLE_GAP
                    }
                    if (hasSettings) {
                    }
                    val textWidth = max(80f, textRight - textX)

                    val hovered = MouseUtils.isInside(mouseX, mouseY, card.x, cardY + scrollValue, card.width, card.height) &&
                        !MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY + scrollValue, style.settingsSize, style.settingsSize) &&
                        !MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY + scrollValue, toggleWidth, toggleHeight)
                    card.mod.hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 18.toDouble())
                    val hoverProgress = card.mod.hoverAnimation.value

                    val settingsHover = MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY + scrollValue, style.settingsSize, style.settingsSize)
                    card.mod.settingsHoverAnimation.setAnimation(if (settingsHover) 1.0f else 0.0f, 18.toDouble())
                    val settingsHoverAnimation = card.mod.settingsHoverAnimation.value

                    val overlayAlpha = (18 + (hoverProgress * 26)).toInt()
                    val fillAlpha = (220 + (hoverProgress * 32)).toInt()
                    val outlineAlpha = (hoverProgress * 220).toInt()

                    nvg.drawRoundedRect(card.x, cardY, card.width, card.height, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), fillAlpha))
                    nvg.drawGradientRoundedRect(card.x, cardY, card.width, card.height, 8f, ColorUtils.applyAlpha(accentColor.color1, overlayAlpha), ColorUtils.applyAlpha(accentColor.color2, overlayAlpha))

                    if (outlineAlpha > 0) {
                        nvg.drawOutlineRoundedRect(card.x, cardY, card.width, card.height, 8f, 1.0f, ColorUtils.applyAlpha(accentColor.color2, outlineAlpha))
                    }

                    card.mod.animation.setAnimation(if (card.mod.isToggled()) 1.0f else 0.0f, 16.toDouble())
                    val toggleProgress = card.mod.animation.value

                    val icon = card.mod.getMenuIcon()
                    if (!icon.isNullOrEmpty()) {
                        nvg.drawCenteredText(
                            icon,
                            iconX + style.iconSize / 2f,
                            iconY + style.iconSize / 2f - LIST_ICON_FONT_OFFSET,
                            palette.getFontColor(ColorType.DARK),
                            LIST_ICON_FONT_SIZE,
                            Fonts.LEGACYICON
                        )
                    }

                    val modName = nvg.getLimitText(card.mod.getName(), 11.5f, Fonts.MEDIUM, textWidth)
                    nvg.drawText(modName, textX, cardY + 14f, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)

                    if (card.mod.isRestricted()) {
                        val warning = "Restricted on some servers"
                        val warningY = cardY + card.height - LIST_WARNING_BOTTOM_PADDING
                        nvg.drawText(LegacyIcon.INFO, textX, warningY - LIST_WARNING_ICON_OFFSET, Color(255, 180, 90), 8.5f, Fonts.LEGACYICON)
                        nvg.drawText(nvg.getLimitText(warning, 8f, Fonts.REGULAR, textWidth - 10f), textX + 10f, warningY, Color(255, 180, 90), 8f, Fonts.REGULAR)
                    }

                    val description = nvg.getLimitText(card.mod.getDescription(), 8.5f, Fonts.REGULAR, textWidth)
                    nvg.drawText(description, textX, cardY + 26f, palette.getFontColor(ColorType.NORMAL), 8.5f, Fonts.REGULAR)

                    if (hasSettings) {
                        nvg.drawRoundedRect(settingsX, settingsY, style.settingsSize, style.settingsSize, 5f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180))
                        nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + style.settingsSize / 2f - 1f, settingsY + style.settingsSize / 2f - 6f, palette.getFontColor(ColorType.DARK), 14f, Fonts.LEGACYICON)
                        nvg.drawGradientOutlineRoundedRect(
                            settingsX,
                            settingsY,
                            style.settingsSize,
                            style.settingsSize,
                            5f,
                            1.0f,
                            ColorUtils.applyAlpha(accentColor.color1, (settingsHoverAnimation * 255).toInt()),
                            ColorUtils.applyAlpha(accentColor.color2, (settingsHoverAnimation * 255).toInt())
                        )
                    }

                    val toggleRadius = toggleHeight / 2f
                    val toggleBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200)
                    nvg.drawRoundedRect(toggleX, toggleY, toggleWidth, toggleHeight, toggleRadius, toggleBase)
                    if (toggleProgress > 0f) {
                        nvg.drawGradientRoundedRect(
                            toggleX,
                            toggleY,
                            toggleWidth,
                            toggleHeight,
                            toggleRadius,
                            ColorUtils.applyAlpha(accentColor.color1, (toggleProgress * 255).toInt()),
                            ColorUtils.applyAlpha(accentColor.color2, (toggleProgress * 255).toInt())
                        )
                    }
                    val knobSize = toggleHeight - 6f
                    val knobX = toggleX + 3f + toggleProgress * (toggleWidth - knobSize - 6f)
                    val knobY = toggleY + 3f
                    nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, Color.WHITE)

            }
        }

        nvg.restore()
        nvg.drawVerticalGradientRect(getX() + 15f, getY().toFloat(), getWidth() - 30f, 12f, palette.getBackgroundColor(ColorType.NORMAL), noColour) // top
        nvg.drawVerticalGradientRect(getX() + 15f, getY() + getHeight() - 12f, getWidth() - 30f, 12f, noColour, palette.getBackgroundColor(ColorType.NORMAL)) // bottom
        nvg.restore()

        // Draw mod setting scene
        nvg.save()
        nvg.translate(settingAnimation.getValue().toFloat() * 600f, 0f)

        val activeMod = currentMod
        if (activeMod != null) {
            if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) {
                settingScroll.onScroll()
                settingScroll.onAnimation()
            }

            settingsPanel.setLayoutMode(InternalSettingsMod.instance.settingsLayoutMode)

            val headerX = getX() + 15f
            val headerY = getY() + 15f
            val headerWidth = getWidth() - 30f
            val headerHeight = getHeight() - 30f

            nvg.drawShadow(headerX, headerY, headerWidth, headerHeight, 12f, 7)
            nvg.drawRoundedRect(headerX, headerY, headerWidth, headerHeight, 12f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
            nvg.drawRoundedRect(headerX + 1f, headerY + 1f, headerWidth - 2f, headerHeight - 2f, 11f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

            nvg.drawText(LegacyIcon.CHEVRON_LEFT, headerX + 10, headerY + 8, palette.getFontColor(ColorType.DARK), 13f, Fonts.LEGACYICON)
            nvg.drawText(activeMod.getName(), headerX + 27, headerY + 9, palette.getFontColor(ColorType.DARK), 13f, Fonts.MEDIUM)
            nvg.drawText(LegacyIcon.REFRESH, headerX + headerWidth - 24, headerY + 7.5f, palette.getFontColor(ColorType.DARK), 13f, Fonts.LEGACYICON)

            val contentX = getX() + 25f
            val contentY = headerY + 32f
            val contentWidth = getWidth() - 50f
            val viewportHeight = headerHeight - 47f

            nvg.save()
            nvg.scissor(headerX + 5f, contentY - 5f, headerWidth - 10f, viewportHeight + 10f)
            settingsPanel.draw(mouseX, mouseY, partialTicks, contentX, contentY, contentWidth, viewportHeight, nvg, palette, settingScroll)
            nvg.restore()
        }

        nvg.restore()

        val viewportHeight = getHeight() - 26f
        scroll.maxScroll = max(0f, moduleContentHeight - viewportHeight)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val modManager = instance.modManager

        if (!openSetting && mouseButton == 0) {
            for (chip in categoryChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click()
                    return
                }
            }
        }

        if (!openSetting) {
            val iconLayout = false
            val cardStyle = getCardStyle(resolveModuleColumns())

            for (card in moduleCardCache) {
                val cardY = getY() + card.y + scroll.getValue()

                if (!MouseUtils.isInside(mouseX, mouseY, card.x, cardY, card.width, card.height)) {
                    continue
                }

                if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat()) && mouseButton == 0) {
                    if (iconLayout && card.hasSettings && MouseUtils.isInside(mouseX, mouseY, card.settingsX, card.settingsY, card.settingsSize, card.settingsSize)) {
                        val settings: ArrayList<Setting>? = modManager.getSettingsByMod(card.mod)
                        if (settings != null) {
                            settingsPanel.buildEntries(settings)
                            settingScroll.resetAll()
                            currentMod = card.mod
                            openSetting = true
                            setCanClose(false)
                        }
                        continue
                    }

                    if (!iconLayout) {
                        val settingsX = card.settingsX
                        val settingsY = card.settingsY
                        val toggleX = card.toggleX
                        val toggleY = card.toggleY

                        if (MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, cardStyle.settingsSize, cardStyle.settingsSize) && !openSetting) {
                            val settings: ArrayList<Setting>? = modManager.getSettingsByMod(card.mod)
                            if (settings != null) {
                                settingsPanel.buildEntries(settings)
                                settingScroll.resetAll()
                                currentMod = card.mod
                                openSetting = true
                                setCanClose(false)
                            }
                            continue
                        }

                        if (MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, card.toggleWidth, card.toggleHeight)) {
                            card.mod.toggle()
                        }
                        continue
                    }

                }
            }
        }

        if (openSetting && settingAnimation.isDone(Direction.BACKWARDS)) {
            settingsPanel.setLayoutMode(InternalSettingsMod.instance.settingsLayoutMode)
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 22f, getY() + 20f, 18f, 18f) && mouseButton == 0) {
                openSetting = false
                settingsPanel.clear()
                return
            }
            val x = getX() - 32
            val y = getY() - 31
            val width = getWidth() + 32
            val height = getHeight() + 31
            if (!MouseUtils.isInside(mouseX, mouseY, x - 5f, y - 5f, width + 10f, height + 10f) && mouseButton == 0) {
                openSetting = false
                settingsPanel.clear()
                return
            }

            val headerY = this.getY() + 15f
            val headerHeight = this.getHeight() - 30f
            val contentX = this.getX() + 25f
            val contentY = headerY + 32f
            val contentWidth = this.getWidth() - 50f
            val viewportHeight = headerHeight - 47f

            if (settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, contentX, contentY, contentWidth, viewportHeight, settingScroll)) {
                return
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 41f, this.getY() + 21f, 16f, 16f) && mouseButton == 0) {
                settingsPanel.resetSettings()
            }
        }

        if (openSetting && mouseButton == 3) {
            openSetting = false
            settingsPanel.clear()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (currentMod != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.instance.settingsLayoutMode)
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentMod != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.instance.settingsLayoutMode)
            settingsPanel.keyTyped(typedChar, keyCode)
        }

        if (openSetting && keyCode == Keyboard.KEY_ESCAPE) {
            openSetting = false
            settingsPanel.clear()
            return
        }

        if (!openSetting) {
            scroll.onKey(keyCode)
            if (keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_ESCAPE) {
                getSearchBox().setFocused(true)
            }
        }
    }

    private fun filterMod(m: Mod): Boolean {
        if (m.isHide()) {
            return true
        }

        if (!m.isAllowed()) {
            return true
        }

        if (currentCategory != ModCategory.ALL && m.getCategory() != currentCategory) {
            return true
        }

        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(Shindo.getInstance().modManager.getWords(m), getSearchBox().getText())
    }

    private fun rebuildModuleCards(modManager: ModManager, startOffset: Float, columns: Int, iconLayout: Boolean) {
        moduleCardCache.clear()

        val spacingY = if (iconLayout) ICON_CARD_GAP else 14f
        val availableWidth = getWidth() - 30f
        val normalizedColumns = if (iconLayout) max(1, min(columns, 2)) else max(1, min(columns, 2))
        val spacingX = if (normalizedColumns > 1) {
            if (iconLayout) ICON_CARD_GAP else 24f
        } else {
            0f
        }
        val cardWidth = if (normalizedColumns == 1) {
            availableWidth
        } else {
            (availableWidth - (spacingX * (normalizedColumns - 1))) / normalizedColumns
        }
        val cardHeight = if (iconLayout) cardWidth * ICON_CARD_HEIGHT_RATIO else LIST_CARD_HEIGHT

        var columnIndex = 0
        var rowY = startOffset

        for (m in modManager.getMods()) {
            if (filterMod(m)) {
                continue
            }

            val cardX = getX() + 15f + columnIndex * (cardWidth + spacingX)
            moduleCardCache.add(ModuleCard(m, cardX, rowY, cardWidth, cardHeight))

            columnIndex++
            if (columnIndex >= normalizedColumns) {
                columnIndex = 0
                rowY += cardHeight + spacingY
            }
        }

        if (moduleCardCache.isEmpty()) {
            moduleContentHeight = max(0f, startOffset - 13f)
            return
        }

        val last = moduleCardCache[moduleCardCache.size - 1]
        val lastBottom = last.y + last.height

        moduleContentHeight = max(0f, lastBottom - 13f)
    }

    private fun resolveModuleColumns(): Int {
        return max(1, min(2, InternalSettingsMod.instance.moduleGridColumns))
    }

    private fun drawCategoryChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        scrollOffset: Float,
        mouseX: Int,
        mouseY: Int
    ): Float {
        categoryChips.clear()

        val startX = getX() + 18f
        val maxX = getX() + getWidth() - 18f
        var currentX = startX
        var currentY = getY() + 16f
        var blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT

        for (category in ModCategory.values()) {
            val label = category.getName()
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null)

            if (currentX + chipWidth > maxX) {
                currentX = startX
                currentY += CategoryChipRenderer.CHIP_HEIGHT + CHIP_GAP
                blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT
            }

            val active = category == currentCategory
            val hovered = !openSetting && MouseUtils.isInside(mouseX, mouseY, currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)

            CategoryChipRenderer.drawChip(nvg, palette, accentColor, currentX, currentY, chipWidth, label, null, active, hovered)

            val chip = FilterChip(Runnable {
                if (currentCategory != category) {
                    currentCategory = category
                    scroll.resetAll()
                    moduleCardCache.clear()
                }
            })
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            categoryChips.add(chip)

            currentX += chipWidth + CHIP_GAP
        }

        return (blockBottom - getY()) + CHIP_GAP
    }

    private fun getCardStyle(columns: Int): CardStyle {
        return when (columns) {
            1 -> CardStyle(28f, 20f, 18f, 14f, 18f)
            2 -> CardStyle(28f, 18f, 18f, 12f, 16f)
            else -> CardStyle(26f, 20f, 18f, 14f, 18f)
        }
    }

    private data class ModuleCard(
        val mod: Mod,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        var toggleX: Float = 0f,
        var toggleY: Float = 0f,
        var toggleWidth: Float = 0f,
        var toggleHeight: Float = 0f,
        var settingsX: Float = 0f,
        var settingsY: Float = 0f,
        var settingsSize: Float = 0f,
        var hasSettings: Boolean = false
    )

    private data class CardStyle(
        val iconSize: Float,
        val leftPadding: Float,
        val settingsSize: Float,
        val settingsPadding: Float,
        val textRightPadding: Float
    )

    private companion object {
        const val CHIP_GAP = 8f
        const val ICON_CARD_GAP = 14f
        const val ICON_CARD_RADIUS = 12f
        const val ICON_CARD_PADDING = 12f
        const val ICON_ICON_SIZE = 72f
        const val ICON_ICON_OFFSET = 0f
        const val ICON_ICON_FONT_SIZE = 26f
        const val ICON_ICON_FONT_OFFSET = 14f
        const val ICON_SETTINGS_SIZE = 18f
        const val ICON_TOGGLE_WIDTH = 44f
        const val ICON_TOGGLE_HEIGHT = 18f
        const val ICON_TOGGLE_GAP = 6f
        const val ICON_RESTRICT_SIZE = 12f
        const val ICON_CARD_HEIGHT_RATIO = 0.576f
        const val ICON_TEXT_GAP = 12f
        const val ICON_ICON_GAP = 10f
        const val LIST_CARD_HEIGHT = 51.84f
        const val LIST_TOGGLE_WIDTH = 44f
        const val LIST_TOGGLE_HEIGHT = 18f
        const val LIST_TOGGLE_GAP = 6f
        const val LIST_ICON_FONT_SIZE = 24f
        const val LIST_ICON_FONT_OFFSET = 7.5f
        const val LIST_WARNING_BOTTOM_PADDING = 12f
        const val LIST_WARNING_ICON_OFFSET = 2f
    }
}
