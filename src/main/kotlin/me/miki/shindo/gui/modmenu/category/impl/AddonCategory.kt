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

class AddonCategory(parent: GuiModMenu) : Category(parent, TranslateText.ADDONS, LegacyIcon.LAYOUT_2, true, true) {

    private val settingScroll = Scroll()
    private val settingsPanel = SettingsPanel()
    private val addonCardCache = ArrayList<AddonCard>()
    private val typeChips = ArrayList<FilterChip>()
    private val noColour = Color(0, 0, 0, 0)
    private var currentType: AddonType = AddonType.ALL
    private var openSetting = false
    private var settingAnimation: Animation = SmoothStepAnimation(260, 1.0)
    private var currentAddon: Addon? = null
    private var contentHeight = 0f

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

        nvg.save()
        nvg.translate(-(600f - (settingAnimation.getValue().toFloat() * 600f)), 0f)

        // Lista de addons (lado esquerdo)
        nvg.save()
        nvg.translate(0f, scrollValue)

        val chipOffset = drawTypeChips(nvg, palette, accentColor, scrollValue, mouseX, mouseY)
        rebuildAddonCards(addonManager, chipOffset)

        for (card in addonCardCache) {
            val cardY = getY() + card.y
            val iconSize = LIST_ICON_SIZE
            val iconX = card.x + LIST_ICON_LEFT_PADDING
            val iconY = cardY + (card.height - iconSize) / 2f

            val hasSettings = addonManager.getSettingByAddon(card.addon) != null
            val toggleWidth = LIST_TOGGLE_WIDTH
            val toggleHeight = LIST_TOGGLE_HEIGHT
            val toggleX = card.x + card.width - toggleWidth - 18f
            val toggleY = cardY + (card.height - toggleHeight) / 2f
            val settingsX = toggleX - LIST_TOGGLE_GAP - SETTINGS_SIZE
            val settingsY = cardY + (card.height - SETTINGS_SIZE) / 2f

            card.hasSettings = hasSettings
            card.settingsX = settingsX
            card.settingsY = settingsY + scrollValue
            card.toggleX = toggleX
            card.toggleY = toggleY + scrollValue
            card.toggleWidth = toggleWidth
            card.toggleHeight = toggleHeight

            val textSpacing = 10f
            val textX = iconX + iconSize + textSpacing
            val textRight = if (hasSettings) settingsX - LIST_TOGGLE_GAP else toggleX - LIST_TOGGLE_GAP
            val textWidth = max(80f, textRight - textX)

            val hovered =
                MouseUtils.isInside(mouseX, mouseY, card.x, cardY + scrollValue, card.width, card.height) &&
                        !MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            settingsX,
                            settingsY + scrollValue,
                            SETTINGS_SIZE,
                            SETTINGS_SIZE
                        ) &&
                        !MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            toggleX,
                            toggleY + scrollValue,
                            toggleWidth,
                            toggleHeight
                        )


            card.addon.hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 16.0)
            val hoverProgress = card.addon.hoverAnimation.value

            val settingsHover = MouseUtils.isInside(
                mouseX,
                mouseY,
                settingsX,
                settingsY + scrollValue,
                SETTINGS_SIZE,
                SETTINGS_SIZE
            )

            card.addon.settingsHoverAnimation.setAnimation(if (settingsHover) 1.0f else 0.0f, 18.0)

            val settingsHoverAnimation = card.addon.settingsHoverAnimation.value

            val overlayAlpha = (18 + (hoverProgress * 26)).toInt()
            val fillAlpha = (220 + (hoverProgress * 32)).toInt()
            val outlineAlpha = (hoverProgress * 220).toInt()

            nvg.drawRoundedRect(
                card.x,
                cardY,
                card.width,
                card.height,
                8f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), fillAlpha)
            )

            nvg.drawGradientRoundedRect(
                card.x,
                cardY,
                card.width,
                card.height,
                8f,
                ColorUtils.applyAlpha(accentColor.color1, overlayAlpha),
                ColorUtils.applyAlpha(accentColor.color2, overlayAlpha)
            )

            if (outlineAlpha > 0) {
                nvg.drawOutlineRoundedRect(
                    card.x,
                    cardY,
                    card.width,
                    card.height,
                    8f,
                    1.0f,
                    ColorUtils.applyAlpha(accentColor.color2, outlineAlpha)
                )
            }

            card.addon.animation.setAnimation(if (card.addon.isToggled()) 1.0f else 0.0f, 16.0)
            val toggleProgress = card.addon.animation.value

            val icon = card.addon.icon
            if (icon.isNotEmpty()) {
                nvg.drawCenteredText(
                    icon,
                    iconX + iconSize / 2f,
                    iconY + iconSize / 2f - LIST_ICON_FONT_OFFSET,
                    palette.getFontColor(ColorType.DARK),
                    LIST_ICON_FONT_SIZE,
                    Fonts.LEGACYICON
                )
            }

            val name = nvg.getLimitText(card.addon.name, 11.5f, Fonts.MEDIUM, textWidth)
            nvg.drawText(name, textX, cardY + 14f, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)

            val description = nvg.getLimitText(card.addon.getDescription(), 8.5f, Fonts.REGULAR, textWidth)
            nvg.drawText(
                description,
                textX,
                cardY + 26f,
                palette.getFontColor(ColorType.NORMAL),
                8.5f,
                Fonts.REGULAR
            )

            if (hasSettings) {
                nvg.drawRoundedRect(
                    settingsX,
                    settingsY,
                    SETTINGS_SIZE,
                    SETTINGS_SIZE,
                    5f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180)
                )

                nvg.drawCenteredText(
                    LegacyIcon.SETTINGS,
                    settingsX + SETTINGS_SIZE / 2f - 1f,
                    settingsY + SETTINGS_SIZE / 2f - 6f,
                    palette.getFontColor(ColorType.DARK),
                    14f,
                    Fonts.LEGACYICON
                )

                nvg.drawGradientOutlineRoundedRect(
                    settingsX,
                    settingsY,
                    SETTINGS_SIZE,
                    SETTINGS_SIZE,
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

        nvg.restore()

        nvg.drawVerticalGradientRect(
            getX() + 15f,
            getY().toFloat(),
            getWidth() - 30f,
            12f,
            palette.getBackgroundColor(ColorType.NORMAL),
            noColour
        )
        nvg.drawVerticalGradientRect(
            getX() + 15f,
            getY() + getHeight() - 12f,
            getWidth() - 30f,
            12f,
            noColour,
            palette.getBackgroundColor(ColorType.NORMAL)
        )

        nvg.restore()

        nvg.save()
        nvg.translate(settingAnimation.getValue().toFloat() * 600f, 0f)

        val activeAddon = currentAddon
        if (activeAddon != null) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    getX().toFloat(),
                    getY().toFloat(),
                    getWidth().toFloat(),
                    getHeight().toFloat()
                )
            ) {
                settingScroll.onScroll()
                settingScroll.onAnimation()
            }

            settingsPanel.setLayoutMode(InternalSettingsMod.instance.settingsLayoutMode)

            val headerX = getX() + 15f
            val headerY = getY() + 15f
            val headerWidth = getWidth() - 30f
            val headerHeight = getHeight() - 30f

            nvg.drawShadow(headerX, headerY, headerWidth, headerHeight, 12f, 7)
            nvg.drawRoundedRect(
                headerX,
                headerY,
                headerWidth,
                headerHeight,
                12f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210)
            )
            nvg.drawRoundedRect(
                headerX + 1f,
                headerY + 1f,
                headerWidth - 2f,
                headerHeight - 2f,
                11f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230)
            )

            nvg.drawText(
                LegacyIcon.CHEVRON_LEFT,
                headerX + 10,
                headerY + 8,
                palette.getFontColor(ColorType.DARK),
                13f,
                Fonts.LEGACYICON
            )
            nvg.drawText(
                activeAddon.name,
                headerX + 27,
                headerY + 9,
                palette.getFontColor(ColorType.DARK),
                13f,
                Fonts.MEDIUM
            )
            nvg.drawText(
                LegacyIcon.REFRESH,
                headerX + headerWidth - 24,
                headerY + 7.5f,
                palette.getFontColor(ColorType.DARK),
                13f,
                Fonts.LEGACYICON
            )

            val contentX = getX() + 25f
            val contentY = headerY + 32f
            val contentWidth = getWidth() - 50f
            val viewportHeight2 = headerHeight - 47f

            nvg.save()
            nvg.scissor(headerX + 5f, contentY - 5f, headerWidth - 10f, viewportHeight2 + 10f)
            settingsPanel.draw(
                mouseX,
                mouseY,
                partialTicks,
                contentX,
                contentY,
                contentWidth,
                viewportHeight2,
                nvg,
                palette,
                settingScroll
            )
            nvg.restore()
        }

        nvg.restore()

        val viewportHeight = getHeight() - 26f
        scroll.maxScroll = max(0f, contentHeight - viewportHeight)
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

        if (!openSetting) {

            for (card in addonCardCache) {
                val cardY = getY() + card.y + scroll.getValue()

                if (!MouseUtils.isInside(mouseX, mouseY, card.x, cardY, card.width, card.height)) {
                    continue
                }

                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        getX().toFloat(),
                        getY().toFloat(),
                        getWidth().toFloat(),
                        getHeight().toFloat()
                    ) && mouseButton == 0
                ) {
                    val settingsX = card.settingsX
                    val settingsY = card.settingsY
                    val toggleX = card.toggleX
                    val toggleY = card.toggleY

                    if (MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            settingsX,
                            settingsY,
                            SETTINGS_SIZE,
                            SETTINGS_SIZE
                        ) && !openSetting
                    ) {
                        val settings: ArrayList<Setting>? = addonManager.getSettingByAddon(card.addon)
                        if (settings != null) {
                            settingsPanel.buildEntries(settings)
                            settingScroll.resetAll()
                            currentAddon = card.addon
                            openSetting = true
                            setCanClose(false)
                        }
                        continue
                    }

                    if (MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            toggleX,
                            toggleY,
                            card.toggleWidth,
                            card.toggleHeight
                        )
                    ) {
                        card.addon.toggle()
                    }
                    continue
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

            this.getX() + 15f
            val headerY = this.getY() + 15f
            this.getWidth() - 30f
            val headerHeight = this.getHeight() - 30f
            val contentX = this.getX() + 25f
            val contentY = headerY + 32f
            val contentWidth = this.getWidth() - 50f
            val viewportHeight = headerHeight - 47f

            if (settingsPanel.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton,
                    contentX,
                    contentY,
                    contentWidth,
                    viewportHeight,
                    settingScroll
                )
            ) {
                return
            }

            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX() + this.getWidth() - 41f,
                    this.getY() + 21f,
                    16f,
                    16f
                ) && mouseButton == 0
            ) {
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
            settingsPanel.setLayoutMode(InternalSettingsMod.instance.settingsLayoutMode)
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentAddon != null) {
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


    private fun drawTypeChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        scrollOffset: Float,
        mouseX: Int,
        mouseY: Int
    ): Float {
        typeChips.clear()

        val startX = getX() + 18f
        val maxX = getX() + getWidth() - 18f
        var currentX = startX
        var currentY = getY() + 16f
        var blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT


        for (type in AddonType.entries) {
            val label = type.getName()
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null)

            if (currentX + chipWidth > maxX) {
                currentX = startX
                currentY += CategoryChipRenderer.CHIP_HEIGHT + TYPE_CHIP_GAP
                blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT
            }

            val active = type == currentType
            val hovered =
                !openSetting && MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    currentX,
                    currentY + scrollOffset,
                    chipWidth,
                    CategoryChipRenderer.CHIP_HEIGHT
                )

            CategoryChipRenderer.drawChip(
                nvg,
                palette,
                accentColor,
                currentX,
                currentY,
                chipWidth,
                label,
                null,
                active,
                hovered
            )

            val chip = FilterChip(
                Runnable {
                    if (currentType != type) {
                        currentType = type
                        scroll.resetAll()
                        addonCardCache.clear()
                    }
                }
            )
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            typeChips.add(chip)

            currentX += chipWidth + TYPE_CHIP_GAP
        }

        return (blockBottom - getY()) + TYPE_CHIP_GAP
    }

    private fun filterAddon(a: Addon): Boolean {
        if (currentType != AddonType.ALL && a.type != currentType) {
            return true
        }

        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(
            Shindo.getInstance().addonManager.getWords(a),
            getSearchBox().getText()
        )
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

    private data class AddonCard(
        val addon: Addon,
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
        var hasSettings: Boolean = false
    )

    private fun rebuildAddonCards(addonManager: AddonManager, startOffset: Float) {
        addonCardCache.clear()

        val availableWidth = getWidth() - 30f
        val cardWidth = availableWidth
        val cardHeight = LIST_CARD_HEIGHT
        val spacingY = 14f

        var rowY = startOffset

        for (addon in addonManager.addons) {
            if (filterAddon(addon)) continue

            val cardX = getX() + 15f
            addonCardCache.add(AddonCard(addon, cardX, rowY, cardWidth, cardHeight))

            rowY += cardHeight + spacingY
        }

        if (addonCardCache.isEmpty()) {
            contentHeight = max(0f, startOffset - 13f)
            return
        }

        val last = addonCardCache[addonCardCache.size - 1]
        val lastBottom = last.y + last.height

        contentHeight = max(0f, lastBottom - 13F)

    }

    private companion object {
        const val TYPE_CHIP_GAP = 8f
        const val CHIP_HORIZONTAL_PADDING = 12f
        const val LIST_CARD_HEIGHT = 51.84f
        const val LIST_TOGGLE_WIDTH = 44f
        const val LIST_TOGGLE_HEIGHT = 18f
        const val LIST_TOGGLE_GAP = 6f
        const val LIST_ICON_SIZE = 26f
        const val LIST_ICON_LEFT_PADDING = 20f
        const val LIST_ICON_FONT_SIZE = 24f
        const val LIST_ICON_FONT_OFFSET = 7.5f
        const val SETTINGS_SIZE = 18f
    }
}
