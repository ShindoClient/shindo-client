package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.mods.impl.InternalSettingsMod
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
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class AddonCategory(parent: GuiModMenu) : Category(parent, TranslateText.ADDONS, LegacyIcon.LAYOUT_2, true, true) {

    private val settingScroll = Scroll()
    private val settingsPanel = SettingsPanel()
    private val noColour = Color(0, 0, 0, 0)
    private val cardLayouts = HashMap<Addon, CardLayout>()
    private val typeChips = ArrayList<FilterChip>()
    private var currentType: AddonType = AddonType.ALL
    private var openSetting = false
    private var settingAnimation: Animation = SmoothStepAnimation(260, 1.0)
    private var currentAddon: Addon? = null

    override fun initGui() {
        currentType = AddonType.ALL
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
        val addonManager: AddonManager = instance.addonManager
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.palette
        val accentColor: AccentColor = colorManager.currentColor

        val scrollValue = scroll.getValue()

        settingAnimation.setDirection(if (openSetting) Direction.BACKWARDS else Direction.FORWARDS)

        if (settingAnimation.isDone(Direction.FORWARDS)) {
            setCanClose(true)
            currentAddon = null
            settingsPanel.clear()
        }

        val visibleAddons = collectVisibleAddons(addonManager)
        val iconLayout = false
        val contentStartY = getY() + 52f
        val availableWidth = getWidth() - (CARD_HORIZONTAL_PADDING * 2)
        val columns = if (iconLayout) 2 else 2
        val cardGap = if (iconLayout) ICON_CARD_GAP else CARD_COLUMN_GAP
        val cardWidth = if (columns <= 1) availableWidth else (availableWidth - cardGap * (columns - 1)) / columns
        val cardHeight = if (iconLayout) cardWidth * ICON_CARD_HEIGHT_RATIO else CARD_HEIGHT
        val viewportHeight = getHeight() - (contentStartY - getY()) - 24f

        nvg.save()
        nvg.translate(-(600f - (settingAnimation.getValue().toFloat() * 600f)), 0f)

        nvg.save()
        nvg.translate(0f, scrollValue)
        drawTypeChips(nvg, palette, accentColor, scrollValue, mouseX, mouseY)

        cardLayouts.clear()

        for (i in visibleAddons.indices) {
            val addon = visibleAddons[i]
            val column = i % columns
            val row = i / columns

            val cardX = getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + cardGap)
            val cardY = contentStartY + row * (cardHeight + if (iconLayout) ICON_CARD_GAP else CARD_ROW_GAP)

            if (cardY + scrollValue > getY() + getHeight() || cardY + scrollValue + cardHeight < getY()) {
                continue
            }

            val settings = addonManager.getSettingByAddon(addon)
            val hasSettings = !settings.isNullOrEmpty()

            val layout = CardLayout()
            layout.cardX = cardX
            layout.cardY = cardY + scrollValue
            layout.cardWidth = cardWidth
            layout.cardHeight = cardHeight
            if (iconLayout) {
            } else {
                layout.toggleX = cardX + cardWidth - TOGGLE_WIDTH - 18f
                layout.toggleY = cardY + CARD_HEIGHT - TOGGLE_HEIGHT - 18f + scrollValue
                layout.toggleWidth = TOGGLE_WIDTH
                layout.toggleHeight = TOGGLE_HEIGHT
            }
            if (hasSettings) {
                layout.hasSettings = true
                layout.settingsSize = if (iconLayout) ICON_SETTINGS_SIZE else SETTINGS_BUTTON_SIZE
                if (iconLayout) {
                } else {
                    layout.settingsX = cardX + cardWidth - layout.settingsSize - 18f
                    layout.settingsY = cardY + 12f + scrollValue
                }
            }
            cardLayouts[addon] = layout

            val hovered = !openSetting && layout.contains(mouseX, mouseY)
            if (iconLayout) {
            } else {
                drawAddonCard(nvg, palette, accentColor, addon, cardX, cardY, cardWidth, hovered, hasSettings, mouseX, mouseY)
            }
        }

        nvg.restore()
        nvg.drawVerticalGradientRect(getX() + 15f, getY().toFloat(), getWidth() - 30f, 12f, palette.getBackgroundColor(ColorType.NORMAL), noColour) // top
        nvg.drawVerticalGradientRect(getX() + 15f, getY() + getHeight() - 12f, getWidth() - 30f, 12f, noColour, palette.getBackgroundColor(ColorType.NORMAL)) // bottom
        nvg.restore()

        nvg.save()
        nvg.translate(settingAnimation.getValue().toFloat() * 600f, 0f)

        val activeAddon = currentAddon
        if (activeAddon != null) {
            if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) {
                settingScroll.onScroll()
                settingScroll.onAnimation()
            }

            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().settingsLayoutMode)

            val headerX = getX() + 15f
            val headerY = getY() + 15f
            val headerWidth = getWidth() - 30f
            val headerHeight = getHeight() - 30f

            nvg.drawShadow(headerX, headerY, headerWidth, headerHeight, 12f, 7)
            nvg.drawRoundedRect(headerX, headerY, headerWidth, headerHeight, 12f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
            nvg.drawRoundedRect(headerX + 1f, headerY + 1f, headerWidth - 2f, headerHeight - 2f, 11f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

            nvg.drawText(LegacyIcon.CHEVRON_LEFT, headerX + 10, headerY + 8, palette.getFontColor(ColorType.DARK), 13f, Fonts.LEGACYICON)
            nvg.drawText(activeAddon.name, headerX + 27, headerY + 9, palette.getFontColor(ColorType.DARK), 13f, Fonts.MEDIUM)
            nvg.drawText(LegacyIcon.REFRESH, headerX + headerWidth - 24, headerY + 7.5f, palette.getFontColor(ColorType.DARK), 13f, Fonts.LEGACYICON)

            val contentX = getX() + 25f
            val contentY = headerY + 32f
            val contentWidth = getWidth() - 50f
            val viewportHeight2 = headerHeight - 47f

            nvg.save()
            nvg.scissor(headerX + 5f, contentY - 5f, headerWidth - 10f, viewportHeight2 + 10f)
            settingsPanel.draw(mouseX, mouseY, partialTicks, contentX, contentY, contentWidth, viewportHeight2, nvg, palette, settingScroll)
            nvg.restore()
        }

        nvg.restore()

        var scrollMax = 0

        if (visibleAddons.isNotEmpty()) {
            val totalRows = ceil(visibleAddons.size / columns.toDouble())
            val rowGap = if (iconLayout) ICON_CARD_GAP else CARD_ROW_GAP
            val contentHeight = totalRows * cardHeight + max(0.0, totalRows - 1) * rowGap
            scrollMax = max(0, (contentHeight - viewportHeight).toInt())
        }

        scroll.maxScroll = scrollMax.toFloat()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val addonManager = instance.addonManager

        if (!openSetting && mouseButton == 0) {
            for (chip in typeChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click()
                    return
                }
            }
        }

        if (!openSetting && mouseButton == 0) {
            val iconLayout = false
            val visibleAddons = collectVisibleAddons(addonManager)
            for (addon in visibleAddons) {
                val layout = cardLayouts[addon] ?: continue
                if (!layout.contains(mouseX, mouseY)) {
                    continue
                }

                val settings = addonManager.getSettingByAddon(addon)
                if (!settings.isNullOrEmpty() && layout.insideSettings(mouseX, mouseY)) {
                    settingsPanel.buildEntries(settings)
                    settingScroll.resetAll()
                    currentAddon = addon
                    openSetting = true
                    setCanClose(false)
                    return
                }

                if (!iconLayout && layout.insideToggle(mouseX, mouseY)) {
                    addon.toggle()
                    return
                }

                if (iconLayout && layout.insideToggle(mouseX, mouseY)) {
                }
            }
        }

        if (openSetting && settingAnimation.isDone(Direction.BACKWARDS)) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().settingsLayoutMode)
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

            val headerX = this.getX() + 15f
            val headerY = this.getY() + 15f
            val headerWidth = this.getWidth() - 30f
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
        if (currentAddon != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().settingsLayoutMode)
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentAddon != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().settingsLayoutMode)
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

    private fun drawAddonCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        addon: Addon,
        cardX: Float,
        cardY: Float,
        cardWidth: Float,
        hovered: Boolean,
        hasSettings: Boolean,
        mouseX: Int,
        mouseY: Int
    ) {
        addon.animation.setAnimation(if (addon.isToggled()) 1.0f else 0.0f, 16.0)

        val cardBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), if (hovered) 236 else 206)
        val overlayStart = ColorUtils.applyAlpha(accentColor.color1, (addon.animation.value * 70).toInt())
        val overlayEnd = ColorUtils.applyAlpha(accentColor.color2, (addon.animation.value * 70).toInt())

        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, CARD_RADIUS, cardBase)
        if (addon.animation.value > 0f) {
            nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, CARD_RADIUS, overlayStart, overlayEnd)
        }

        val padding = 18f
        val titleX = cardX + padding
        val titleY = cardY + padding
        val textWidth = cardWidth - (padding * 2)

        nvg.drawText(addon.name, titleX, titleY, palette.getFontColor(ColorType.DARK), 12f, Fonts.SEMIBOLD)

        val description = addon.description
        val wrapped = nvg.getLimitText(description, 8.6f, Fonts.REGULAR, textWidth)
        nvg.drawText(wrapped, titleX, titleY + 20f, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 8.6f, Fonts.REGULAR)

        val typeName = addon.type.getName()
        val chipWidth = max(48f, nvg.getTextWidth(typeName, 8f, Fonts.MEDIUM) + 18f)
        var chipX = cardX + cardWidth - chipWidth - 18f
        if (hasSettings) {
            chipX -= SETTINGS_BUTTON_SIZE + 10f
        }
        chipX = max(chipX, titleX)

        if (hasSettings) {
            val settingsX = cardX + cardWidth - SETTINGS_BUTTON_SIZE - 18f
            val settingsY = cardY + padding - 6f

            val settingsBg = palette.getBackgroundColor(ColorType.DARK)

            nvg.drawRoundedRect(settingsX, settingsY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, 8f, settingsBg)
            nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + (SETTINGS_BUTTON_SIZE / 2f) - 1f, settingsY + 5f, palette.getFontColor(ColorType.DARK), 14f, Fonts.LEGACYICON)

            if (MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE)) {
                nvg.drawGradientOutlineRoundedRect(settingsX, settingsY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, 8f, 2f, accentColor.color1, accentColor.color2)
            }
        }

        val toggleX = cardX + cardWidth - TOGGLE_WIDTH - 18f
        val toggleY = cardY + CARD_HEIGHT - TOGGLE_HEIGHT - 18f
        val toggleRadius = TOGGLE_HEIGHT / 2f

        val toggleBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
        nvg.drawRoundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleRadius, toggleBase)

        if (addon.animation.value > 0f) {
            nvg.drawGradientRoundedRect(
                toggleX,
                toggleY,
                TOGGLE_WIDTH,
                TOGGLE_HEIGHT,
                toggleRadius,
                ColorUtils.applyAlpha(accentColor.color1, (addon.animation.value * 255).toInt()),
                ColorUtils.applyAlpha(accentColor.color2, (addon.animation.value * 255).toInt())
            )
        }

        val knobSize = TOGGLE_HEIGHT - 8f
        val knobX = toggleX + 4f + addon.animation.value * (TOGGLE_WIDTH - knobSize - 8f)
        val knobY = toggleY + 4f
        nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2f, Color.WHITE)
    }

    private fun drawAddonIconCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        addon: Addon,
        cardX: Float,
        cardY: Float,
        cardWidth: Float,
        cardHeight: Float,
        hovered: Boolean,
        hasSettings: Boolean,
        mouseX: Int,
        mouseY: Int,
        scrollOffset: Float
    ) {
        addon.animation.setAnimation(if (addon.isToggled()) 1.0f else 0.0f, 16.0)
        val toggleProgress = addon.animation.value

        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), if (hovered) 235 else 210)
        nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, ICON_CARD_RADIUS, base)
        if (toggleProgress > 0f) {
            nvg.drawGradientRoundedRect(
                cardX,
                cardY,
                cardWidth,
                cardHeight,
                ICON_CARD_RADIUS,
                ColorUtils.applyAlpha(accentColor.color1, (toggleProgress * 120).toInt()),
                ColorUtils.applyAlpha(accentColor.color2, (toggleProgress * 120).toInt())
            )
        }

        val iconBox = ICON_ICON_SIZE
        val toggleWidth = ICON_TOGGLE_WIDTH
        val toggleHeight = ICON_TOGGLE_HEIGHT
        val toggleX = cardX + cardWidth - ICON_CARD_PADDING - toggleWidth
        val toggleY = cardY + (cardHeight - toggleHeight) / 2f
        val actionLeftX = if (hasSettings) {
            toggleX - ICON_TOGGLE_GAP - ICON_SETTINGS_SIZE
        } else {
            toggleX
        }
        val iconX = actionLeftX - ICON_ICON_GAP - iconBox
        val iconY = cardY + (cardHeight - iconBox) / 2f - ICON_ICON_OFFSET

        if (addon.icon.isNotEmpty()) {
            nvg.drawCenteredText(addon.icon, iconX + iconBox / 2f, iconY + iconBox / 2f - ICON_ICON_FONT_OFFSET, palette.getFontColor(ColorType.DARK), ICON_ICON_FONT_SIZE, Fonts.LEGACYICON)
        }

        val textX = cardX + ICON_CARD_PADDING
        val textRight = iconX - ICON_TEXT_GAP
        val textWidth = max(40f, textRight - textX)
        val titleSize = 10.5f
        val descSize = 8f
        val titleY = cardY + ICON_CARD_PADDING + titleSize
        val descY = titleY + 10f

        val name = nvg.getLimitText(addon.name, titleSize, Fonts.MEDIUM, textWidth)
        nvg.drawText(name, textX, titleY, palette.getFontColor(ColorType.DARK), titleSize, Fonts.MEDIUM)

        val description = nvg.getLimitText(addon.description, descSize, Fonts.REGULAR, textWidth)
        nvg.drawText(description, textX, descY, palette.getFontColor(ColorType.NORMAL), descSize, Fonts.REGULAR)

        if (hasSettings) {
            val settingsSize = ICON_SETTINGS_SIZE
            val settingsX = toggleX - ICON_TOGGLE_GAP - settingsSize
            val settingsY = cardY + (cardHeight - settingsSize) / 2f
            val hoveredSettings = MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY + scrollOffset, settingsSize, settingsSize)
            nvg.drawRoundedRect(settingsX, settingsY, settingsSize, settingsSize, 6f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190))
            nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + settingsSize / 2f - 1f, settingsY + settingsSize / 2f - 6f, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
            if (hoveredSettings) {
                nvg.drawGradientOutlineRoundedRect(settingsX, settingsY, settingsSize, settingsSize, 6f, 1.0f, accentColor.color1, accentColor.color2)
            }
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

    private fun drawTypeChips(nvg: NanoVGManager, palette: ColorPalette, accentColor: AccentColor, scrollOffset: Float, mouseX: Int, mouseY: Int) {
        typeChips.clear()

        val startX = getX() + CARD_HORIZONTAL_PADDING
        val maxX = getX() + getWidth() - CARD_HORIZONTAL_PADDING
        var currentX = startX
        var currentY = getY() + 12f

        for (type in AddonType.values()) {
            val label = type.getName()
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null)

            if (currentX + chipWidth > maxX) {
                currentX = startX
                currentY += CategoryChipRenderer.CHIP_HEIGHT + TYPE_CHIP_GAP
            }

            val active = type == currentType
            val hovered = !openSetting && MouseUtils.isInside(mouseX, mouseY, currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)

            CategoryChipRenderer.drawChip(nvg, palette, accentColor, currentX, currentY, chipWidth, label, null, active, hovered)

            val chip = FilterChip(Runnable {
                if (currentType != type) {
                    currentType = type
                    scroll.resetAll()
                }
            })
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            typeChips.add(chip)

            currentX += chipWidth + TYPE_CHIP_GAP
        }
    }

    private fun filterAddon(a: Addon): Boolean {
        if (currentType != AddonType.ALL && a.type != currentType) {
            return true
        }

        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(Shindo.getInstance().addonManager.getWords(a), getSearchBox().getText())
    }

    private fun collectVisibleAddons(addonManager: AddonManager): ArrayList<Addon> {
        val visible = ArrayList<Addon>()
        for (addon in addonManager.addons) {
            if (!filterAddon(addon)) {
                visible.add(addon)
            }
        }
        return visible
    }

    private class CardLayout {
        var cardX = 0f
        var cardY = 0f
        var cardWidth = 0f
        var cardHeight = 0f
        var toggleX = 0f
        var toggleY = 0f
        var toggleWidth = 0f
        var toggleHeight = 0f
        var settingsX = 0f
        var settingsY = 0f
        var settingsSize = 0f
        var hasSettings = false

        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight)
        }

        fun insideToggle(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, toggleWidth, toggleHeight)
        }

        fun insideSettings(mouseX: Int, mouseY: Int): Boolean {
            return hasSettings && MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, settingsSize, settingsSize)
        }
    }

    private companion object {
        const val TYPE_CHIP_GAP = 8f
        const val CHIP_HORIZONTAL_PADDING = 12f
        const val CARD_HORIZONTAL_PADDING = 18f
        const val CARD_COLUMN_GAP = 16f
        const val CARD_ROW_GAP = 16f
        const val CARD_HEIGHT = 122f
        const val CARD_RADIUS = 14f
        const val TOGGLE_WIDTH = 58f
        const val TOGGLE_HEIGHT = 26f
        const val SETTINGS_BUTTON_SIZE = 24f
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
        const val ICON_CARD_HEIGHT_RATIO = 0.576f
        const val ICON_TEXT_GAP = 12f
        const val ICON_ICON_GAP = 10f
    }
}
