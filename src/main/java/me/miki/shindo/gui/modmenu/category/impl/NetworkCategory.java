package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.network.NetworkSection;
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer;
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.network.NetworkDiagnostics;
import me.miki.shindo.management.network.NetworkDiagnostics.LatencyResult;
import me.miki.shindo.management.network.NetworkDiagnostics.SpeedResult;
import me.miki.shindo.management.network.NetworkManager;
import me.miki.shindo.management.network.NetworkManager.LinkMedium;
import me.miki.shindo.management.network.NetworkManager.ProfileSnapshot;
import me.miki.shindo.management.network.proxy.WarpProxyManager;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.impl.combo.Option;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.ui.comp.impl.CompDropdown;
import me.miki.shindo.ui.comp.impl.CompSlider;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Network screen with a more tool-like aesthetic: large hero, single-column cards, animated buttons and live
 * diagnostics (ping + lightweight speed test).
 */
public class NetworkCategory extends Category {

    private static final float CONTENT_PADDING = 18F;
    private static final float CARD_RADIUS = 14F;
    private static final float CARD_GAP = 12F;
    private static final float HERO_HEIGHT = 190F;
    private static final float CHART_HEIGHT = 120F;
    private static final float BUTTON_HEIGHT = 28F;
    private static final float LINE_HEIGHT = 26F;

    private final Scroll scroll = new Scroll();
    private final List<FilterChip> navigationChips = new ArrayList<>();
    private final SimpleAnimation heroGlow = new SimpleAnimation();
    private final DecimalFormat df = new DecimalFormat("0.0");
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final Scroll settingsScroll = new Scroll();
    private Animation settingsAnimation;

    private NetworkManager manager;
    private ProfileSnapshot snapshot;
    private ProfileSnapshot baselineSnapshot;
    private NetworkSection activeSection = NetworkSection.TWEAKER;
    private Animation slideAnimation;
    private boolean dropdownOpen;
    private float dropdownX;
    private float dropdownY;
    private float dropdownW;
    private float dropdownH;
    private ComboSetting mediumSetting;
    private CompDropdown mediumDropdown;
    private NumberSetting capacitySetting;
    private CompSlider capacitySlider;
    private NumberSetting responsivenessSetting;
    private CompSlider responsivenessSlider;
    private BooleanSetting dynamicSetting;
    private BooleanSetting burstSetting;
    private BooleanSetting autoFlushSetting;
    private CompToggleButton dynamicToggle;
    private CompToggleButton burstToggle;
    private CompToggleButton autoFlushToggle;
    private NumberSetting jitterSetting;
    private CompSlider jitterSlider;
    private boolean runningSpeedTest;
    private boolean runningLatency;
    private boolean settingsOpen;
    private float optimizerButtonX;
    private float optimizerButtonY;
    private float optimizerButtonW;
    private float optimizerButtonH;
    private float settingsButtonX;
    private float settingsButtonY;
    private float settingsButtonW;
    private float settingsButtonH;
    private float warpButtonX;
    private float warpButtonY;
    private float warpButtonW;
    private float warpButtonH;

    public NetworkCategory(GuiModMenu parent) {
        super(parent, TranslateText.NETWORK, LegacyIcon.GLOBE, false, true);
    }

    @Override
    public void initGui() {
        manager = Shindo.getInstance().getConnectionTweakerManager();
        snapshot = manager != null ? manager.getProfileSnapshot() : null;
        if (baselineSnapshot == null) {
            baselineSnapshot = snapshot;
        }
        slideAnimation = new SmoothStepAnimation(240, 1.0);
        settingsAnimation = new SmoothStepAnimation(260, 1.0);
        settingsAnimation.setValue(1.0);
        settingsOpen = false;
        settingsPanel.clear();
        settingsScroll.resetAll();
        buildControls();
    }

    @Override
    public void initCategory() {
        snapshot = manager != null ? manager.getProfileSnapshot() : null;
        settingsOpen = false;
        settingsPanel.clear();
        settingsScroll.resetAll();
        if (settingsAnimation == null) {
            settingsAnimation = new SmoothStepAnimation(260, 1.0);
        }
        settingsAnimation.setValue(1.0);
        buildControls();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (manager == null) {
            return;
        }
        snapshot = manager.getProfileSnapshot();
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorManager cm = Shindo.getInstance().getColorManager();
        ColorPalette palette = cm.getPalette();
        AccentColor accent = cm.getCurrentColor();

        float viewportX = getX();
        float viewportY = getY();
        float viewportW = getWidth();
        float viewportH = getHeight();
        boolean settingsAnimating = settingsAnimation != null && !settingsAnimation.isDone(Direction.FORWARDS);
        boolean overlayActive = settingsOpen || settingsAnimating;
        float slideOffset = settingsAnimation != null ? (float) -(600 - (settingsAnimation.getValue() * 600)) : 0F;

        // background scissor
        nvg.save();
        nvg.scissor(viewportX, viewportY, viewportW, viewportH);

        float scrollY = scroll.getValue();
        int contentMouseY = (int) (mouseY - scrollY);

        navigationChips.clear();
        float tabH = drawTabs(nvg, palette, accent, viewportX, viewportY, viewportW, mouseX, mouseY, scrollY, slideOffset);
        float contentTop = viewportY + tabH + 12F;
        float contentH = viewportH - (contentTop - viewportY);
        scroll.setMaxScroll(Math.max(0F, computeContentHeight() - contentH));
        if (!overlayActive && MouseUtils.isInside(mouseX, mouseY, viewportX, viewportY, viewportW, viewportH)) {
            scroll.onScroll();
            scroll.onAnimation();
        }


        nvg.save();
        nvg.translate(slideOffset, 0);
        nvg.translate(0, scrollY);
        if (activeSection == NetworkSection.TWEAKER) {
            drawTweaker(nvg, palette, accent, viewportX, contentTop, viewportW, mouseX, contentMouseY);
        } else {
            drawProxy(nvg, palette, accent, viewportX, contentTop, viewportW, mouseX, contentMouseY);
        }
        nvg.restore();
        nvg.restore();

        if (overlayActive) {
            drawSettingsPanel(nvg, palette, accent, mouseX, mouseY, partialTicks);
        }
    }

    private float drawTabs(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, int mouseX, int mouseY, float scrollOffset, float slideOffset) {
        float chipGap = 10F;
        float startX = x + CONTENT_PADDING;
        float currentX = startX;
        float currentY = y + 6F;
        for (NetworkSection section : NetworkSection.values()) {
            String label = section.getLabel();
            String icon = section.getIcon();
            float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, icon);
            boolean active = section == activeSection;
            float drawX = currentX + slideOffset;
            float drawY = currentY + scrollOffset;
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, drawX, drawY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            CategoryChipRenderer.drawChip(nvg, palette, accent, drawX, drawY, chipWidth, label, icon, active, hovered);
            FilterChip chip = new FilterChip(() -> activateSection(section));
            chip.setBounds(drawX, drawY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            navigationChips.add(chip);
            currentX += chipWidth + chipGap;
        }
        return currentY + CategoryChipRenderer.CHIP_HEIGHT - y;
    }

    private void drawTweaker(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float baseX, float baseY, float width, int mouseX, int mouseY) {
        float cursorY = baseY + CONTENT_PADDING;
        heroGlow.setAnimation(snapshot != null && snapshot.isOptimizerEnabled() ? 1F : 0.5F, 20);

        cursorY = drawHero(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2F, mouseX, mouseY) + CARD_GAP;
        cursorY = drawFocusChart(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2F) + CARD_GAP;
        cursorY = drawMetricsCards(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2F) + CARD_GAP;
        cursorY = drawAdvancedSettings(nvg, palette, accent, baseX + CONTENT_PADDING, cursorY, width - CONTENT_PADDING * 2F, mouseX, mouseY) + CARD_GAP;
    }

    private void drawProxy(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float baseX, float baseY, float width, int mouseX, int mouseY) {
        float cardX = baseX + CONTENT_PADDING;
        float cardW = width - CONTENT_PADDING * 2F;
        float cardY = baseY + CONTENT_PADDING;
        Color bg = palette.getBackgroundColor(ColorType.DARK);
        nvg.drawRoundedRect(cardX, cardY, cardW, 120F, CARD_RADIUS, bg);
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP.getText(), cardX + 16F, cardY + 18F, palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.getText(), cardX + 16F, cardY + 36F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9.5F, Fonts.REGULAR);
        if (manager != null) {
            WarpProxyManager warp = Shindo.getInstance().getWarpProxyManager();
            String status = warp != null ? warp.getDiagnostics().getStatus().name() : "UNKNOWN";
            nvg.drawText(status, cardX + 16F, cardY + 54F, accent.getColor1(), 11F, Fonts.MEDIUM);
        }
        boolean warpEnabled = manager != null && manager.isWarpProxyEnabled();
        warpButtonW = 150F;
        warpButtonH = BUTTON_HEIGHT;
        warpButtonX = cardX + cardW - warpButtonW - 16F;
        warpButtonY = cardY + 60F;
        boolean warpHovered = MouseUtils.isInside(mouseX, mouseY, warpButtonX, warpButtonY, warpButtonW, warpButtonH);
        Color warpBg = warpEnabled
                ? (warpHovered ? ColorUtils.applyAlpha(accent.getColor1(), 220) : ColorUtils.applyAlpha(accent.getColor1(), 180))
                : (warpHovered ? palette.getBackgroundColor(ColorType.NORMAL) : palette.getBackgroundColor(ColorType.MID));
        nvg.drawRoundedRect(warpButtonX, warpButtonY, warpButtonW, warpButtonH, 8F, warpBg);
        nvg.drawText(LegacyIcon.CLOUD, warpButtonX + 10F, warpButtonY + 6F, palette.getFontColor(ColorType.DARK), 12F, Fonts.LEGACYICON);
        String warpLabel = warpEnabled ? "Disable WARP" : "Enable WARP";
        nvg.drawText(warpLabel, warpButtonX + 30F, warpButtonY + 8F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
    }

    private float drawHero(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float w, int mouseX, int mouseY) {
        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color glow = ColorUtils.applyAlpha(accent.getColor1(), (int) (heroGlow.getValue() * 90));
        nvg.drawRoundedRect(x, y, w, HERO_HEIGHT, CARD_RADIUS, base);
        nvg.drawGradientRoundedRect(x, y, w, HERO_HEIGHT, CARD_RADIUS, glow, ColorUtils.applyAlpha(accent.getColor2(), 80));

        float titleY = y + 18F;
        nvg.drawText(TranslateText.NETWORK.getText(), x + 18F, titleY, palette.getFontColor(ColorType.DARK), 16F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_SUMMARY.getText(), x + 18F, titleY + 18F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 10F, Fonts.REGULAR);

        // Sliders
        float sliderY = titleY + 55F;
        if (capacitySetting != null && capacitySlider != null) {
            nvg.drawText(TranslateText.NETWORK_LINK_CAPACITY.getText(), x + 18F, sliderY - 10F, palette.getFontColor(ColorType.NORMAL), 9.5F, Fonts.MEDIUM);
            capacitySetting.setValue(manager.getLinkCapacityMbps());
            capacitySlider.setX(x + 18F);
            capacitySlider.setY(sliderY);
            capacitySlider.setWidth(w - 36F);
            capacitySlider.draw(mouseX, mouseY, 0);
            sliderY += 34F;
            manager.setLinkCapacityMbps(capacitySetting.getValueInt());
        }
        if (responsivenessSetting != null && responsivenessSlider != null) {
            nvg.drawText(TranslateText.NETWORK_RESPONSIVENESS.getText(), x + 18F, sliderY - 10F, palette.getFontColor(ColorType.NORMAL), 9.5F, Fonts.MEDIUM);
            responsivenessSetting.setValue(manager.getResponsivenessLevel());
            responsivenessSlider.setX(x + 18F);
            responsivenessSlider.setY(sliderY);
            responsivenessSlider.setWidth(w - 36F);
            responsivenessSlider.draw(mouseX, mouseY, 0);
            sliderY += 34F;
            manager.setResponsivenessLevel(responsivenessSetting.getValueInt());
        }

        // Toggle
        float toggleW = 120F;
        float toggleX = x + w - toggleW - 18F;
        float buttonsY = sliderY + 10F;
        float maxButtonsY = y + HERO_HEIGHT - BUTTON_HEIGHT - 10F;
        float toggleY = Math.min(buttonsY, maxButtonsY);
        boolean on = snapshot != null && snapshot.isOptimizerEnabled();
        boolean toggleHovered = MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, toggleW, BUTTON_HEIGHT);
        Color toggleBg = on
                ? (toggleHovered ? ColorUtils.applyAlpha(accent.getColor1(), 220) : ColorUtils.applyAlpha(accent.getColor1(), 200))
                : (toggleHovered ? palette.getBackgroundColor(ColorType.NORMAL) : palette.getBackgroundColor(ColorType.MID));
        nvg.drawRoundedRect(toggleX, toggleY, toggleW, BUTTON_HEIGHT, 8F, toggleBg);
        nvg.drawText(LegacyIcon.POWER, toggleX + 10F, toggleY + 6F, palette.getFontColor(ColorType.DARK), 12F, Fonts.LEGACYICON);
        nvg.drawText(on ? "ON" : "OFF", toggleX + 32F, toggleY + 8F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        optimizerButtonX = toggleX;
        optimizerButtonY = toggleY;
        optimizerButtonW = toggleW;
        optimizerButtonH = BUTTON_HEIGHT;

        settingsButtonW = 120F;
        settingsButtonH = BUTTON_HEIGHT;
        settingsButtonX = toggleX - settingsButtonW - 10F;
        settingsButtonY = toggleY;
        drawButton(nvg, palette, accent, settingsButtonX, settingsButtonY, settingsButtonW, settingsButtonH, TranslateText.SETTINGS.getText(), LegacyIcon.SETTINGS, true, mouseX, mouseY, null);

        // Connection dropdown aligned with settings button and opening upwards
        dropdownX = x + 18F;
        dropdownY = settingsButtonY;
        dropdownW = 80F;
        dropdownH = settingsButtonH;

        if (mediumSetting != null && mediumDropdown != null) {
            mediumSetting.setOption(mediumSetting.getOption());
            mediumDropdown.setX(dropdownX);
            mediumDropdown.setY(dropdownY);
            mediumDropdown.setWidth(dropdownW);
            mediumDropdown.setHeight(dropdownH);
            mediumDropdown.setOpenUp(true);
            mediumDropdown.draw(mouseX, mouseY, 0);
        }

        return y + HERO_HEIGHT;
    }

    private float drawFocusChart(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float w) {
        float h = CHART_HEIGHT;
        nvg.drawRoundedRect(x, y, w, h, CARD_RADIUS, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.getText(), x + 16F, y + 16F, palette.getFontColor(ColorType.DARK), 11F, Fonts.SEMIBOLD);

        float barX = x + 16F;
        float barY = y + 36F;
        float barW = w - 32F;
        float barH = 12F;
        float[] focuses = {
                snapshot != null ? snapshot.getLatencyFocus() : 0.5F,
                snapshot != null ? snapshot.getStabilityFocus() : 0.5F,
                snapshot != null ? snapshot.getThroughputFocus() : 0.5F
        };
        TranslateText[] labels = {
                TranslateText.NETWORK_LATENCY_FOCUS,
                TranslateText.NETWORK_STABILITY_FOCUS,
                TranslateText.NETWORK_THROUGHPUT_FOCUS
        };
        for (int i = 0; i < focuses.length; i++) {
            float fy = barY + i * (barH + 14F);
            nvg.drawText(labels[i].getText(), barX, fy - 2F, palette.getFontColor(ColorType.NORMAL), 9.5F, Fonts.REGULAR);
            nvg.drawRoundedRect(barX, fy + 8F, barW, barH, 6F, palette.getBackgroundColor(ColorType.MID));
            nvg.drawRoundedRect(barX, fy + 8F, barW * focuses[i], barH, 6F, ColorUtils.applyAlpha(accent.getColor1(), 220));
            nvg.drawText(df.format(focuses[i] * 100) + "%", barX + barW - 40F, fy + 10F, palette.getFontColor(ColorType.DARK), 9F, Fonts.MEDIUM);
        }
        return y + h;
    }

    private float drawMetricsCards(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float w) {
        float cardH = 82F;
        float halfW = (w - CARD_GAP) / 2F;
        drawMetricCard(nvg, palette, accent, x, y, halfW, cardH, LegacyIcon.TIMER, "Before", baselineSnapshot);
        drawMetricCard(nvg, palette, accent, x + halfW + CARD_GAP, y, halfW, cardH, LegacyIcon.ARROW_UP, "After", snapshot);
        return y + cardH;
    }

    private void drawMetricCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float w, float h, String icon, String title, ProfileSnapshot snap) {
        nvg.drawRoundedRect(x, y, w, h, CARD_RADIUS, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(icon, x + 14F, y + 14F, accent.getColor1(), 14F, Fonts.ICON_OUTLINE);
        nvg.drawText(title, x + 32F, y + 15F, palette.getFontColor(ColorType.DARK), 12F, Fonts.SEMIBOLD);
        if (snap == null) {
            nvg.drawText("—", x + 14F, y + 36F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.REGULAR);
            return;
        }
        nvg.drawText("Ping: " + snap.getAveragePingMs() + " ms", x + 14F, y + 36F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.MEDIUM);
        nvg.drawText("Jitter: " + snap.getJitterMs() + " ms", x + 14F, y + 50F, palette.getFontColor(ColorType.NORMAL), 9.5F, Fonts.REGULAR);
        nvg.drawText("Dynamic: " + (snap.isDynamicFlush() ? snap.getDynamicIntervalMs() + "ms / " + snap.getDynamicThreshold() : "OFF"),
                x + 14F, y + 64F, palette.getFontColor(ColorType.NORMAL), 9F, Fonts.REGULAR);
    }

    private void drawButton(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float w, float h, String label, String icon, boolean enabled, int mouseX, int mouseY, Runnable action) {
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, w, h);
        Color bg = enabled
                ? (hovered ? ColorUtils.applyAlpha(accent.getColor1(), 220) : ColorUtils.applyAlpha(accent.getColor1(), 180))
                : palette.getBackgroundColor(ColorType.MID);
        nvg.drawRoundedRect(x, y, w, h, 8F, bg);
        nvg.drawText(icon, x + 10F, y + h / 2F - 7F, palette.getFontColor(ColorType.DARK), 12F, Fonts.ICON_FILLED);
        nvg.drawText(label, x + 30F, y + h / 2F - 5F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        if (enabled && hovered && action != null && Mouse.isButtonDown(0)) {
            action.run();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (manager == null) {
            return;
        }
        boolean settingsAnimating = settingsAnimation != null && !settingsAnimation.isDone(Direction.FORWARDS);
        boolean overlayActive = settingsOpen || settingsAnimating;
        if (overlayActive) {
            if (handleSettingsClick(mouseX, mouseY, mouseButton)) {
                return;
            }
        }

        float scrollY = scroll.getValue();
        int contentMouseY = (int) (mouseY - scrollY);

        if (mouseButton == 0) {
            for (FilterChip chip : navigationChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click();
                    return;
                }
            }
        }
        if (activeSection == NetworkSection.TWEAKER) {
            if (mediumDropdown != null) {
                mediumDropdown.mouseClicked(mouseX, contentMouseY, mouseButton);
                LinkMedium selected = mediumSetting != null && mediumSetting.getOption() != null
                        ? LinkMedium.fromKey(mediumSetting.getOption().getNameKey())
                        : null;
                if (selected != null) {
                    manager.setNetworkMedium(selected);
                }
            }
            if (capacitySlider != null) {
                capacitySlider.mouseClicked(mouseX, contentMouseY, mouseButton);
                manager.setLinkCapacityMbps(capacitySetting.getValueInt());
            }
            if (responsivenessSlider != null) {
                responsivenessSlider.mouseClicked(mouseX, contentMouseY, mouseButton);
                manager.setResponsivenessLevel(responsivenessSetting.getValueInt());
            }
            if (MouseUtils.isInside(mouseX, contentMouseY, dropdownX, dropdownY, dropdownW, dropdownH)) {
                dropdownOpen = !dropdownOpen;
            }
            if (mouseButton == 0 && MouseUtils.isInside(mouseX, contentMouseY, optimizerButtonX, optimizerButtonY, optimizerButtonW, optimizerButtonH)) {
                boolean newState = snapshot == null || !snapshot.isOptimizerEnabled();
                manager.setOptimizerEnabled(newState);
            }
            if (mouseButton == 0 && MouseUtils.isInside(mouseX, contentMouseY, settingsButtonX, settingsButtonY, settingsButtonW, settingsButtonH)) {
                openSettingsPanel();
                return;
            }
            if (dynamicToggle != null) {
                dynamicToggle.mouseClicked(mouseX, contentMouseY, mouseButton);
                manager.setDynamicFlushEnabled(dynamicSetting.isToggled());
            }
            if (burstToggle != null) {
                burstToggle.mouseClicked(mouseX, contentMouseY, mouseButton);
                manager.setBurstFlushSmoothing(burstSetting.isToggled());
            }
            if (autoFlushToggle != null) {
                autoFlushToggle.mouseClicked(mouseX, contentMouseY, mouseButton);
                manager.setAutoFlushEnabled(autoFlushSetting.isToggled());
            }
            if (jitterSlider != null) {
                jitterSlider.mouseClicked(mouseX, contentMouseY, mouseButton);
                manager.setJitterSensitivity((int) jitterSetting.getValue());
            }
        } else if (mouseButton == 0 && MouseUtils.isInside(mouseX, contentMouseY, warpButtonX, warpButtonY, warpButtonW, warpButtonH)) {
            manager.setWarpProxyEnabled(!manager.isWarpProxyEnabled());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        boolean settingsAnimating = settingsAnimation != null && !settingsAnimation.isDone(Direction.FORWARDS);
        if (settingsOpen) {
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingsScroll);
            return;
        }
        if (settingsAnimating) {
            return;
        }
        int contentMouseY = (int) (mouseY - scroll.getValue());
        if (mediumDropdown != null) {
            mediumDropdown.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
        if (capacitySlider != null) {
            capacitySlider.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
        if (responsivenessSlider != null) {
            responsivenessSlider.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
        if (dynamicToggle != null) {
            dynamicToggle.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
        if (burstToggle != null) {
            burstToggle.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
        if (autoFlushToggle != null) {
            autoFlushToggle.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
        if (jitterSlider != null) {
            jitterSlider.mouseReleased(mouseX, contentMouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        boolean settingsAnimating = settingsAnimation != null && !settingsAnimation.isDone(Direction.FORWARDS);
        if (settingsOpen) {
            settingsPanel.keyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_ESCAPE) {
                closeSettingsPanel();
            }
            return;
        }
        if (settingsAnimating) {
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            dropdownOpen = false;
        }
        scroll.onKey(keyCode);
    }

    private boolean handleSettingsClick(int mouseX, int mouseY, int mouseButton) {
        SettingsLayout layout = getSettingsLayout();
        float offsetX = settingsAnimation != null ? (float) (settingsAnimation.getValue() * 600) : 0F;
        float closeSize = 16F;
        float closeX = layout.x + layout.width - closeSize - 12F;
        float closeY = layout.y + 12F;
        if (mouseButton == 0 && MouseUtils.isInside(mouseX, mouseY, closeX - 4F + offsetX, closeY - 4F, closeSize + 8F, closeSize + 8F)) {
            closeSettingsPanel();
            return true;
        }
        if (MouseUtils.isInside(mouseX, mouseY, layout.x + offsetX, layout.y, layout.width, layout.height)) {
            settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, layout.contentX, layout.contentY, layout.contentWidth, layout.viewportHeight, settingsScroll);
            return true;
        }
        if (mouseButton == 0) {
            closeSettingsPanel();
            return true;
        }
        return false;
    }

    private void openSettingsPanel() {
        if (manager == null) {
            return;
        }
        settingsPanel.setLayoutMode(SettingsPanel.LayoutMode.SINGLE_COLUMN);
        settingsPanel.clear();
        settingsPanel.buildEntries(getFilteredSettings());
        settingsScroll.resetAll();
        settingsOpen = true;
        if (settingsAnimation != null) {
            settingsAnimation.setDirection(Direction.BACKWARDS);
        }
        setCanClose(false);
    }

    private void closeSettingsPanel() {
        settingsOpen = false;
        settingsPanel.clear();
        settingsScroll.resetAll();
        if (settingsAnimation != null) {
            settingsAnimation.setDirection(Direction.FORWARDS);
        }
        setCanClose(true);
    }


    private void activateSection(NetworkSection section) {
        if (section == activeSection) {
            return;
        }
        activeSection = section;
        slideAnimation.setDirection(Direction.FORWARDS);
        dropdownOpen = false;
        scroll.resetAll();
    }

    private void drawSettingsPanel(NanoVGManager nvg, ColorPalette palette, AccentColor accent, int mouseX, int mouseY, float partialTicks) {
        if (settingsAnimation == null) {
            return;
        }
        settingsAnimation.setDirection(settingsOpen ? Direction.BACKWARDS : Direction.FORWARDS);
        SettingsLayout layout = getSettingsLayout();
        float offsetX = (float) (settingsAnimation.getValue() * 600);
        if (MouseUtils.isInside(mouseX, mouseY, layout.x + offsetX, layout.y, layout.width, layout.height)) {
            settingsScroll.onScroll();
        }
        settingsScroll.onAnimation();

        nvg.save();
        nvg.translate(offsetX, 0);
        nvg.drawShadow(layout.x, layout.y, layout.width, layout.height, 12F, 7);
        nvg.drawRoundedRect(layout.x, layout.y, layout.width, layout.height, 12F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));
        nvg.drawRoundedRect(layout.x + 1F, layout.y + 1F, layout.width - 2F, layout.height - 2F, 11F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));

        nvg.drawText(TranslateText.NETWORK.getText(), layout.x + 16F, layout.y + 16F, palette.getFontColor(ColorType.DARK), 13F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.getText(), layout.x + 16F, layout.y + 32F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9F, Fonts.REGULAR);

        float closeSize = 16F;
        float closeX = layout.x + layout.width - closeSize - 12F;
        float closeY = layout.y + 12F;
        boolean closeHovered = MouseUtils.isInside(mouseX, mouseY, closeX - 4F + offsetX, closeY - 4F, closeSize + 8F, closeSize + 8F);
        if (closeHovered) {
            nvg.drawRoundedRect(closeX - 4F, closeY - 4F, closeSize + 8F, closeSize + 8F, 6F, ColorUtils.applyAlpha(accent.getColor1(), 80));
        }
        nvg.drawText(LegacyIcon.CLOSE, closeX, closeY, palette.getFontColor(ColorType.DARK), 12F, Fonts.LEGACYICON);

        nvg.save();
        nvg.scissor(layout.x + 6F, layout.contentY - 6F, layout.width - 12F, layout.viewportHeight + 12F);
        settingsPanel.draw(mouseX, mouseY, partialTicks, layout.contentX, layout.contentY, layout.contentWidth, layout.viewportHeight, nvg, palette, settingsScroll);
        nvg.restore();
        nvg.restore();
    }

    private SettingsLayout getSettingsLayout() {
        SettingsLayout layout = new SettingsLayout();
        layout.x = getX() + 24F;
        layout.y = getY() + 20F;
        layout.width = getWidth() - 48F;
        layout.height = getHeight() - 40F;
        layout.contentX = layout.x + 14F;
        layout.contentY = layout.y + 44F;
        layout.contentWidth = layout.width - 28F;
        layout.viewportHeight = layout.height - 58F;
        return layout;
    }

    private static class SettingsLayout {
        float x;
        float y;
        float width;
        float height;
        float contentX;
        float contentY;
        float contentWidth;
        float viewportHeight;
    }

    private List<Setting> getFilteredSettings() {
        List<Setting> settings = SettingRegistry.getSettings(manager);
        if (settings == null || settings.isEmpty()) {
            return settings;
        }
        List<String> hiddenKeys = Arrays.asList(
                TranslateText.NETWORK_OPTIMIZER_TOGGLE.getKey(),
                TranslateText.NETWORK_MEDIUM.getKey(),
                TranslateText.NETWORK_LINK_CAPACITY.getKey(),
                TranslateText.NETWORK_RESPONSIVENESS.getKey(),
                TranslateText.NETWORK_DYNAMIC_FLUSH.getKey(),
                TranslateText.NETWORK_BURST_SMOOTHING.getKey(),
                TranslateText.NETWORK_AUTO_FLUSH.getKey(),
                TranslateText.NETWORK_JITTER_SENSITIVITY.getKey(),
                TranslateText.NETWORK_PROXY_WARP.getKey()
        );
        List<String> hiddenCategories = Arrays.asList("overview", "routing");
        List<Setting> filtered = new ArrayList<>();
        for (Setting setting : settings) {
            if (setting == null) {
                continue;
            }
            if (setting instanceof CategorySetting) {
                TranslateText t = setting.getTranslate();
                if (t == TranslateText.NETWORK_CATEGORY_OVERVIEW || t == TranslateText.NETWORK_CATEGORY_ROUTING) {
                    continue;
                }
            }
            String key = setting.getNameKey();
            if (key != null) {
                String keyLower = key.toLowerCase();
                boolean hide = false;
                for (String hidden : hiddenKeys) {
                    if (hidden == null) {
                        continue;
                    }
                    String hiddenLower = hidden.toLowerCase();
                    if (keyLower.equals(hiddenLower) || keyLower.endsWith(":" + hiddenLower)) {
                        hide = true;
                        break;
                    }
                }
                if (hide) {
                    continue;
                }
            }
            if (setting instanceof CategorySetting) {
                String categoryKey = setting.getNameKey();
                boolean hideCategory = false;
                for (String hc : hiddenCategories) {
                    if (categoryKey == null || hc == null) {
                        continue;
                    }
                    String catLower = categoryKey.toLowerCase();
                    String hcLower = hc.toLowerCase();
                    if (catLower.equals(hcLower) || catLower.endsWith(":" + hcLower)) {
                        hideCategory = true;
                        break;
                    }
                }
                if (hideCategory) {
                    continue;
                }
            }
            filtered.add(setting);
        }
        return filtered;
    }

    private float computeContentHeight() {
        return CONTENT_PADDING * 2 + HERO_HEIGHT + CHART_HEIGHT + 82F + 160F + 120F + (CARD_GAP * 5);
    }

    private void buildControls() {
        if (manager == null) {
            return;
        }
        List<Option> mediums = new ArrayList<>();
        for (LinkMedium medium : LinkMedium.values()) {
            mediums.add(new Option(medium.getTranslate()));
        }
        mediumSetting = new ComboSetting(TranslateText.NETWORK_MEDIUM, manager, LinkMedium.WIRED.getTranslate(), mediums);
        mediumDropdown = new CompDropdown(200F, mediumSetting);

        capacitySetting = new NumberSetting(TranslateText.NETWORK_LINK_CAPACITY, manager, manager.getLinkCapacityMbps(), 10, 1000, true);
        capacitySlider = new CompSlider(capacitySetting);

        responsivenessSetting = new NumberSetting(TranslateText.NETWORK_RESPONSIVENESS, manager, manager.getResponsivenessLevel(), 1, 10, true);
        responsivenessSlider = new CompSlider(responsivenessSetting);

        dynamicSetting = new BooleanSetting(TranslateText.NETWORK_DYNAMIC_FLUSH, manager, manager.isDynamicFlushEnabled());
        burstSetting = new BooleanSetting(TranslateText.NETWORK_BURST_SMOOTHING, manager, manager.isBurstFlushSmoothing());
        autoFlushSetting = new BooleanSetting(TranslateText.NETWORK_AUTO_FLUSH, manager, manager.isAutoFlushEnabled());
        dynamicToggle = new CompToggleButton(dynamicSetting);
        burstToggle = new CompToggleButton(burstSetting);
        autoFlushToggle = new CompToggleButton(autoFlushSetting);

        jitterSetting = new NumberSetting(TranslateText.NETWORK_JITTER_SENSITIVITY, manager, manager.getJitterSensitivity(), 1, 20, true);
        jitterSlider = new CompSlider(jitterSetting);
        jitterSlider.setShowValue(true);
    }

    private float drawAdvancedSettings(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float w, int mouseX, int mouseY) {
        float cardH = 140F;
        nvg.drawRoundedRect(x, y, w, cardH, CARD_RADIUS, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText("Advanced Tuning", x + 16F, y + 16F, palette.getFontColor(ColorType.DARK), 12F, Fonts.SEMIBOLD);

        float rowY = y + 36F;
        float rowX = x + 16F;
        if (dynamicSetting != null && dynamicToggle != null) {
            dynamicSetting.setToggled(manager.isDynamicFlushEnabled());
            dynamicToggle.setX(rowX + w - 60);
            dynamicToggle.setY(rowY);
            dynamicToggle.draw(mouseX, mouseY, 0);
            nvg.drawText(TranslateText.NETWORK_DYNAMIC_FLUSH.getText(), rowX , rowY + 2F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.MEDIUM);
            rowY += 24F;
        }
        if (burstSetting != null && burstToggle != null) {
            burstSetting.setToggled(manager.isBurstFlushSmoothing());
            burstToggle.setX(rowX + w - 60);
            burstToggle.setY(rowY);
            burstToggle.draw(mouseX, mouseY, 0);
            nvg.drawText(TranslateText.NETWORK_BURST_SMOOTHING.getText(), rowX, rowY + 2F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.MEDIUM);
            rowY += 24F;
        }
        if (autoFlushSetting != null && autoFlushToggle != null) {
            autoFlushSetting.setToggled(manager.isAutoFlushEnabled());
            autoFlushToggle.setX(rowX + w - 60);
            autoFlushToggle.setY(rowY);
            autoFlushToggle.draw(mouseX, mouseY, 0);
            nvg.drawText(TranslateText.NETWORK_AUTO_FLUSH.getText(), rowX, rowY + 2F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.MEDIUM);
            rowY += 34F;
        }
        if (jitterSetting != null && jitterSlider != null) {
            jitterSetting.setValue(manager.getJitterSensitivity());
            nvg.drawText(TranslateText.NETWORK_JITTER_SENSITIVITY.getText(), rowX, rowY - 10F, palette.getFontColor(ColorType.NORMAL), 9.5F, Fonts.MEDIUM);
            jitterSlider.setX(rowX);
            jitterSlider.setY(rowY);
            jitterSlider.setWidth(w - 40F);
            jitterSlider.draw(mouseX, mouseY, 0);
            manager.setJitterSensitivity((int) jitterSetting.getValue());
        }
        return y + cardH;
    }
}
