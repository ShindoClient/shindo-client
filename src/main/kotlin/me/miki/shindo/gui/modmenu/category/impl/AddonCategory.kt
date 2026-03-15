package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.addon.AddonCategoryRenderer
import me.miki.shindo.gui.modmenu.category.list.ModMenuListCardLayoutSpec
import me.miki.shindo.gui.modmenu.category.list.ModMenuListPageContract
import me.miki.shindo.gui.modmenu.category.list.ModMenuListPageRenderContext
import me.miki.shindo.gui.modmenu.navigation.ModMenuDetailLayerTransitionCoordinator
import me.miki.shindo.gui.modmenu.render.ModMenuListCardLayout
import me.miki.shindo.gui.modmenu.render.ModMenuSettingsOverlayRenderer
import me.miki.shindo.gui.modmenu.style.ModMenuMotion
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonManager
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.addons.FailedAddonEntry
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
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.chips.CategoryChipRenderer
import me.miki.shindo.ui.comp.chips.FilterChip
import me.miki.shindo.ui.comp.layout.SettingsPanel
import me.miki.shindo.ui.comp.layout.settingspanel.SettingsPanelStyle
import me.miki.shindo.utils.SearchUtils
import me.miki.shindo.utils.TextUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class AddonCategory(parent: GuiModMenu) :
    Category(parent, TranslateText.ADDONS, LegacyIcon.LAYOUT_2, true, true),
    ModMenuListPageContract {

    private val settingScroll = Scroll()
    private val settingsPanel = SettingsPanel()
    private val addonCardCache = ArrayList<AddonCard>()
    private val typeChips = ArrayList<FilterChip>()
    private val noColour = Color(0, 0, 0, 0)
    private val detailTransition = ModMenuDetailLayerTransitionCoordinator()
    private var currentType: AddonType = AddonType.ALL
    private var currentAddon: Addon? = null
    private var contentHeight = 0f
    private val resetSpinAnimation = SimpleAnimation(0f)
    private var resetSpinTarget = 0f
    private var currentLayoutColumns = 1

    override fun initGui() {
        currentType = AddonType.ALL
        detailTransition.reset()
        resetSpinAnimation.value = 0f
        resetSpinTarget = 0f
        currentAddon = null
        settingsPanel.clear()
    }

    override fun initCategory() {
        scroll.resetAll()
        detailTransition.reset()
        resetSpinAnimation.value = 0f
        resetSpinTarget = 0f
        currentAddon = null
        settingsPanel.clear()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.getPalette()
        val accentColor: AccentColor = colorManager.getCurrentColor()

        val scrollValue = scroll.getValue()
        currentLayoutColumns = 1

        detailTransition.update {
            setCanClose(true)
            currentAddon = null
            settingsPanel.clear()
        }

        nvg.save()
        nvg.translate(detailTransition.getListTranslateX(), 0f)

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
        nvg.translate(detailTransition.getDetailsTranslateX(), 0f)

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
        scroll.maxScroll = max(0f, contentHeight - viewportHeight)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val addonManager = instance.addonManager

        if (detailTransition.isListInteractive() && mouseButton == 0) {
            for (chip in typeChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click()
                    return
                }
            }
        }

        if (detailTransition.isListInteractive()) {

            for (card in addonCardCache) {
                val cardY = getY() + card.y + scroll.getValue()
                val controlLayout = ModMenuListCardLayout.build(
                    cardX = card.x,
                    cardY = cardY,
                    cardWidth = card.width,
                    cardHeight = card.height,
                    settingsSize = SETTINGS_SIZE,
                    settingsPaddingFromRight = ADDON_SETTINGS_PADDING,
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
                    val addon = card.addon

                    if (!card.isFailed && addon != null && controlLayout.isSettingsHit(
                            mouseX,
                            mouseY
                        ) && detailTransition.isListInteractive()
                    ) {
                        val settings: ArrayList<Setting>? = addonManager.getSettingByAddon(addon)
                        if (settings != null) {
                            settingsPanel.buildEntries(settings)
                            settingScroll.resetAll()
                            currentAddon = addon
                            detailTransition.open()
                            setCanClose(false)
                        }
                        continue
                    }

                    if (!card.isFailed && addon != null && addon.showToggle && controlLayout.isToggleHit(
                            mouseX,
                            mouseY
                        )
                    ) {
                        addon.toggle()
                    }
                    continue
                }
            }
        }

        if (detailTransition.isDetailsInteractive()) {
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
        if (currentAddon != null && detailTransition.isDetailsInteractive()) {
            applySettingsPanelPreferences()
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentAddon != null && detailTransition.isDetailsInteractive()) {
            applySettingsPanelPreferences()
            settingsPanel.keyTyped(typedChar, keyCode)
        }

        if (isDetailsLayerOpen() && keyCode == Keyboard.KEY_ESCAPE) {
            detailTransition.close()
            return
        }

        if (detailTransition.isListInteractive()) {
            scroll.onKey(keyCode)
            if (keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_ESCAPE) {
                getSearchBox().setFocused(true)
            }
        }
    }

    override fun drawTopFilters(context: ModMenuListPageRenderContext): Float {
        return drawTypeChips(
            nvg = context.nvg,
            palette = context.palette,
            accentColor = context.accent,
            scrollOffset = context.scrollOffset,
            mouseX = context.mouseX,
            mouseY = context.mouseY
        )
    }

    override fun rebuildFilteredEntries(topFiltersBottom: Float) {
        rebuildAddonCards(Shindo.getInstance().addonManager, topFiltersBottom)
    }

    override fun resolveCardLayoutSpec(): ModMenuListCardLayoutSpec {
        if (addonCardCache.isNotEmpty()) {
            val first = addonCardCache[0]
            return ModMenuListCardLayoutSpec(
                columns = 1,
                cardWidth = first.width,
                cardHeight = first.height,
                spacingX = 0f,
                spacingY = 14f
            )
        }

        return ModMenuListCardLayoutSpec(
            columns = 1,
            cardWidth = getWidth() - 30f,
            cardHeight = LIST_CARD_HEIGHT,
            spacingX = 0f,
            spacingY = 14f
        )
    }

    override fun drawEntryCards(context: ModMenuListPageRenderContext, layout: ModMenuListCardLayoutSpec) {
        if (addonCardCache.isEmpty()) {
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

        val addonManager = Shindo.getInstance().addonManager

        for (card in addonCardCache) {
            val addon = card.addon
            val cardY = getY() + card.y
            val isExternal = !card.isFailed && addon != null && addon.isBuiltIn.not()
            val indicatorWidth = if (isExternal || card.isFailed) LOAD_INDICATOR_WIDTH else 0f
            val iconX = card.x + indicatorWidth + LIST_ICON_LEFT_PADDING
            val iconY = cardY + (card.height - LIST_ICON_SIZE) / 2f

            val showToggleForCard = !card.isFailed && addon != null && addon.showToggle
            val hasSettings = !card.isFailed && addon != null && addonManager.getSettingByAddon(addon) != null
            val controlLayout = ModMenuListCardLayout.build(
                cardX = card.x,
                cardY = cardY,
                cardWidth = card.width,
                cardHeight = card.height,
                settingsSize = SETTINGS_SIZE,
                settingsPaddingFromRight = ADDON_SETTINGS_PADDING,
                toggleWidth = LIST_TOGGLE_WIDTH,
                toggleHeight = LIST_TOGGLE_HEIGHT,
                settingsGap = LIST_TOGGLE_GAP
            )
            val hitboxLayout = controlLayout.withOffset(context.scrollOffset)

            card.hasSettings = hasSettings

            val textX = iconX + LIST_ICON_SIZE + 10f
            val textRight = when {
                card.isFailed -> card.x + card.width - 18f
                hasSettings -> controlLayout.settingsX - LIST_TOGGLE_GAP
                showToggleForCard -> controlLayout.toggleX - LIST_TOGGLE_GAP
                else -> card.x + card.width - 18f
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

            val hoverProgress = if (card.isFailed || addon == null) {
                0f
            } else {
                addon.hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, ModMenuMotion.CARD_HOVER_SPEED)
                addon.hoverAnimation.value
            }

            val settingsHover = hasSettings && hitboxLayout.isSettingsHit(context.mouseX, context.mouseY)
            if (!card.isFailed && addon != null) {
                addon.settingsHoverAnimation.setAnimation(
                    if (settingsHover) 1.0f else 0.0f,
                    ModMenuMotion.CARD_HOVER_SPEED
                )
            }
            val settingsHoverAnimation = if (card.isFailed || addon == null) 0f else addon.settingsHoverAnimation.value

            AddonCategoryRenderer.drawCardShell(
                nvg = context.nvg,
                palette = context.palette,
                accent = context.accent,
                x = card.x,
                y = cardY,
                width = card.width,
                height = card.height,
                hoverProgress = hoverProgress,
                indicatorWidth = indicatorWidth,
                failed = card.isFailed
            )

            if (card.isFailed) {
                val failedName = context.nvg.getLimitText(
                    TextUtils.stripUnicodeAccents(card.failedEntry!!.jarFileName),
                    11.5f,
                    Fonts.MEDIUM,
                    textWidth
                )
                val failedDesc = context.nvg.getLimitText("Falhou ao carregar", 8.5f, Fonts.REGULAR, textWidth)
                AddonCategoryRenderer.drawFailedText(
                    nvg = context.nvg,
                    palette = context.palette,
                    textX = textX,
                    cardY = cardY,
                    failedName = failedName,
                    failedDescription = failedDesc
                )
                continue
            }

            if (addon == null) {
                continue
            }
            addon.animation.setAnimation(if (addon.isToggled()) 1.0f else 0.0f, ModMenuMotion.CARD_TOGGLE_SPEED)
            val toggleProgress = addon.animation.value

            val safeName = TextUtils.stripUnicodeAccents(addon.name)
            val name = context.nvg.getLimitText(safeName, 11.5f, Fonts.MEDIUM, textWidth)
            val safeDesc = TextUtils.stripUnicodeAccents(addon.getDescription())
            val description = context.nvg.getLimitText(safeDesc, 8.5f, Fonts.REGULAR, textWidth)
            AddonCategoryRenderer.drawAddonIdentity(
                nvg = context.nvg,
                palette = context.palette,
                icon = addon.icon,
                iconCenterX = iconX + LIST_ICON_SIZE / 2f,
                iconCenterY = iconY + LIST_ICON_SIZE / 2f - LIST_ICON_FONT_OFFSET,
                name = name,
                textX = textX,
                cardY = cardY,
                description = description,
                builtIn = addon.isBuiltIn
            )

            AddonCategoryRenderer.drawCardControls(
                nvg = context.nvg,
                palette = context.palette,
                accent = context.accent,
                hasSettings = hasSettings,
                settingsX = controlLayout.settingsX,
                settingsY = controlLayout.settingsY,
                settingsSize = SETTINGS_SIZE,
                settingsHoverProgress = settingsHoverAnimation,
                showToggle = showToggleForCard,
                toggleX = controlLayout.toggleX,
                toggleY = controlLayout.toggleY,
                toggleWidth = controlLayout.toggleWidth,
                toggleHeight = controlLayout.toggleHeight,
                toggleProgress = toggleProgress
            )
        }
    }

    override fun isDetailsLayerOpen(): Boolean {
        return currentAddon != null
    }

    override fun drawDetailsLayer(context: ModMenuListPageRenderContext) {
        val activeAddon = currentAddon ?: return

        if (detailTransition.isDetailsInteractive() && MouseUtils.isInside(
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
            title = TextUtils.stripUnicodeAccents(activeAddon.name),
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


        for (type in AddonType.values()) {
            val label = type.getName()
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null)

            if (currentX + chipWidth > maxX) {
                currentX = startX
                currentY += CategoryChipRenderer.CHIP_HEIGHT + TYPE_CHIP_GAP
                blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT
            }

            val active = type == currentType
            val hovered =
                detailTransition.isListInteractive() && MouseUtils.isInside(
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
        if (a.isHide()) {
            return true
        }

        if (currentType != AddonType.ALL && a.type != currentType) {
            return true
        }

        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(
            Shindo.getInstance().addonManager.getWords(a),
            getSearchBox().getText()
        )
    }

    private fun filterFailedAddon(failed: FailedAddonEntry): Boolean {
        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(
            failed.jarFileName,
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
        val addon: Addon?,
        val failedEntry: FailedAddonEntry?,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        var hasSettings: Boolean = false
    ) {
        val isFailed: Boolean get() = failedEntry != null
    }

    private fun applySettingsPanelPreferences() {
        val settings = InternalSettingsMod.instance
        ModMenuSettingsOverlayRenderer.configureSettingsPanel(
            panel = settingsPanel,
            panelStyle = ADDON_SETTINGS_PANEL_STYLE,
            layoutMode = settings.settingsLayoutMode
        )
    }

    private fun rebuildAddonCards(addonManager: AddonManager, startOffset: Float) {
        addonCardCache.clear()

        val availableWidth = getWidth() - 30f
        val cardHeight = LIST_CARD_HEIGHT
        val spacingY = 14f

        var rowY = startOffset

        for (addon in addonManager.addons) {
            if (filterAddon(addon)) continue
            val cardX = getX() + 15f
            addonCardCache.add(AddonCard(addon, null, cardX, rowY, availableWidth, cardHeight))
            rowY += cardHeight + spacingY
        }

        if (currentType == AddonType.ALL) {
            for (failed in addonManager.failedAddons) {
                if (filterFailedAddon(failed)) continue
                val cardX = getX() + 15f
                addonCardCache.add(AddonCard(null, failed, cardX, rowY, availableWidth, cardHeight))
                rowY += cardHeight + spacingY
            }
        }

        if (addonCardCache.isEmpty()) {
            contentHeight = max(0f, startOffset - 13f)
            return
        }

        val last = addonCardCache[addonCardCache.size - 1]
        val lastBottom = last.y + last.height
        contentHeight = max(0f, lastBottom - 13f)
    }

    private companion object {
        const val TYPE_CHIP_GAP = 8f
        const val LOAD_INDICATOR_WIDTH = 4f
        const val LIST_CARD_HEIGHT = 51.84f
        const val LIST_TOGGLE_WIDTH = 44f
        const val LIST_TOGGLE_HEIGHT = 18f
        const val LIST_TOGGLE_GAP = 6f
        const val ADDON_SETTINGS_PADDING = 18f
        const val LIST_ICON_SIZE = 26f
        const val LIST_ICON_LEFT_PADDING = 20f
        const val LIST_ICON_FONT_SIZE = 24f
        const val LIST_ICON_FONT_OFFSET = 7.5f
        const val SETTINGS_SIZE = 18f

        val ADDON_SETTINGS_PANEL_STYLE = SettingsPanelStyle(
            cardPaddingX = 15f,
            cardPaddingY = 11f,
            rowGap = 7f,
            categoryGap = 13f
        )
    }
}
