package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.management.tweaker.ConnectionTweakerManager;
import me.miki.shindo.management.tweaker.ConnectionTweakerManager.ProfileSnapshot;
import me.miki.shindo.management.tweaker.proxy.WarpProxyManager;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TweakerCategory extends Category {

    private static final float HERO_HEIGHT = 122F;
    private static final float HERO_PADDING = 20F;
    private static final float CARD_GAP = 12F;
    private static final float METRIC_CARD_HEIGHT = 60F;
    private static final float WARP_CARD_HEIGHT = 116F;

    private enum NetworkSection {
        TWEAKER(TranslateText.NETWORK_CATEGORY_OVERVIEW, LegacyIcon.NET_MAX),
        PROXY(TranslateText.NETWORK_PROXY_WARP, LegacyIcon.GLOBE);

        private final TranslateText label;
        private final String icon;

        NetworkSection(TranslateText label, String icon) {
            this.label = label;
            this.icon = icon;
        }

        public String getLabel() {
            return label.getText();
        }

        public String getIcon() {
            return icon;
        }
    }

    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final Scroll overviewScroll = new Scroll();
    private final Scroll settingsScroll = new Scroll();
    private final Scroll proxyScroll = new Scroll();
    private final SimpleAnimation heroGlowAnimation = new SimpleAnimation();
    private final SimpleAnimation disabledOverlayAnimation = new SimpleAnimation();

    private final Rect settingsButtonBounds = new Rect();
    private final List<FilterChip> navigationChips = new ArrayList<>();

    private ConnectionTweakerManager manager;
    private BooleanSetting optimizerSetting;
    private BooleanSetting warpSetting;
    private CompToggleButton optimizerToggle;
    private CompToggleButton warpToggle;
    private List<Setting> cachedSettings = Collections.emptyList();
    private ProfileSnapshot snapshot;
    private boolean settingsOpen;
    private NetworkSection activeSection = NetworkSection.TWEAKER;
    private float lastTabHeight;
    private float overlayX;
    private float overlayY;
    private float overlayWidth;
    private float overlayHeight;

    public TweakerCategory(GuiModMenu parent) {
        super(parent, TranslateText.NETWORK_CATEGORY_OVERVIEW, LegacyIcon.GLOBE, false, true);
    }

    @Override
    public void initGui() {
        manager = Shindo.getInstance().getConnectionTweakerManager();
        if (manager != null) {
            optimizerSetting = SettingRegistry.getBooleanSetting(manager, "optimizerEnabled");
            warpSetting = SettingRegistry.getBooleanSetting(manager, "warpProxyEnabled");
            cachedSettings = SettingRegistry.getSettings(manager);
            optimizerToggle = optimizerSetting != null ? new CompToggleButton(optimizerSetting) : null;
            warpToggle = warpSetting != null ? new CompToggleButton(warpSetting) : null;
            settingsPanel.clear();
            if (!cachedSettings.isEmpty()) {
                settingsPanel.buildEntries(cachedSettings);
            }
        } else {
            optimizerSetting = null;
            warpSetting = null;
            optimizerToggle = null;
            warpToggle = null;
            cachedSettings = Collections.emptyList();
            settingsPanel.clear();
        }

        overviewScroll.resetAll();
        settingsScroll.resetAll();
        proxyScroll.resetAll();
        heroGlowAnimation.setValue(1F);
        disabledOverlayAnimation.setValue(0F);
        settingsOpen = false;
        snapshot = manager != null ? manager.getProfileSnapshot() : null;
        activeSection = NetworkSection.TWEAKER;
        lastTabHeight = 0F;
        overlayX = overlayY = overlayWidth = overlayHeight = 0F;
    }

    @Override
    public void initCategory() {
        settingsScroll.resetAll();
        proxyScroll.resetAll();
        overviewScroll.resetAll();
        if (manager != null) {
            cachedSettings = SettingRegistry.getSettings(manager);
            settingsPanel.clear();
            if (!cachedSettings.isEmpty()) {
                settingsPanel.buildEntries(cachedSettings);
            }
        } else {
            cachedSettings = Collections.emptyList();
            settingsPanel.clear();
        }
        snapshot = manager != null ? manager.getProfileSnapshot() : null;
        settingsOpen = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        if (manager == null) {
            return;
        }

        snapshot = manager.getProfileSnapshot();

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accent = colorManager.getCurrentColor();

        float viewportX = getX();
        float viewportY = getY();
        float viewportWidth = getWidth();
        float viewportHeight = getHeight();

        navigationChips.clear();

        float tabHeight = drawSectionTabs(nvg, palette, accent, viewportX, viewportY, viewportWidth);
        lastTabHeight = tabHeight;
        float contentTop = viewportY + tabHeight + 12F;
        float contentHeight = Math.max(0F, viewportHeight - (contentTop - viewportY));
        if (contentHeight <= 0F) {
            return;
        }

        switch (activeSection) {
            case TWEAKER:
                drawTweakerSection(nvg, palette, accent, mouseX, mouseY, partialTicks, contentTop, contentHeight);
                break;
            case PROXY:
                drawProxySection(nvg, palette, accent, mouseX, mouseY, partialTicks, contentTop, contentHeight);
                break;
        }
    }

    private float drawSectionTabs(NanoVGManager nvg,
                                  ColorPalette palette,
                                  AccentColor accent,
                                  float viewportX,
                                  float viewportY,
                                  float viewportWidth) {

        float chipHeight = 22F;
        float gap = 10F;
        float currentX = viewportX + HERO_PADDING - 6F;
        float currentY = viewportY + 6F;
        float maxX = viewportX + viewportWidth - HERO_PADDING + 6F;

        for (NetworkSection section : NetworkSection.values()) {

            String label = section.getLabel();
            float textWidth = nvg.getTextWidth(label, 9.5F, Fonts.MEDIUM);
            float iconWidth = nvg.getTextWidth(section.getIcon(), 12F, Fonts.LEGACYICON) + 6F;
            float chipWidth = CHIP_PADDING() * 2F + iconWidth + textWidth;

            if (currentX + chipWidth > maxX) {
                currentX = viewportX + HERO_PADDING - 6F;
                currentY += chipHeight + gap;
            }

            boolean active = section == activeSection;
            Color base = palette.getBackgroundColor(ColorType.DARK);

            if (active) {
                Color start = ColorUtils.applyAlpha(accent.getColor1(), 200);
                Color end = ColorUtils.applyAlpha(accent.getColor2(), 200);
                nvg.drawGradientRoundedRect(currentX, currentY, chipWidth, chipHeight, chipHeight / 2F, start, end);
            } else {
                nvg.drawRoundedRect(currentX, currentY, chipWidth, chipHeight, chipHeight / 2F, base);
            }

            float textX = currentX + CHIP_PADDING();
            float textY = currentY + chipHeight / 2F - 1F;
            Color iconColor = active ? Color.WHITE : palette.getFontColor(ColorType.NORMAL);

            nvg.drawText(section.getIcon(), textX, textY - 6F, iconColor, 12F, Fonts.LEGACYICON);
            textX += iconWidth;
            nvg.drawText(label, textX, textY, iconColor, 9.5F, Fonts.MEDIUM);

            navigationChips.add(new FilterChip(currentX, currentY, chipWidth, chipHeight, () -> {
                if (activeSection != section) {
                    activeSection = section;
                    overviewScroll.resetAll();
                    proxyScroll.resetAll();
                    settingsOpen = false;
                    settingsPanel.clear();
                    if (activeSection == NetworkSection.TWEAKER && !cachedSettings.isEmpty()) {
                        settingsPanel.buildEntries(cachedSettings);
                    }
                }
            }));

            currentX += chipWidth + gap;
        }

        return (currentY + chipHeight) - viewportY;
    }

    private void drawTweakerSection(NanoVGManager nvg,
                                    ColorPalette palette,
                                    AccentColor accent,
                                    int mouseX,
                                    int mouseY,
                                    float partialTicks,
                                    float contentTop,
                                    float contentHeight) {

        boolean optimizerActive = snapshot != null && snapshot.isOptimizerEnabled();
        heroGlowAnimation.setAnimation(optimizerActive ? 1F : 0.75F, 18);
        disabledOverlayAnimation.setAnimation(optimizerActive ? 0F : 1F, 16);

        float estimated = HERO_HEIGHT + METRIC_CARD_HEIGHT + 120F;
        overviewScroll.setMaxScroll(Math.max(0F, estimated - contentHeight + 40F));

        if (!settingsOpen && MouseUtils.isInside(mouseX, mouseY, getX(), contentTop, getWidth(), contentHeight)) {
            overviewScroll.onScroll();
        }
        overviewScroll.onAnimation();
        float scrollOffset = overviewScroll.getValue();

        nvg.save();
        nvg.scissor(getX(), contentTop, getWidth(), contentHeight);

        float contentX = getX() + HERO_PADDING;
        float contentWidth = getWidth() - HERO_PADDING * 2F;

        float heroY = contentTop + 18F + scrollOffset;
        drawHeroCard(nvg, palette, accent, contentX, heroY, contentWidth, mouseX, mouseY, partialTicks, optimizerActive);

        float metricY = heroY + HERO_HEIGHT + CARD_GAP;
        drawMetricsRow(nvg, palette, accent, contentX, metricY, contentWidth);

        float recommendationY = metricY + METRIC_CARD_HEIGHT + CARD_GAP;
        drawRecommendationCard(nvg, palette, accent, contentX, recommendationY, contentWidth);

        nvg.restore();

        if (settingsOpen) {
            drawSettingsOverlay(nvg, palette, mouseX, mouseY, partialTicks, contentTop, contentHeight);
        }
    }

    private void drawProxySection(NanoVGManager nvg,
                                  ColorPalette palette,
                                  AccentColor accent,
                                  int mouseX,
                                  int mouseY,
                                  float partialTicks,
                                  float contentTop,
                                  float contentHeight) {

        float estimated = WARP_CARD_HEIGHT + 36F;
        proxyScroll.setMaxScroll(Math.max(0F, estimated - contentHeight));
        if (MouseUtils.isInside(mouseX, mouseY, getX(), contentTop, getWidth(), contentHeight)) {
            proxyScroll.onScroll();
        }
        proxyScroll.onAnimation();
        float scrollOffset = proxyScroll.getValue();

        nvg.save();
        nvg.scissor(getX(), contentTop, getWidth(), contentHeight);

        float cardX = getX() + HERO_PADDING;
        float cardY = contentTop + 18F + scrollOffset;
        float cardWidth = getWidth() - HERO_PADDING * 2F;

        drawWarpProxyCard(nvg, palette, accent, cardX, cardY, cardWidth, mouseX, mouseY, partialTicks);

        nvg.restore();
    }

    private void drawHeroCard(NanoVGManager nvg,
                              ColorPalette palette,
                              AccentColor accent,
                              float x,
                              float y,
                              float width,
                              int mouseX,
                              int mouseY,
                              float partialTicks,
                              boolean optimizerActive) {

        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color overlay = ColorUtils.applyAlpha(accent.getColor1(), (int) (heroGlowAnimation.getValue() * 90));
        Color overlay2 = ColorUtils.applyAlpha(accent.getColor2(), (int) (heroGlowAnimation.getValue() * 70));

        nvg.drawRoundedRect(x, y, width, HERO_HEIGHT, 14F, base);
        nvg.drawGradientRoundedRect(x, y, width, HERO_HEIGHT, 14F, overlay, overlay2);
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, HERO_HEIGHT - 2F, 13F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 192));

        float titleY = y + 18F;
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_TOGGLE.getText(), x + 20F, titleY, palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_SUMMARY.getText(), x + 20F, titleY + 16F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 9.5F, Fonts.REGULAR);

        if (optimizerToggle != null) {
            optimizerToggle.setScale(1.15F);
            optimizerToggle.setX(x + width - optimizerToggle.getWidth() - 18F);
            optimizerToggle.setY(y + 20F);
            optimizerToggle.draw(mouseX, mouseY, partialTicks);
        }

        float settingsSize = 20F;
        float settingsX = x + width - settingsSize - 58F;
        float settingsY = y + 20F;
        settingsButtonBounds.set(settingsX, settingsY, settingsSize, settingsSize);
        boolean hovered = settingsButtonBounds.contains(mouseX, mouseY);
        Color buttonBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered ? 220 : 190);
        Color buttonStart = ColorUtils.applyAlpha(accent.getColor1(), hovered ? 180 : 110);
        Color buttonEnd = ColorUtils.applyAlpha(accent.getColor2(), hovered ? 180 : 110);

        nvg.drawRoundedRect(settingsX, settingsY, settingsSize, settingsSize, 6F, buttonBase);
        nvg.drawGradientRoundedRect(settingsX, settingsY, settingsSize, settingsSize, 6F, buttonStart, buttonEnd);
        nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + settingsSize / 2F - 1F, settingsY + settingsSize / 2F - 6F, palette.getFontColor(ColorType.DARK), 14F, Fonts.LEGACYICON);

        if (snapshot == null) {
            return;
        }

        float statY = y + HERO_HEIGHT - 54F;
        float columnWidth = (width - 40F) / 3F;

        drawHeroStat(nvg,
                palette,
                TranslateText.NETWORK_MEDIUM.getText(),
                snapshot.getNetworkMedium().getDisplayName(),
                0F,
                x + 20F,
                statY,
                columnWidth);

        drawHeroStat(nvg,
                palette,
                TranslateText.NETWORK_LINK_CAPACITY.getText(),
                snapshot.getLinkCapacityMbps() + " Mbps",
                0F,
                x + 20F + columnWidth,
                statY,
                columnWidth);

        drawHeroStat(nvg,
                palette,
                TranslateText.NETWORK_WRITE_BUFFER.getText(),
                snapshot.getWriteBufferKb() + " KB",
                snapshot.getRecommendedBufferKb(),
                x + 20F + (columnWidth * 2),
                statY,
                columnWidth);

        if (!optimizerActive) {
            Color mask = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), (int) (disabledOverlayAnimation.getValue() * 150));
            nvg.drawRoundedRect(x, y, width, HERO_HEIGHT, 14F, mask);
            nvg.drawCenteredText(TranslateText.NETWORK_OPTIMIZER_DISABLED.getText(),
                    x + width / 2F,
                    y + HERO_HEIGHT / 2F - 8F,
                    palette.getFontColor(ColorType.NORMAL),
                    11F,
                    Fonts.MEDIUM);
        }
    }

    private void drawHeroStat(NanoVGManager nvg, ColorPalette palette, String title, String value, float recommended, float x, float y, float width) {

        nvg.drawText(title, x, y, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 9F, Fonts.REGULAR);
        nvg.drawText(value, x, y + 15F, palette.getFontColor(ColorType.DARK), 12F, Fonts.MEDIUM);

        if (recommended > 0F) {
            String recommendText = TranslateText.NETWORK_RECOMMENDED_BUFFER.getText() + " " + (int) recommended + " KB";
            nvg.drawText(recommendText, x, y + 32F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 180), 8F, Fonts.REGULAR);
        }
    }

    private void drawMetricsRow(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width) {

        if (snapshot == null) {
            return;
        }

        float totalGap = CARD_GAP * 2;
        float cardWidth = (width - totalGap) / 3F;

        drawMetricCard(nvg, palette, accent, TranslateText.NETWORK_LATENCY_FOCUS.getText(), snapshot.getLatencyFocus(), x, y, cardWidth, METRIC_CARD_HEIGHT);
        drawMetricCard(nvg, palette, accent, TranslateText.NETWORK_STABILITY_FOCUS.getText(), snapshot.getStabilityFocus(), x + cardWidth + CARD_GAP, y, cardWidth, METRIC_CARD_HEIGHT);
        drawMetricCard(nvg, palette, accent, TranslateText.NETWORK_THROUGHPUT_FOCUS.getText(), snapshot.getThroughputFocus(), x + (cardWidth + CARD_GAP) * 2, y, cardWidth, METRIC_CARD_HEIGHT);
    }

    private void drawMetricCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, String title, float value, float x, float y, float width, float height) {

        value = Math.max(0F, Math.min(1F, value));

        Color base = palette.getBackgroundColor(ColorType.DARK);
        nvg.drawRoundedRect(x, y, width, height, 10F, base);
        nvg.drawGradientRoundedRect(x, y, width, height, 10F, ColorUtils.applyAlpha(accent.getColor1(), 35), ColorUtils.applyAlpha(accent.getColor2(), 55));
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, height - 2F, 9F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210));

        nvg.drawText(title, x + 14F, y + 12F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        String percent = (int) Math.round(value * 100F) + "%";
        nvg.drawText(percent, x + 14F, y + 24F, palette.getFontColor(ColorType.NORMAL), 11F, Fonts.MEDIUM);

        float barX = x + 14F;
        float barY = y + height - 18F;
        float barWidth = width - 28F;
        float barHeight = 8F;

        nvg.drawRoundedRect(barX, barY, barWidth, barHeight, barHeight / 2F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180));
        nvg.drawGradientRoundedRect(barX, barY, barWidth * value, barHeight, barHeight / 2F,
                ColorUtils.applyAlpha(accent.getColor1(), 200),
                ColorUtils.applyAlpha(accent.getColor2(), 200));
    }

    private void drawWarpProxyCard(NanoVGManager nvg,
                                   ColorPalette palette,
                                   AccentColor accent,
                                   float x,
                                   float y,
                                   float width,
                                   int mouseX,
                                   int mouseY,
                                   float partialTicks) {

        if (snapshot == null) {
            return;
        }

        boolean enabled = snapshot.isWarpProxyEnabled();
        WarpProxyManager.WarpStatus status = snapshot.getWarpStatus();

        float height = WARP_CARD_HEIGHT;
        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), enabled ? 220 : 205);
        Color gradientStart = ColorUtils.applyAlpha(accent.getColor1(), enabled ? 85 : 40);
        Color gradientEnd = ColorUtils.applyAlpha(accent.getColor2(), enabled ? 85 : 40);

        nvg.drawRoundedRect(x, y, width, height, 10F, base);
        nvg.drawGradientRoundedRect(x, y, width, height, 10F, gradientStart, gradientEnd);
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, height - 2F, 9F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), enabled ? 236 : 222));

        float padding = 22F;
        float titleY = y + padding - 4F;
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP.getText(), x + padding, titleY, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.SEMIBOLD);

        if (warpToggle != null) {
            warpToggle.setScale(1.05F);
            warpToggle.setX(x + width - warpToggle.getWidth() - padding);
            warpToggle.setY(y + padding - 6F);
            warpToggle.draw(mouseX, mouseY, partialTicks);
        }

        String summary = enabled ? TranslateText.NETWORK_PROXY_WARP_SUMMARY.getText() : TranslateText.NETWORK_PROXY_WARP_DESCRIPTION_DISABLED.getText();
        nvg.drawText(summary, x + padding, titleY + 16F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), enabled ? 210 : 170), 8.7F, Fonts.REGULAR);

        float lineY = titleY + 32F;
        float lineStep = 14F;

        String statusLabel = TranslateText.NETWORK_PROXY_WARP_STATUS.getText() + ": " + getWarpStatusLabel(status);
        Color statusColor = resolveWarpStatusColor(status, palette, accent, enabled);
        nvg.drawText(statusLabel, x + padding, lineY, statusColor, 9.3F, Fonts.MEDIUM);

        lineY += lineStep;
        String resolver = snapshot.getWarpResolver();
        if (resolver == null || resolver.isEmpty()) {
            resolver = enabled ? TranslateText.NETWORK_PROXY_WARP_STATUS_CONNECTING.getText() : TranslateText.NETWORK_PROXY_WARP_STATUS_DISABLED.getText();
        }
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP_RESOLVER.getText() + ": " + resolver, x + padding, lineY, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);

        lineY += lineStep;
        long lookupMs = snapshot.getWarpLookupMs();
        String lookup = lookupMs > 0L ? lookupMs + "ms" : "-";
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP_LATENCY.getText() + ": " + lookup, x + padding, lineY, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);

        lineY += lineStep;
        String updated = formatRelativeTime(snapshot.getWarpLastUpdatedAt());
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP_LAST_SYNC.getText() + ": " + updated, x + padding, lineY, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);

        if (status == WarpProxyManager.WarpStatus.ERROR) {
            String error = snapshot.getWarpError();
            if (error != null && !error.isEmpty()) {
                lineY += lineStep;
                String limited = nvg.getLimitText(error, 8.1F, Fonts.REGULAR, width - (padding * 2F));
                nvg.drawText(limited, x + padding, lineY, new Color(216, 92, 104, 230), 8.1F, Fonts.REGULAR);
            }
        }
    }

    private void drawRecommendationCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width) {

        float height = 120F;
        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210);
        Color start = ColorUtils.applyAlpha(accent.getColor1(), 90);
        Color end = ColorUtils.applyAlpha(accent.getColor2(), 90);

        nvg.drawRoundedRect(x, y, width, height, 12F, base);
        nvg.drawGradientRoundedRect(x, y, width, height, 12F, start, end);
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, height - 2F, 11F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 235));

        float padding = 22F;
        float headerY = y + padding;

        nvg.drawText(TranslateText.NETWORK_CATEGORY_PROFILE.getText(), x + padding, headerY,
                palette.getFontColor(ColorType.DARK), 12F, Fonts.MEDIUM);

        if (snapshot != null) {
            String bufferValue = snapshot.getRecommendedBufferKb() + " KB";
            nvg.drawText(bufferValue, x + padding, headerY + 18F, palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);

            String summary = TranslateText.NETWORK_OPTIMIZER_SUMMARY.getText();
            nvg.drawText(summary, x + padding, headerY + 40F,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9F, Fonts.REGULAR);
        }

        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.getText(), x + padding, headerY + 60F,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);
    }

    private void drawSettingsOverlay(NanoVGManager nvg,
                                     ColorPalette palette,
                                     int mouseX,
                                     int mouseY,
                                     float partialTicks,
                                     float contentTop,
                                     float contentHeight) {

        settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());

        overlayX = getX() + 15F;
        overlayY = contentTop + 15F;
        overlayWidth = getWidth() - 30F;
        overlayHeight = contentHeight - 30F;

        if (MouseUtils.isInside(mouseX, mouseY, overlayX, overlayY, overlayWidth, overlayHeight)) {
            settingsScroll.onScroll();
        }
        settingsScroll.onAnimation();

        nvg.drawRoundedRect(overlayX, overlayY, overlayWidth, overlayHeight, 10F, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(LegacyIcon.CHEVRON_LEFT, overlayX + 10F, overlayY + 8F, palette.getFontColor(ColorType.DARK), 13F, Fonts.LEGACYICON);
        nvg.drawText(TranslateText.SETTINGS.getText(), overlayX + 27F, overlayY + 9F, palette.getFontColor(ColorType.DARK), 13F, Fonts.MEDIUM);
        nvg.drawText(LegacyIcon.REFRESH, overlayX + overlayWidth - 24, overlayY + 7.5F, palette.getFontColor(ColorType.DARK), 13F, Fonts.LEGACYICON);

        float contentX = overlayX + 10F;
        float contentY = overlayY + 32F;
        float contentWidth = overlayWidth - 20F;
        float viewportHeight = overlayHeight - 47F;

        nvg.save();
        nvg.scissor(overlayX + 5F, contentY - 5F, overlayWidth - 10F, viewportHeight + 10F);
        settingsPanel.draw(mouseX, mouseY, partialTicks, contentX, contentY, contentWidth, viewportHeight, nvg, palette, settingsScroll);
        nvg.restore();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (manager == null) {
            return;
        }

        if (mouseButton == 0) {
            for (FilterChip chip : navigationChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.onClick();
                    return;
                }
            }
        }

        switch (activeSection) {
            case TWEAKER:
                handleTweakerClick(mouseX, mouseY, mouseButton);
                break;
            case PROXY:
                if (warpToggle != null) {
                    warpToggle.mouseClicked(mouseX, mouseY, mouseButton);
                }
                break;
        }
    }

    private void handleTweakerClick(int mouseX, int mouseY, int mouseButton) {

        if (settingsOpen) {
            float headerX = overlayWidth > 0F ? overlayX : getX() + 15F;
            float headerY = overlayHeight > 0F ? overlayY : getY() + lastTabHeight + 27F;
            float headerWidth = overlayWidth > 0F ? overlayWidth : getWidth() - 30F;
            float headerHeight = overlayHeight > 0F ? overlayHeight : getHeight() - (headerY - getY()) - 27F;
            float contentX = headerX + 10F;
            float contentY = headerY + 32F;
            float contentWidth = headerWidth - 20F;
            float viewportHeight = headerHeight - 47F;

            if (mouseButton == 0) {
                if (MouseUtils.isInside(mouseX, mouseY, headerX + 6F, headerY + 6F, 20F, 18F)) {
                    settingsOpen = false;
                    settingsPanel.clear();
                    overlayWidth = overlayHeight = 0F;
                    return;
                }

                if (MouseUtils.isInside(mouseX, mouseY, headerX + headerWidth - 28F, headerY + 6F, 18F, 18F)) {
                    settingsPanel.resetSettings();
                    return;
                }
            }

            if (!MouseUtils.isInside(mouseX, mouseY, headerX - 4F, headerY - 4F, headerWidth + 8F, headerHeight + 8F) && mouseButton == 0) {
                settingsOpen = false;
                settingsPanel.clear();
                overlayWidth = overlayHeight = 0F;
                return;
            }

            if (settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, contentX, contentY, contentWidth, viewportHeight, settingsScroll)) {
                return;
            }
        }

        if (optimizerToggle != null) {
            optimizerToggle.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (mouseButton == 0 && settingsButtonBounds.contains(mouseX, mouseY)) {
            if (!cachedSettings.isEmpty()) {
                settingsPanel.clear();
                settingsPanel.buildEntries(cachedSettings);
            }
            settingsOpen = true;
            settingsScroll.resetAll();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

        if (manager == null) {
            return;
        }

        if (optimizerToggle != null) {
            optimizerToggle.mouseReleased(mouseX, mouseY, mouseButton);
        }

        if (warpToggle != null) {
            warpToggle.mouseReleased(mouseX, mouseY, mouseButton);
        }

        if (settingsOpen) {
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingsScroll);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (settingsOpen) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                settingsOpen = false;
                settingsPanel.clear();
                overlayWidth = overlayHeight = 0F;
                return;
            }
            settingsPanel.keyTyped(typedChar, keyCode);
            settingsScroll.onKey(keyCode);
        }
    }

    private String getWarpStatusLabel(WarpProxyManager.WarpStatus status) {
        if (status == null) {
            return TranslateText.NETWORK_PROXY_WARP_STATUS_DISABLED.getText();
        }
        switch (status) {
            case ACTIVE:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_ACTIVE.getText();
            case CACHED:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_CACHED.getText();
            case RESOLVING:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_CONNECTING.getText();
            case IDLE:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_IDLE.getText();
            case BYPASSED:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_BYPASSED.getText();
            case ERROR:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_ERROR.getText();
            case DISABLED:
            default:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_DISABLED.getText();
        }
    }

    private Color resolveWarpStatusColor(WarpProxyManager.WarpStatus status, ColorPalette palette, AccentColor accent, boolean enabled) {
        if (status == null) {
            return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200);
        }
        switch (status) {
            case ACTIVE:
                return ColorUtils.applyAlpha(accent.getColor1(), enabled ? 220 : 140);
            case CACHED:
                return ColorUtils.applyAlpha(accent.getColor2(), enabled ? 220 : 140);
            case RESOLVING:
                return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220);
            case BYPASSED:
                return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200);
            case ERROR:
                return new Color(216, 92, 104, 220);
            case DISABLED:
            case IDLE:
            default:
                return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200);
        }
    }

    private String formatRelativeTime(long timestamp) {
        if (timestamp <= 0L) {
            return "-";
        }
        long diff = Math.max(0L, System.currentTimeMillis() - timestamp);
        long seconds = diff / 1000L;
        if (seconds < 60L) {
            return seconds + "s";
        }
        long minutes = seconds / 60L;
        if (minutes < 60L) {
            return minutes + "m";
        }
        long hours = minutes / 60L;
        if (hours < 24L) {
            return hours + "h";
        }
        long days = hours / 24L;
        return days + "d";
    }

    private static float CHIP_PADDING() {
        return 14F;
    }

    private static class FilterChip {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final Runnable onClick;

        FilterChip(float x, float y, float width, float height, Runnable onClick) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.onClick = onClick;
        }

        boolean contains(int mx, int my) {
            return MouseUtils.isInside(mx, my, x, y, width, height);
        }

        void onClick() {
            onClick.run();
        }
    }

    private static class Rect {
        private float x;
        private float y;
        private float width;
        private float height;

        void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(int mx, int my) {
            return MouseUtils.isInside(mx, my, x, y, width, height);
        }
    }
}
