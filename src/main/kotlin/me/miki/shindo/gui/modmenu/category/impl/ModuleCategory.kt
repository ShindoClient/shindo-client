package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.module.ModuleCategoryRenderer
import me.miki.shindo.gui.modmenu.category.list.ModMenuListCardLayoutSpec
import me.miki.shindo.gui.modmenu.category.list.ModMenuListPageContract
import me.miki.shindo.gui.modmenu.category.list.ModMenuListPageRenderContext
import me.miki.shindo.gui.modmenu.navigation.ModMenuSlideTransitionCoordinator
import me.miki.shindo.gui.modmenu.render.ModMenuListCardLayout
import me.miki.shindo.gui.modmenu.render.ModMenuSettingsOverlayRenderer
import me.miki.shindo.gui.modmenu.style.ModMenuMotion
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
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.chips.CategoryChipRenderer
import me.miki.shindo.ui.comp.chips.FilterChip
import me.miki.shindo.ui.comp.layout.SettingsPanel
import me.miki.shindo.ui.comp.layout.settingspanel.SettingsPanelStyle
import me.miki.shindo.utils.SearchUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class ModuleCategory(parent: GuiModMenu) :
    Category(parent, TranslateText.MODULE, LegacyIcon.ARCHIVE, true, true),
    ModMenuListPageContract {

    private val settingScroll = Scroll()
    private val settingsPanel = SettingsPanel()
    private val moduleCardCache = ArrayList<ModuleCard>()
    private val categoryChips = ArrayList<FilterChip>()
    private val noColour = Color(0, 0, 0, 0)
    private val detailTransition = ModMenuSlideTransitionCoordinator()
    private var currentCategory: ModCategory = ModCategory.ALL
    private var currentMod: Mod? = null
    private var moduleContentHeight = 0f
    private val resetSpinAnimation = SimpleAnimation(0f)
    private var resetSpinTarget = 0f
    private var currentLayoutColumns = 1

    override fun initGui() {
        currentCategory = ModCategory.ALL
        detailTransition.reset()
        resetSpinAnimation.value = 0f
        resetSpinTarget = 0f
        currentMod = null
        settingsPanel.clear()
    }

    override fun initCategory() {
        scroll.resetAll()
        detailTransition.reset()
        resetSpinAnimation.value = 0f
        resetSpinTarget = 0f
        currentMod = null
        settingsPanel.clear()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.getPalette()
        val accentColor: AccentColor = colorManager.getCurrentColor()

        val scrollValue = scroll.getValue()
        currentLayoutColumns = resolveModuleColumns()

        detailTransition.update {
            setCanClose(true)
            currentMod = null
            settingsPanel.clear()
        }

        nvg.save()
        nvg.translate(detailTransition.getEnterTranslateX(ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE), 0f)

        nvg.save()
        nvg.translate(0f, scrollValue)

        val listContext = ModMenuListPageRenderContext(
            nvg = nvg,
            palette = palette,
            accent = accentColor,
            mouseX = mouseX,
            mouseY = mouseY,
            partialTicks = partialTicks,
            scrollOffset = scrollValue
        )
        val topFiltersBottom = drawTopFilters(listContext)
        rebuildFilteredEntries(topFiltersBottom)
        drawEntryCards(listContext, resolveCardLayoutSpec())

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
        nvg.translate(detailTransition.getSlideOffset(ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE), 0f)

        if (isDetailsLayerOpen()) {
            val detailContext = ModMenuListPageRenderContext(
                nvg = nvg,
                palette = palette,
                accent = accentColor,
                mouseX = mouseX,
                mouseY = mouseY,
                partialTicks = partialTicks,
                scrollOffset = settingScroll.getValue()
            )
            drawDetailsLayer(detailContext)
        }

        nvg.restore()

        val viewportHeight = getHeight() - 26f
        scroll.maxScroll = max(0f, moduleContentHeight - viewportHeight)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val modManager = instance.modManager

        if (detailTransition.isInteractive() && mouseButton == 0) {
            for (chip in categoryChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click()
                    return
                }
            }
        }

        if (detailTransition.isInteractive()) {
            val iconLayout = false
            val cardStyle = getCardStyle(resolveModuleColumns())

            for (card in moduleCardCache) {
                val cardY = getY() + card.y + scroll.getValue()
                val controlLayout = ModMenuListCardLayout.build(
                    cardX = card.x,
                    cardY = cardY,
                    cardWidth = card.width,
                    cardHeight = card.height,
                    settingsSize = cardStyle.settingsSize,
                    settingsPaddingFromRight = cardStyle.settingsPadding,
                    toggleWidth = LIST_TOGGLE_WIDTH,
                    toggleHeight = LIST_TOGGLE_HEIGHT,
                    settingsGap = LIST_TOGGLE_GAP
                )

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
                    if (iconLayout && card.hasSettings && controlLayout.isSettingsHit(mouseX, mouseY)) {
                        val settings: ArrayList<Setting>? = modManager.getSettingsByMod(card.mod)
                        if (settings != null) {
                            settingsPanel.buildEntries(settings)
                            settingScroll.resetAll()
                            currentMod = card.mod
                            detailTransition.open()
                            setCanClose(false)
                        }
                        continue
                    }

                    if (!iconLayout) {
                        if (controlLayout.isSettingsHit(mouseX, mouseY) && detailTransition.isInteractive()) {
                            val settings: ArrayList<Setting>? = modManager.getSettingsByMod(card.mod)
                            if (settings != null) {
                                settingsPanel.buildEntries(settings)
                                settingScroll.resetAll()
                                currentMod = card.mod
                                detailTransition.open()
                                setCanClose(false)
                            }
                            continue
                        }

                        if (controlLayout.isToggleHit(mouseX, mouseY)) {
                            card.mod.toggle()
                        }
                        continue
                    }
                }
            }
        }

        if (detailTransition.isActive()) {
            applySettingsPanelPreferences()
            val overlayLayout = ModMenuSettingsOverlayRenderer.computeLayout(
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat()
            )

            val backX = overlayLayout.panelX + 10f
            val backY = overlayLayout.headerIconY
            if (MouseUtils.isInside(mouseX, mouseY, backX - 3f, backY - 3f, 20f, 20f) && mouseButton == 0) {
                detailTransition.close()
                return
            }
            val x = getX() - 32
            val y = getY() - 31
            val width = getWidth() + 32
            val height = getHeight() + 31
            if (!MouseUtils.isInside(mouseX, mouseY, x - 5f, y - 5f, width + 10f, height + 10f) && mouseButton == 0) {
                detailTransition.close()
                return
            }

            if (settingsPanel.mouseClicked(
                    mouseX,
                    mouseY,
                    mouseButton,
                    overlayLayout.contentX,
                    overlayLayout.contentY,
                    overlayLayout.contentWidth,
                    overlayLayout.contentHeight,
                    settingScroll
                )
            ) {
                return
            }

            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    overlayLayout.panelX + overlayLayout.panelWidth - 26f,
                    overlayLayout.headerIconY,
                    16f,
                    16f
                ) && mouseButton == 0
            ) {
                settingsPanel.resetSettings()
                resetSpinTarget += 360f
            }
        }

        if (isDetailsLayerOpen() && mouseButton == 3) {
            detailTransition.close()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (currentMod != null && detailTransition.isActive()) {
            applySettingsPanelPreferences()
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentMod != null && detailTransition.isActive()) {
            applySettingsPanelPreferences()
            settingsPanel.keyTyped(typedChar, keyCode)
        }

        if (isDetailsLayerOpen() && keyCode == Keyboard.KEY_ESCAPE) {
            detailTransition.close()
            return
        }

        if (detailTransition.isInteractive()) {
            scroll.onKey(keyCode)
            if (keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_ESCAPE) {
                getSearchBox().setFocused(true)
            }
        }
    }

    override fun drawTopFilters(context: ModMenuListPageRenderContext): Float {
        return drawCategoryChips(
            nvg = context.nvg,
            palette = context.palette,
            accentColor = context.accent,
            scrollOffset = context.scrollOffset,
            mouseX = context.mouseX,
            mouseY = context.mouseY
        )
    }

    override fun rebuildFilteredEntries(topFiltersBottom: Float) {
        rebuildModuleCards(
            modManager = Shindo.getInstance().modManager,
            startOffset = topFiltersBottom,
            columns = currentLayoutColumns,
            iconLayout = false
        )
    }

    override fun resolveCardLayoutSpec(): ModMenuListCardLayoutSpec {
        if (moduleCardCache.isNotEmpty()) {
            val first = moduleCardCache[0]
            return ModMenuListCardLayoutSpec(
                columns = currentLayoutColumns,
                cardWidth = first.width,
                cardHeight = first.height,
                spacingX = if (currentLayoutColumns > 1) 24f else 0f,
                spacingY = 14f
            )
        }

        val columns = max(1, min(2, currentLayoutColumns))
        val spacingX = if (columns > 1) 24f else 0f
        val cardWidth = if (columns == 1) {
            getWidth() - 30f
        } else {
            (getWidth() - 30f - spacingX) / columns
        }

        return ModMenuListCardLayoutSpec(
            columns = columns,
            cardWidth = cardWidth,
            cardHeight = LIST_CARD_HEIGHT,
            spacingX = spacingX,
            spacingY = 14f
        )
    }

    override fun drawEntryCards(context: ModMenuListPageRenderContext, layout: ModMenuListCardLayoutSpec) {
        if (moduleCardCache.isEmpty()) {
            context.nvg.drawCenteredText(
                TranslateText.NONE.getText(),
                getX() + getWidth() / 2f,
                getY() + 86f,
                context.palette.getFontColor(ColorType.NORMAL),
                10f,
                Fonts.REGULAR
            )
            return
        }

        val modManager = Shindo.getInstance().modManager
        val style = getCardStyle(layout.columns)

        for (card in moduleCardCache) {
            if (card.y + context.scrollOffset + card.height <= 0 || card.y + context.scrollOffset >= getHeight()) {
                continue
            }

            val cardY = getY() + card.y
            val iconX = card.x + style.leftPadding
            val iconY = cardY + (card.height - style.iconSize) / 2f

            val hasSettings = modManager.getSettingsByMod(card.mod) != null
            val controlLayout = ModMenuListCardLayout.build(
                cardX = card.x,
                cardY = cardY,
                cardWidth = card.width,
                cardHeight = card.height,
                settingsSize = style.settingsSize,
                settingsPaddingFromRight = style.settingsPadding,
                toggleWidth = LIST_TOGGLE_WIDTH,
                toggleHeight = LIST_TOGGLE_HEIGHT,
                settingsGap = LIST_TOGGLE_GAP
            )
            val hitboxLayout = controlLayout.withOffset(context.scrollOffset)
            card.hasSettings = hasSettings

            val textX = iconX + style.iconSize + 10f
            val textRight = if (hasSettings) {
                controlLayout.settingsX - LIST_TOGGLE_GAP
            } else {
                controlLayout.toggleX - LIST_TOGGLE_GAP
            }
            val textWidth = max(80f, textRight - textX)

            val hovered = hitboxLayout.isBodyHit(
                context.mouseX,
                context.mouseY,
                card.x,
                cardY + context.scrollOffset,
                card.width,
                card.height
            )
            card.mod.hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, ModMenuMotion.CARD_HOVER_SPEED)
            val hoverProgress = card.mod.hoverAnimation.value

            val settingsHover = hitboxLayout.isSettingsHit(context.mouseX, context.mouseY)
            card.mod.settingsHoverAnimation.setAnimation(
                if (settingsHover) 1.0f else 0.0f,
                ModMenuMotion.CARD_HOVER_SPEED
            )
            val settingsHoverAnimation = card.mod.settingsHoverAnimation.value

            card.mod.animation.setAnimation(if (card.mod.isToggled()) 1.0f else 0.0f, ModMenuMotion.CARD_TOGGLE_SPEED)
            val toggleProgress = card.mod.animation.value

            val modName = context.nvg.getLimitText(card.mod.getName(), 11.5f, Fonts.MEDIUM, textWidth)
            val description = context.nvg.getLimitText(card.mod.getDescription(), 8.5f, Fonts.REGULAR, textWidth)
            ModuleCategoryRenderer.drawCard(
                nvg = context.nvg,
                palette = context.palette,
                accent = context.accent,
                x = card.x,
                y = cardY,
                width = card.width,
                height = card.height,
                hoverProgress = hoverProgress,
                icon = card.mod.getMenuIcon(),
                iconCenterX = iconX + style.iconSize / 2f,
                iconCenterY = iconY + style.iconSize / 2f - LIST_ICON_FONT_OFFSET,
                iconFontSize = LIST_ICON_FONT_SIZE,
                name = modName,
                description = description,
                textX = textX,
                nameY = cardY + 14f,
                descriptionY = cardY + 26f,
                restricted = card.mod.isRestricted(),
                warningY = cardY + card.height - LIST_WARNING_BOTTOM_PADDING,
                hasSettings = hasSettings,
                settingsX = controlLayout.settingsX,
                settingsY = controlLayout.settingsY,
                settingsSize = style.settingsSize,
                settingsHoverProgress = settingsHoverAnimation,
                toggleX = controlLayout.toggleX,
                toggleY = controlLayout.toggleY,
                toggleWidth = controlLayout.toggleWidth,
                toggleHeight = controlLayout.toggleHeight,
                toggleProgress = toggleProgress
            )
        }
    }

    override fun isDetailsLayerOpen(): Boolean {
        return currentMod != null
    }

    override fun drawDetailsLayer(context: ModMenuListPageRenderContext) {
        val activeMod = currentMod ?: return

        if (detailTransition.isActive() && MouseUtils.isInside(
                context.mouseX,
                context.mouseY,
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat()
            )
        ) {
            settingScroll.onScroll()
            settingScroll.onAnimation()
        }

        applySettingsPanelPreferences()
        ModMenuSettingsOverlayRenderer.drawBackdrop(
            nvg = context.nvg,
            palette = context.palette,
            viewportX = getX().toFloat(),
            viewportY = getY().toFloat(),
            viewportWidth = getWidth().toFloat(),
            viewportHeight = getHeight().toFloat()
        )
        val layout = ModMenuSettingsOverlayRenderer.computeLayout(
            getX().toFloat(),
            getY().toFloat(),
            getWidth().toFloat(),
            getHeight().toFloat()
        )

        resetSpinAnimation.setAnimation(resetSpinTarget, 20.0)
        ModMenuSettingsOverlayRenderer.drawChrome(
            nvg = context.nvg,
            palette = context.palette,
            layout = layout,
            title = activeMod.getName(),
            resetRotation = resetSpinAnimation.value,
            mouseX = context.mouseX,
            mouseY = context.mouseY
        )
        ModMenuSettingsOverlayRenderer.drawSettingsPanel(
            nvg = context.nvg,
            palette = context.palette,
            panel = settingsPanel,
            layout = layout,
            scroll = settingScroll,
            mouseX = context.mouseX,
            mouseY = context.mouseY,
            partialTicks = context.partialTicks
        )
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

        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(
            Shindo.getInstance().modManager.getWords(
                m
            ), getSearchBox().getText()
        )
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
            val hovered = detailTransition.isInteractive() && MouseUtils.isInside(
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
            else -> CardStyle(28f, 20f, 18f, 14f, 18f)
        }
    }

    private data class ModuleCard(
        val mod: Mod,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        var hasSettings: Boolean = false
    )

    private data class CardStyle(
        val iconSize: Float,
        val leftPadding: Float,
        val settingsSize: Float,
        val settingsPadding: Float,
        val textRightPadding: Float
    )

    private fun applySettingsPanelPreferences() {
        val settings = InternalSettingsMod.instance
        ModMenuSettingsOverlayRenderer.configureSettingsPanel(
            panel = settingsPanel,
            panelStyle = MODULE_SETTINGS_PANEL_STYLE,
            layoutMode = settings.settingsLayoutMode
        )
    }

    private companion object {
        const val CHIP_GAP = 8f
        const val ICON_CARD_GAP = 14f
        const val ICON_CARD_HEIGHT_RATIO = 0.576f
        const val LIST_CARD_HEIGHT = 51.84f
        const val LIST_TOGGLE_WIDTH = 44f
        const val LIST_TOGGLE_HEIGHT = 18f
        const val LIST_TOGGLE_GAP = 6f
        const val LIST_ICON_FONT_SIZE = 24f
        const val LIST_ICON_FONT_OFFSET = 7.5f
        const val LIST_WARNING_BOTTOM_PADDING = 12f
        const val LIST_WARNING_ICON_OFFSET = 2f

        val MODULE_SETTINGS_PANEL_STYLE = SettingsPanelStyle(
            cardPaddingX = 16f,
            cardPaddingY = 12f,
            rowGap = 8f,
            categoryGap = 14f
        )
    }
}
