package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.network.NetworkSection
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.network.NetworkManager
import me.miki.shindo.management.network.NetworkManager.LinkMedium
import me.miki.shindo.management.network.NetworkManager.ProfileSnapshot
import me.miki.shindo.management.network.proxy.WarpProxyManager
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.CategorySetting
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.impl.combo.Option
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.ui.comp.inputs.CompDropdown
import me.miki.shindo.ui.comp.inputs.CompSlider
import me.miki.shindo.ui.comp.buttons.CompToggleButton
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Arrays

class NetworkCategory(parent: GuiModMenu) : Category(parent, TranslateText.NETWORK, LegacyIcon.GLOBE, false, true) {

    private val navigationChips = ArrayList<FilterChip>()
    private val heroGlow = SimpleAnimation()
    private val df = DecimalFormat("0.0")
    private val settingsPanel = SettingsPanel()
    private val settingsScroll = Scroll()
    private var settingsAnimation: Animation? = null

    private var manager: NetworkManager? = null
    private var snapshot: ProfileSnapshot? = null
    private var baselineSnapshot: ProfileSnapshot? = null
    private var activeSection = NetworkSection.TWEAKER
    private var slideAnimation: Animation? = null
    private var dropdownOpen = false
    private var dropdownX = 0f
    private var dropdownY = 0f
    private var dropdownW = 0f
    private var dropdownH = 0f
    private var mediumSetting: ComboSetting? = null
    private var mediumDropdown: CompDropdown? = null
    private var capacitySetting: NumberSetting? = null
    private var capacitySlider: CompSlider? = null
    private var responsivenessSetting: NumberSetting? = null
    private var responsivenessSlider: CompSlider? = null
    private var dynamicSetting: BooleanSetting? = null
    private var burstSetting: BooleanSetting? = null
    private var autoFlushSetting: BooleanSetting? = null
    private var dynamicToggle: CompToggleButton? = null
    private var burstToggle: CompToggleButton? = null
    private var autoFlushToggle: CompToggleButton? = null
    private var jitterSetting: NumberSetting? = null
    private var jitterSlider: CompSlider? = null
    private var runningSpeedTest = false
    private var runningLatency = false
    private var settingsOpen = false
    private var optimizerButtonX = 0f
    private var optimizerButtonY = 0f
    private var optimizerButtonW = 0f
    private var optimizerButtonH = 0f
    private var settingsButtonX = 0f
    private var settingsButtonY = 0f
    private var settingsButtonW = 0f
    private var settingsButtonH = 0f
    private var warpButtonX = 0f
    private var warpButtonY = 0f
    private var warpButtonW = 0f
    private var warpButtonH = 0f

    override fun initGui() {
        manager = Shindo.getInstance().connectionTweakerManager
        snapshot = manager?.profileSnapshot
        if (baselineSnapshot == null) {
            baselineSnapshot = snapshot
        }
        slideAnimation = SmoothStepAnimation(240, 1.0)
        settingsAnimation = SmoothStepAnimation(260, 1.0)
        settingsAnimation?.setValue(1.0)
        settingsOpen = false
        settingsPanel.clear()
        settingsScroll.resetAll()
        buildControls()
    }

    override fun initCategory() {
        snapshot = manager?.profileSnapshot
        settingsOpen = false
        settingsPanel.clear()
        settingsScroll.resetAll()
        if (settingsAnimation == null) {
            settingsAnimation = SmoothStepAnimation(260, 1.0)
        }
        settingsAnimation?.setValue(1.0)
        buildControls()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val localManager = manager ?: return
        snapshot = localManager.profileSnapshot
        val nvg = Shindo.getInstance().nanoVGManager!!
        val cm = Shindo.getInstance().colorManager
        val palette = cm.palette
        val accent = cm.currentColor

        val viewportX = getX().toFloat()
        val viewportY = getY().toFloat()
        val viewportW = getWidth().toFloat()
        val viewportH = getHeight().toFloat()
        val settingsAnimating = settingsAnimation != null && !(settingsAnimation?.isDone(Direction.FORWARDS) ?: false)
        val overlayActive = settingsOpen || settingsAnimating
        val slideOffset = if (settingsAnimation != null) (-(600 - (settingsAnimation?.getValue() ?: 0.0) * 600)).toFloat() else 0f

        nvg.save()
        nvg.scissor(viewportX, viewportY, viewportW, viewportH)

        val scrollY = scroll.getValue()
        val contentMouseY = (mouseY - scrollY).toInt()

        navigationChips.clear()
        val tabH = drawTabs(nvg, palette, accent, viewportX, viewportY, viewportW, mouseX, mouseY, scrollY, slideOffset)
        val contentTop = viewportY + tabH + 12f
        val contentH = viewportH - (contentTop - viewportY)
        scroll.maxScroll = kotlin.math.max(0f, computeContentHeight() - contentH)
        if (!overlayActive && MouseUtils.isInside(mouseX, mouseY, viewportX, viewportY, viewportW, viewportH)) {
            scroll.onScroll()
            scroll.onAnimation()
        }

        nvg.save()
        nvg.translate(slideOffset, 0f)
        nvg.translate(0f, scrollY)
        if (activeSection == NetworkSection.TWEAKER) {
            drawTweaker(nvg, palette, accent, viewportX, contentTop, viewportW, mouseX, contentMouseY)
        } else {
            drawProxy(nvg, palette, accent, viewportX, contentTop, viewportW, mouseX, contentMouseY)
        }
        nvg.restore()
        nvg.restore()

        if (overlayActive) {
            drawSettingsPanel(nvg, palette, accent, mouseX, mouseY, partialTicks)
        }
    }
    private fun drawTabs(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        mouseX: Int,
        mouseY: Int,
        scrollOffset: Float,
        slideOffset: Float
    ): Float {
        val chipGap = 10f
        val startX = x + CONTENT_PADDING
        var currentX = startX
        val currentY = y + 6f
        for (section in NetworkSection.values()) {
            val label = section.getLabel()
            val icon = section.icon
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, icon)
            val active = section == activeSection
            val drawX = currentX + slideOffset
            val drawY = currentY + scrollOffset
            val hovered = MouseUtils.isInside(mouseX, mouseY, drawX, drawY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            CategoryChipRenderer.drawChip(nvg, palette, accent, drawX, drawY, chipWidth, label, icon, active, hovered)
            val chip = FilterChip(Runnable { activateSection(section) })
            chip.setBounds(drawX, drawY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            navigationChips.add(chip)
            currentX += chipWidth + chipGap
        }
        return currentY + CategoryChipRenderer.CHIP_HEIGHT - y
    }

    private fun drawTweaker(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        baseX: Float,
        baseY: Float,
        width: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        var cursorY = baseY + CONTENT_PADDING
        heroGlow.setAnimation(if (snapshot?.isOptimizerEnabled == true) 1f else 0.5f, 20.0)

        cursorY = drawHero(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2f, mouseX, mouseY) + CARD_GAP
        cursorY = drawFocusChart(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2f) + CARD_GAP
        cursorY = drawAdvancedSettings(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2f, mouseX, mouseY) + CARD_GAP
    }

    private fun drawProxy(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        baseX: Float,
        baseY: Float,
        width: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        val cardX = baseX + CONTENT_PADDING
        val cardW = width - CONTENT_PADDING * 2f
        val cardY = baseY + CONTENT_PADDING
        val bg = palette.getBackgroundColor(ColorType.DARK)
        nvg.drawRoundedRect(cardX, cardY, cardW, 120f, CARD_RADIUS, bg)
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP.text, cardX + 16f, cardY + 18f, palette.getFontColor(ColorType.DARK), 14f, Fonts.SEMIBOLD)
        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.text, cardX + 16f, cardY + 36f, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9.5f, Fonts.REGULAR)
        if (manager != null) {
            val warp = Shindo.getInstance().warpProxyManager
            val status = warp?.diagnostics?.status?.name ?: "UNKNOWN"
            nvg.drawText(status, cardX + 16f, cardY + 54f, accent.color1, 11f, Fonts.MEDIUM)
        }
        val warpEnabled = manager?.isWarpProxyEnabled == true
        warpButtonW = 150f
        warpButtonH = BUTTON_HEIGHT
        warpButtonX = cardX + cardW - warpButtonW - 16f
        warpButtonY = cardY + 60f
        val warpHovered = MouseUtils.isInside(mouseX, mouseY, warpButtonX, warpButtonY, warpButtonW, warpButtonH)
        val warpBg = if (warpEnabled) {
            if (warpHovered) ColorUtils.applyAlpha(accent.color1, 220) else ColorUtils.applyAlpha(accent.color1, 180)
        } else {
            if (warpHovered) palette.getBackgroundColor(ColorType.NORMAL) else palette.getBackgroundColor(ColorType.MID)
        }
        nvg.drawRoundedRect(warpButtonX, warpButtonY, warpButtonW, warpButtonH, 8f, warpBg)
        nvg.drawText(LegacyIcon.DNS, warpButtonX + 10f, warpButtonY + 6f, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
        val warpLabel = if (warpEnabled) "Disable WARP" else "Enable WARP"
        nvg.drawText(warpLabel, warpButtonX + 30f, warpButtonY + 8f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
    }

    private fun drawHero(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        w: Float,
        mouseX: Int,
        mouseY: Int
    ): Float {
        val base = palette.getBackgroundColor(ColorType.DARK)
        val glow = ColorUtils.applyAlpha(accent.color1, (heroGlow.value * 90).toInt())
        nvg.drawRoundedRect(x, y, w, HERO_HEIGHT, CARD_RADIUS, base)
        nvg.drawGradientRoundedRect(x, y, w, HERO_HEIGHT, CARD_RADIUS, glow, ColorUtils.applyAlpha(accent.color2, 80))

        val titleY = y + 18f
        nvg.drawText(TranslateText.NETWORK.text, x + 18f, titleY, palette.getFontColor(ColorType.DARK), 16f, Fonts.SEMIBOLD)
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_SUMMARY.text, x + 18f, titleY + 18f, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 10f, Fonts.REGULAR)

        var sliderY = titleY + 55f
        if (capacitySetting != null && capacitySlider != null) {
            nvg.drawText(TranslateText.NETWORK_LINK_CAPACITY.text, x + 18f, sliderY - 10f, palette.getFontColor(ColorType.NORMAL), 9.5f, Fonts.MEDIUM)
            capacitySetting?.setValue(manager?.linkCapacityMbps?.toDouble() ?: 0.0)
            capacitySlider?.setX(x + 18f)
            capacitySlider?.setY(sliderY)
            capacitySlider?.setWidth(w - 36f)
            capacitySlider?.draw(mouseX, mouseY, 0f)
            sliderY += 34f
            manager?.setLinkCapacityMbps(capacitySetting?.getValueInt() ?: 0)
        }
        if (responsivenessSetting != null && responsivenessSlider != null) {
            nvg.drawText(TranslateText.NETWORK_RESPONSIVENESS.text, x + 18f, sliderY - 10f, palette.getFontColor(ColorType.NORMAL), 9.5f, Fonts.MEDIUM)
            responsivenessSetting?.setValue(manager?.responsivenessLevel?.toDouble() ?: 0.0)
            responsivenessSlider?.setX(x + 18f)
            responsivenessSlider?.setY(sliderY)
            responsivenessSlider?.setWidth(w - 36f)
            responsivenessSlider?.draw(mouseX, mouseY, 0f)
            sliderY += 34f
            manager?.setResponsivenessLevel(responsivenessSetting?.getValueInt() ?: 0)
        }

        val toggleW = 120f
        val toggleX = x + w - toggleW - 18f
        val buttonsY = sliderY + 10f
        val maxButtonsY = y + HERO_HEIGHT - BUTTON_HEIGHT - 10f
        val toggleY = kotlin.math.min(buttonsY, maxButtonsY)
        val on = snapshot?.isOptimizerEnabled == true
        val toggleHovered = MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, toggleW, BUTTON_HEIGHT)
        val toggleBg = if (on) {
            if (toggleHovered) ColorUtils.applyAlpha(accent.color1, 220) else ColorUtils.applyAlpha(accent.color1, 200)
        } else {
            if (toggleHovered) palette.getBackgroundColor(ColorType.NORMAL) else palette.getBackgroundColor(ColorType.MID)
        }
        nvg.drawRoundedRect(toggleX, toggleY, toggleW, BUTTON_HEIGHT, 8f, toggleBg)
        nvg.drawText(LegacyIcon.POWER, toggleX + 10f, toggleY + 6f, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
        nvg.drawText(if (on) "ON" else "OFF", toggleX + 32f, toggleY + 8f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        optimizerButtonX = toggleX
        optimizerButtonY = toggleY
        optimizerButtonW = toggleW
        optimizerButtonH = BUTTON_HEIGHT

        settingsButtonW = 120f
        settingsButtonH = BUTTON_HEIGHT
        settingsButtonX = toggleX - settingsButtonW - 10f
        settingsButtonY = toggleY
        drawButton(nvg, palette, accent, settingsButtonX, settingsButtonY, settingsButtonW, settingsButtonH, TranslateText.SETTINGS.text, LegacyIcon.SETTINGS, true, mouseX, mouseY, null)

        dropdownX = x + 18f
        dropdownY = settingsButtonY
        dropdownW = 80f
        dropdownH = settingsButtonH

        if (mediumSetting != null && mediumDropdown != null) {
            mediumSetting?.setOption(mediumSetting?.getOption())
            mediumDropdown?.setX(dropdownX)
            mediumDropdown?.setY(dropdownY)
            mediumDropdown?.setWidth(dropdownW)
            mediumDropdown?.setHeight(dropdownH)
            mediumDropdown?.setOpenUp(true)
            mediumDropdown?.draw(mouseX, mouseY, 0f)
        }

        return y + HERO_HEIGHT
    }

    private fun drawFocusChart(nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, x: Float, y: Float, w: Float): Float {
        val h = CHART_HEIGHT
        nvg.drawRoundedRect(x, y, w, h, CARD_RADIUS, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.text, x + 16f, y + 16f, palette.getFontColor(ColorType.DARK), 11f, Fonts.SEMIBOLD)

        val barX = x + 16f
        val barY = y + 36f
        val barW = w - 32f
        val barH = 12f
        val focuses = floatArrayOf(
            snapshot?.latencyFocus ?: 0.5f,
            snapshot?.stabilityFocus ?: 0.5f,
            snapshot?.throughputFocus ?: 0.5f
        )
        val labels = arrayOf(
            TranslateText.NETWORK_LATENCY_FOCUS,
            TranslateText.NETWORK_STABILITY_FOCUS,
            TranslateText.NETWORK_THROUGHPUT_FOCUS
        )
        for (i in focuses.indices) {
            val fy = barY + i * (barH + 14f)
            nvg.drawText(labels[i].text, barX, fy - 2f, palette.getFontColor(ColorType.NORMAL), 9.5f, Fonts.REGULAR)
            nvg.drawRoundedRect(barX, fy + 8f, barW, barH, 6f, palette.getBackgroundColor(ColorType.MID))
            nvg.drawRoundedRect(barX, fy + 8f, barW * focuses[i], barH, 6f, ColorUtils.applyAlpha(accent.color1, 220))
            nvg.drawText(df.format(focuses[i] * 100) + "%", barX + barW - 40f, fy + 10f, palette.getFontColor(ColorType.DARK), 9f, Fonts.MEDIUM)
        }
        return y + h
    }

    private fun drawButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        label: String,
        icon: String,
        enabled: Boolean,
        mouseX: Int,
        mouseY: Int,
        action: Runnable?
    ) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, w, h)
        val bg = if (enabled) {
            if (hovered) ColorUtils.applyAlpha(accent.color1, 220) else ColorUtils.applyAlpha(accent.color1, 180)
        } else {
            palette.getBackgroundColor(ColorType.MID)
        }
        nvg.drawRoundedRect(x, y, w, h, 8f, bg)
        nvg.drawText(icon, x + 10f, y + h / 2f - 7f, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
        nvg.drawText(label, x + 30f, y + h / 2f - 5f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        if (enabled && hovered && action != null && Mouse.isButtonDown(0)) {
            action.run()
        }
    }
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val localManager = manager ?: return
        val settingsAnimating = settingsAnimation != null && !(settingsAnimation?.isDone(Direction.FORWARDS) ?: false)
        val overlayActive = settingsOpen || settingsAnimating
        if (overlayActive) {
            if (handleSettingsClick(mouseX, mouseY, mouseButton)) {
                return
            }
        }

        val scrollY = scroll.getValue()
        val contentMouseY = (mouseY - scrollY).toInt()

        if (mouseButton == 0) {
            for (chip in navigationChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click()
                    return
                }
            }
        }
        if (activeSection == NetworkSection.TWEAKER) {
            if (mediumDropdown != null) {
                mediumDropdown?.mouseClicked(mouseX, contentMouseY, mouseButton)
                val selected = if (mediumSetting != null && mediumSetting?.getOption() != null) {
                    LinkMedium.fromKey(mediumSetting?.getOption()?.nameKey)
                } else {
                    null
                }
                if (selected != null) {
                    localManager.setNetworkMedium(selected)
                }
            }
            if (capacitySlider != null) {
                capacitySlider?.mouseClicked(mouseX, contentMouseY, mouseButton)
                localManager.setLinkCapacityMbps(capacitySetting?.getValueInt() ?: 0)
            }
            if (responsivenessSlider != null) {
                responsivenessSlider?.mouseClicked(mouseX, contentMouseY, mouseButton)
                localManager.setResponsivenessLevel(responsivenessSetting?.getValueInt() ?: 0)
            }
            if (MouseUtils.isInside(mouseX, contentMouseY, dropdownX, dropdownY, dropdownW, dropdownH)) {
                dropdownOpen = !dropdownOpen
            }
            if (mouseButton == 0 && MouseUtils.isInside(mouseX, contentMouseY, optimizerButtonX, optimizerButtonY, optimizerButtonW, optimizerButtonH)) {
                val newState = snapshot == null || snapshot?.isOptimizerEnabled != true
                localManager.setOptimizerEnabled(newState)
            }
            if (mouseButton == 0 && MouseUtils.isInside(mouseX, contentMouseY, settingsButtonX, settingsButtonY, settingsButtonW, settingsButtonH)) {
                openSettingsPanel()
                return
            }
            if (dynamicToggle != null) {
                dynamicToggle?.mouseClicked(mouseX, contentMouseY, mouseButton)
                localManager.setDynamicFlushEnabled(dynamicSetting?.isToggled() ?: false)
            }
            if (burstToggle != null) {
                burstToggle?.mouseClicked(mouseX, contentMouseY, mouseButton)
                localManager.setBurstFlushSmoothing(burstSetting?.isToggled() ?: false)
            }
            if (autoFlushToggle != null) {
                autoFlushToggle?.mouseClicked(mouseX, contentMouseY, mouseButton)
                localManager.setAutoFlushEnabled(autoFlushSetting?.isToggled() ?: false)
            }
            if (jitterSlider != null) {
                jitterSlider?.mouseClicked(mouseX, contentMouseY, mouseButton)
                localManager.setJitterSensitivity((jitterSetting?.getValue() ?: 0.0).toInt())
            }
        } else if (mouseButton == 0 && MouseUtils.isInside(mouseX, contentMouseY, warpButtonX, warpButtonY, warpButtonW, warpButtonH)) {
            localManager.setWarpProxyEnabled(!localManager.isWarpProxyEnabled)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val settingsAnimating = settingsAnimation != null && !(settingsAnimation?.isDone(Direction.FORWARDS) ?: false)
        if (settingsOpen) {
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingsScroll)
            return
        }
        if (settingsAnimating) {
            return
        }
        val contentMouseY = (mouseY - scroll.getValue()).toInt()
        mediumDropdown?.mouseReleased(mouseX, contentMouseY, mouseButton)
        capacitySlider?.mouseReleased(mouseX, contentMouseY, mouseButton)
        responsivenessSlider?.mouseReleased(mouseX, contentMouseY, mouseButton)
        dynamicToggle?.mouseReleased(mouseX, contentMouseY, mouseButton)
        burstToggle?.mouseReleased(mouseX, contentMouseY, mouseButton)
        autoFlushToggle?.mouseReleased(mouseX, contentMouseY, mouseButton)
        jitterSlider?.mouseReleased(mouseX, contentMouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        val settingsAnimating = settingsAnimation != null && !(settingsAnimation?.isDone(Direction.FORWARDS) ?: false)
        if (settingsOpen) {
            settingsPanel.keyTyped(typedChar, keyCode)
            if (keyCode == Keyboard.KEY_ESCAPE) {
                closeSettingsPanel()
            }
            return
        }
        if (settingsAnimating) {
            return
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            dropdownOpen = false
        }
        scroll.onKey(keyCode)
    }

    private fun handleSettingsClick(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        val layout = getSettingsLayout()
        val offsetX = if (settingsAnimation != null) (settingsAnimation?.getValue() ?: 0.0).toFloat() * 600f else 0f
        val closeSize = 16f
        val closeX = layout.x + layout.width - closeSize - 12f
        val closeY = layout.y + 12f
        if (mouseButton == 0 && MouseUtils.isInside(mouseX, mouseY, closeX - 4f + offsetX, closeY - 4f, closeSize + 8f, closeSize + 8f)) {
            closeSettingsPanel()
            return true
        }
        if (MouseUtils.isInside(mouseX, mouseY, layout.x + offsetX, layout.y, layout.width, layout.height)) {
            settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, layout.contentX, layout.contentY, layout.contentWidth, layout.viewportHeight, settingsScroll)
            return true
        }
        if (mouseButton == 0) {
            closeSettingsPanel()
            return true
        }
        return false
    }

    private fun openSettingsPanel() {
        if (manager == null) {
            return
        }
        settingsPanel.setLayoutMode(SettingsPanel.LayoutMode.SINGLE_COLUMN)
        settingsPanel.clear()
        settingsPanel.buildEntries(getFilteredSettings())
        settingsScroll.resetAll()
        settingsOpen = true
        settingsAnimation?.setDirection(Direction.BACKWARDS)
        setCanClose(false)
    }

    private fun closeSettingsPanel() {
        settingsOpen = false
        settingsPanel.clear()
        settingsScroll.resetAll()
        settingsAnimation?.setDirection(Direction.FORWARDS)
        setCanClose(true)
    }

    private fun activateSection(section: NetworkSection) {
        if (section == activeSection) {
            return
        }
        activeSection = section
        slideAnimation?.setDirection(Direction.FORWARDS)
        dropdownOpen = false
        scroll.resetAll()
    }

    private fun drawSettingsPanel(nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (settingsAnimation == null) {
            return
        }
        settingsAnimation?.setDirection(if (settingsOpen) Direction.BACKWARDS else Direction.FORWARDS)
        val layout = getSettingsLayout()
        val offsetX = (settingsAnimation?.getValue() ?: 0.0).toFloat() * 600f
        if (MouseUtils.isInside(mouseX, mouseY, layout.x + offsetX, layout.y, layout.width, layout.height)) {
            settingsScroll.onScroll()
        }
        settingsScroll.onAnimation()

        nvg.save()
        nvg.translate(offsetX, 0f)
        nvg.drawShadow(layout.x, layout.y, layout.width, layout.height, 12f, 7)
        nvg.drawRoundedRect(layout.x, layout.y, layout.width, layout.height, 12f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
        nvg.drawRoundedRect(layout.x + 1f, layout.y + 1f, layout.width - 2f, layout.height - 2f, 11f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

        nvg.drawText(TranslateText.NETWORK.text, layout.x + 16f, layout.y + 16f, palette.getFontColor(ColorType.DARK), 13f, Fonts.SEMIBOLD)
        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.text, layout.x + 16f, layout.y + 32f, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9f, Fonts.REGULAR)

        val closeSize = 16f
        val closeX = layout.x + layout.width - closeSize - 12f
        val closeY = layout.y + 12f
        val closeHovered = MouseUtils.isInside(mouseX, mouseY, closeX - 4f + offsetX, closeY - 4f, closeSize + 8f, closeSize + 8f)
        if (closeHovered) {
            nvg.drawRoundedRect(closeX - 4f, closeY - 4f, closeSize + 8f, closeSize + 8f, 6f, ColorUtils.applyAlpha(accent.color1, 80))
        }
        nvg.drawText(LegacyIcon.X, closeX, closeY, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)

        nvg.save()
        nvg.scissor(layout.x + 6f, layout.contentY - 6f, layout.width - 12f, layout.viewportHeight + 12f)
        settingsPanel.draw(mouseX, mouseY, partialTicks, layout.contentX, layout.contentY, layout.contentWidth, layout.viewportHeight, nvg, palette, settingsScroll)
        nvg.restore()
        nvg.restore()
    }

    private fun getSettingsLayout(): SettingsLayout {
        val layout = SettingsLayout()
        layout.x = getX() + 24f
        layout.y = getY() + 20f
        layout.width = getWidth() - 48f
        layout.height = getHeight() - 40f
        layout.contentX = layout.x + 14f
        layout.contentY = layout.y + 44f
        layout.contentWidth = layout.width - 28f
        layout.viewportHeight = layout.height - 58f
        return layout
    }
    private fun getFilteredSettings(): List<Setting> {
        val settings = SettingRegistry.getSettings(manager!!)
        if (settings.isEmpty()) {
            return settings ?: emptyList()
        }
        val hiddenKeys = mutableListOf(
            TranslateText.NETWORK_OPTIMIZER_TOGGLE.key,
            TranslateText.NETWORK_MEDIUM.key,
            TranslateText.NETWORK_LINK_CAPACITY.key,
            TranslateText.NETWORK_RESPONSIVENESS.key,
            TranslateText.NETWORK_DYNAMIC_FLUSH.key,
            TranslateText.NETWORK_BURST_SMOOTHING.key,
            TranslateText.NETWORK_AUTO_FLUSH.key,
            TranslateText.NETWORK_JITTER_SENSITIVITY.key,
            TranslateText.NETWORK_PROXY_WARP.key
        )
        val hiddenCategories = mutableListOf("overview", "routing")
        val filtered = ArrayList<Setting>()
        for (setting in settings) {
            if (setting is CategorySetting) {
                val t = setting.getTranslate()
                if (t == TranslateText.NETWORK_CATEGORY_OVERVIEW || t == TranslateText.NETWORK_CATEGORY_ROUTING) {
                    continue
                }
            }
            val key = setting.getNameKey()
            if (key != null) {
                val keyLower = key.toLowerCase()
                var hide = false
                for (hidden in hiddenKeys) {
                    if (hidden == null) {
                        continue
                    }
                    val hiddenLower = hidden.toLowerCase()
                    if (keyLower == hiddenLower || keyLower.endsWith(":$hiddenLower")) {
                        hide = true
                        break
                    }
                }
                if (hide) {
                    continue
                }
            }
            if (setting is CategorySetting) {
                val categoryKey = setting.getNameKey()
                var hideCategory = false
                for (hc in hiddenCategories) {
                    if (categoryKey == null || hc == null) {
                        continue
                    }
                    val catLower = categoryKey.toLowerCase()
                    val hcLower = hc.toLowerCase()
                    if (catLower == hcLower || catLower.endsWith(":$hcLower")) {
                        hideCategory = true
                        break
                    }
                }
                if (hideCategory) {
                    continue
                }
            }
            filtered.add(setting)
        }
        return filtered
    }

    private fun computeContentHeight(): Float {
        return CONTENT_PADDING * 2 + HERO_HEIGHT + CHART_HEIGHT + 120f + (CARD_GAP * 5)
    }

    private fun buildControls() {
        val localManager = manager ?: return
        val mediums = ArrayList<Option>()
        for (medium in LinkMedium.values()) {
            mediums.add(Option(medium.getTranslate()))
        }
        mediumSetting = ComboSetting(TranslateText.NETWORK_MEDIUM, localManager, LinkMedium.WIRED.getTranslate(), mediums)
        mediumDropdown = CompDropdown(200f, mediumSetting!!)

        capacitySetting = NumberSetting(TranslateText.NETWORK_LINK_CAPACITY, localManager, localManager.linkCapacityMbps.toDouble(), 10.0, 1000.0, true)
        capacitySlider = CompSlider(capacitySetting!!)

        responsivenessSetting = NumberSetting(TranslateText.NETWORK_RESPONSIVENESS, localManager, localManager.responsivenessLevel.toDouble(), 1.0, 10.0, true)
        responsivenessSlider = CompSlider(responsivenessSetting!!)

        dynamicSetting = BooleanSetting(TranslateText.NETWORK_DYNAMIC_FLUSH, localManager, localManager.isDynamicFlushEnabled)
        burstSetting = BooleanSetting(TranslateText.NETWORK_BURST_SMOOTHING, localManager, localManager.isBurstFlushSmoothingEnabled)
        autoFlushSetting = BooleanSetting(TranslateText.NETWORK_AUTO_FLUSH, localManager, localManager.isAutoFlushEnabled)
        dynamicToggle = CompToggleButton(dynamicSetting!!)
        burstToggle = CompToggleButton(burstSetting!!)
        autoFlushToggle = CompToggleButton(autoFlushSetting!!)

        jitterSetting = NumberSetting(TranslateText.NETWORK_JITTER_SENSITIVITY, localManager, localManager.jitterSensitivity.toDouble(), 1.0, 20.0, true)
        jitterSlider = CompSlider(jitterSetting!!)
        jitterSlider?.setShowValue(true)
    }

    private fun drawAdvancedSettings(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        w: Float,
        mouseX: Int,
        mouseY: Int
    ): Float {
        val cardH = 140f
        nvg.drawRoundedRect(x, y, w, cardH, CARD_RADIUS, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawText("Advanced Tuning", x + 16f, y + 16f, palette.getFontColor(ColorType.DARK), 12f, Fonts.SEMIBOLD)

        var rowY = y + 36f
        val rowX = x + 16f
        if (dynamicSetting != null && dynamicToggle != null) {
            dynamicSetting?.setToggled(manager?.isDynamicFlushEnabled ?: false)
            dynamicToggle?.setX(rowX + w - 60)
            dynamicToggle?.setY(rowY)
            dynamicToggle?.draw(mouseX, mouseY, 0f)
            nvg.drawText(TranslateText.NETWORK_DYNAMIC_FLUSH.text, rowX, rowY + 2f, palette.getFontColor(ColorType.NORMAL), 10f, Fonts.MEDIUM)
            rowY += 24f
        }
        if (burstSetting != null && burstToggle != null) {
            burstSetting?.setToggled(manager?.isBurstFlushSmoothingEnabled ?: false)
            burstToggle?.setX(rowX + w - 60)
            burstToggle?.setY(rowY)
            burstToggle?.draw(mouseX, mouseY, 0f)
            nvg.drawText(TranslateText.NETWORK_BURST_SMOOTHING.text, rowX, rowY + 2f, palette.getFontColor(ColorType.NORMAL), 10f, Fonts.MEDIUM)
            rowY += 24f
        }
        if (autoFlushSetting != null && autoFlushToggle != null) {
            autoFlushSetting?.setToggled(manager?.isAutoFlushEnabled ?: false)
            autoFlushToggle?.setX(rowX + w - 60)
            autoFlushToggle?.setY(rowY)
            autoFlushToggle?.draw(mouseX, mouseY, 0f)
            nvg.drawText(TranslateText.NETWORK_AUTO_FLUSH.text, rowX, rowY + 2f, palette.getFontColor(ColorType.NORMAL), 10f, Fonts.MEDIUM)
            rowY += 34f
        }
        if (jitterSetting != null && jitterSlider != null) {
            jitterSetting?.setValue(manager?.jitterSensitivity?.toDouble() ?: 0.0)
            nvg.drawText(TranslateText.NETWORK_JITTER_SENSITIVITY.text, rowX, rowY - 10f, palette.getFontColor(ColorType.NORMAL), 9.5f, Fonts.MEDIUM)
            jitterSlider?.setX(rowX)
            jitterSlider?.setY(rowY)
            jitterSlider?.setWidth(w - 40f)
            jitterSlider?.draw(mouseX, mouseY, 0f)
            manager?.setJitterSensitivity((jitterSetting?.getValue() ?: 0.0).toInt())
        }
        return y + cardH
    }

    private class SettingsLayout {
        var x = 0f
        var y = 0f
        var width = 0f
        var height = 0f
        var contentX = 0f
        var contentY = 0f
        var contentWidth = 0f
        var viewportHeight = 0f
    }

    private companion object {
        private const val CONTENT_PADDING = 18f
        private const val CARD_RADIUS = 14f
        private const val CARD_GAP = 12f
        private const val HERO_HEIGHT = 190f
        private const val CHART_HEIGHT = 120f
        private const val BUTTON_HEIGHT = 28f
    }
}
