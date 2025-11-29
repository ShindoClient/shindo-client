package me.miki.shindo.gui.modmenu.category.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.network.ConnectionTweakerManager;
import me.miki.shindo.management.network.ConnectionTweakerManager.ProfileSnapshot;
import me.miki.shindo.management.network.proxy.WarpProxyManager;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

public class NetworkCategory extends Category {

    private static final float CONTENT_PADDING = 20F;
    private static final float HERO_HEIGHT = 122F;
    private static final float HERO_PADDING = 20F;
    private static final float CARD_GAP = 12F;
    private static final float METRIC_CARD_HEIGHT = 60F;
    private static final float HERO_DESCRIPTION_FONT_SIZE = 9.5F;
    private static final float WARP_CARD_HEIGHT = 72F;

    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final List<FilterChip> navigationChips = new ArrayList<>();
    private final Scroll overviewScroll = new Scroll();
    private final Scroll settingsScroll = new Scroll();
    private final Scroll proxyScroll = new Scroll();
    private final SimpleAnimation heroGlowAnimation = new SimpleAnimation();
    private final SimpleAnimation disabledOverlayAnimation = new SimpleAnimation();
    private final Rect settingsButtonBounds = new Rect();

    private Animation settingAnimation;
    private ConnectionTweakerManager manager;
    private NetworkSection activeSection = NetworkSection.TWEAKER;
    private BooleanSetting warpSetting;
    private BooleanSetting optimizerSetting;
    private CompToggleButton optimizerToggle;
    private CompToggleButton warpToggle;
    private List<Setting> cachedSettings = Collections.emptyList();
    private ProfileSnapshot snapshot;
    private boolean settingsOpen;
    private float overlayX;
    private float overlayY;
    private float overlayWidth;
    private float overlayHeight;
    private Object currentSetting;

    public NetworkCategory(GuiModMenu parent) {
        super(parent, TranslateText.NETWORK, LegacyIcon.GLOBE, false, true);
    }

    @Override
    public void initGui() {
        manager = Shindo.getInstance().getConnectionTweakerManager();
        warpSetting = null;
        optimizerSetting = null;
        optimizerToggle = null;
        warpToggle = null;
        cachedSettings = Collections.emptyList();
        settingsPanel.clear();

        if (manager != null) {
            optimizerSetting = SettingRegistry.getBooleanSetting(manager, "optimizerEnabled");
            warpSetting = SettingRegistry.getBooleanSetting(manager, "warpProxyEnabled");
            cachedSettings = filterSettings(SettingRegistry.getSettings(manager));
            optimizerToggle = optimizerSetting != null ? new CompToggleButton(optimizerSetting) : null;
            warpToggle = warpSetting != null ? new CompToggleButton(warpSetting) : null;
        }

        snapshot = manager != null ? manager.getProfileSnapshot() : null;
        activeSection = NetworkSection.TWEAKER;
        onSectionActivated(activeSection);
    }

    @Override
    public void initCategory() {
        cachedSettings = manager != null ? filterSettings(SettingRegistry.getSettings(manager)) : Collections.emptyList();
        snapshot = manager != null ? manager.getProfileSnapshot() : null;
        settingsPanel.clear();
        onSectionActivated(activeSection);
    }

    private List<Setting> filterSettings(List<Setting> settings) {
        if (settings == null || settings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Setting> filtered = new ArrayList<>(settings.size());
        for (Setting setting : settings) {

            if (setting == null) {
                continue;
            }

            if (setting == warpSetting) {
                continue;
            }

            if (setting == optimizerSetting) {
                continue;
            }

            if (setting instanceof CategorySetting && isOverviewCategory((CategorySetting) setting)) {
                continue;
            }

            if (setting instanceof CategorySetting && isRoutingCategory((CategorySetting) setting)) {
                continue;
            }
            filtered.add(setting);
        }
        return filtered;
    }

    private boolean isRoutingCategory(CategorySetting setting) {
        if (setting == null) {
            return false;
        }
        TranslateText translate = setting.getTranslate();
        if (translate == TranslateText.NETWORK_CATEGORY_ROUTING) {
            return true;
        }
        String key = setting.getNameKey();
        if (key != null && key.equalsIgnoreCase("routing")) {
            return true;
        }
        String name = setting.getName();
        return name != null && name.equalsIgnoreCase(TranslateText.NETWORK_CATEGORY_ROUTING.getText());
    }

    private boolean isOverviewCategory(CategorySetting setting) {
        if (setting == null) {
            return false;
        }
        TranslateText translate = setting.getTranslate();
        if (translate == TranslateText.NETWORK_CATEGORY_OVERVIEW) {
            return true;
        }
        String key = setting.getNameKey();
        if (key != null && key.equalsIgnoreCase("overview")) {
            return true;
        }
        String name = setting.getName();
        return name != null && name.equalsIgnoreCase(TranslateText.NETWORK_CATEGORY_OVERVIEW.getText());
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

        float slideOffset = getTweakerSlideOffset();
        float tabHeight = drawSectionTabs(nvg, palette, accent, viewportX, viewportY, viewportWidth, slideOffset, mouseX, mouseY);
        float contentTop = viewportY + tabHeight + 12F;
        float contentHeight = Math.max(0F, viewportHeight - (contentTop - viewportY));
        if (contentHeight <= 0F) {
            return;
        }

        if (activeSection == NetworkSection.TWEAKER) {
            drawTweakerSection(nvg, palette, accent, mouseX, mouseY, partialTicks, contentTop, contentHeight);
        } else if (activeSection == NetworkSection.PROXY) {
            drawProxySection(nvg, palette, accent, mouseX, mouseY, partialTicks, contentTop, contentHeight);
        }
    }

    private float drawSectionTabs(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float viewportX, float viewportY, float viewportWidth, float slideOffset, int mouseX, int mouseY) {

        navigationChips.clear();

        float chipGap = 10F;
        float startX = viewportX + CONTENT_PADDING - 6F;
        float maxX = viewportX + viewportWidth - CONTENT_PADDING + 6F;
        float currentX = startX;
        float currentY = viewportY + 6F;

        for (NetworkSection section : NetworkSection.values()) {

            String label = section.getLabel();
            String icon = section.getIcon();
            float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, icon);
            float renderX = currentX + slideOffset;

            if (currentX + chipWidth > maxX) {
                currentX = startX;
                currentY += CategoryChipRenderer.CHIP_HEIGHT + chipGap;
                renderX = currentX + slideOffset;
            }

            boolean active = section == activeSection;
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, renderX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);

            CategoryChipRenderer.drawChip(nvg, palette, accent, renderX, currentY, chipWidth, label, icon, active, hovered);

            FilterChip chip = new FilterChip(() -> activateSection(section));
            chip.setBounds(renderX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            navigationChips.add(chip);

            currentX += chipWidth + chipGap;
        }

        return (currentY + CategoryChipRenderer.CHIP_HEIGHT) - viewportY;
    }

    private void drawTweakerSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight) {
        if (settingAnimation == null) {
            settingAnimation = new SmoothStepAnimation(260, 1.0);
            settingAnimation.setValue(1.0);
        }

        boolean optimizerActive = snapshot != null && snapshot.isOptimizerEnabled();
        heroGlowAnimation.setAnimation(optimizerActive ? 1F : 0.75F, 18);
        disabledOverlayAnimation.setAnimation(optimizerActive ? 0F : 1F, 16);

        float estimated = HERO_HEIGHT + METRIC_CARD_HEIGHT + 120F;
        overviewScroll.setMaxScroll(Math.max(0F, estimated - contentHeight + 40F));

        settingAnimation.setDirection(settingsOpen ? Direction.BACKWARDS : Direction.FORWARDS);

        if (settingAnimation.isDone(Direction.FORWARDS)) {
            currentSetting = null;
        }

        if (!settingsOpen && MouseUtils.isInside(mouseX, mouseY, getX(), contentTop, getWidth(), contentHeight)) {
            overviewScroll.onScroll();
            overviewScroll.onAnimation();
        }

        float scrollValue = overviewScroll.getValue();

        nvg.save();
        nvg.translate((float) -(600 - (settingAnimation.getValue() * 600)), 0);

        nvg.save();
        nvg.intersectScissor(getX(), contentTop, getWidth(), contentHeight);
        nvg.translate(0, scrollValue);

        float contentX = getX() + HERO_PADDING;
        float contentWidth = getWidth() - HERO_PADDING * 2F;

        float heroY = contentTop + 18F;
        drawHeroCard(nvg, palette, accent, contentX, heroY, contentWidth, mouseX, mouseY, partialTicks, optimizerActive);

        float metricY = heroY + HERO_HEIGHT + CARD_GAP;
        drawMetricsRow(nvg, palette, accent, contentX, metricY, contentWidth);

        float recommendationY = metricY + METRIC_CARD_HEIGHT + CARD_GAP;
        drawRecommendationCard(nvg, palette, accent, contentX, recommendationY, contentWidth);

        nvg.restore();
        nvg.restore();

        nvg.save();
        nvg.translate((float) (settingAnimation.getValue() * 600), 0);

        if (currentSetting != null) {
            drawSettingsOverlay(nvg, palette, mouseX, mouseY, partialTicks, contentTop, contentHeight);
        }

        nvg.restore();
    }

    private void drawProxySection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight) {

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

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (manager == null) {
            return;
        }

        if (mouseButton == 0) {
            for (FilterChip chip : navigationChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click();
                    return;
                }
            }
        }

        if (activeSection == NetworkSection.TWEAKER) {
            handleTweakerMouseClicked(mouseX, mouseY, mouseButton);
        } else if (activeSection == NetworkSection.PROXY) {
            handleProxyMouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

        if (manager == null) {
            return;
        }

        if (activeSection == NetworkSection.TWEAKER) {
            handleTweakerMouseReleased(mouseX, mouseY, mouseButton);
        } else if (activeSection == NetworkSection.PROXY) {
            handleProxyMouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (manager == null) {
            return;
        }

        if (activeSection == NetworkSection.TWEAKER) {
            handleTweakerKeyTyped(typedChar, keyCode);
        }
    }

    private void handleTweakerMouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (!settingsOpen && mouseButton == 0) {

            if (optimizerToggle != null) {
                optimizerToggle.mouseClicked(mouseX, mouseY, mouseButton);
            }

            if (settingsButtonBounds.contains(mouseX, mouseY)) {
                if (!cachedSettings.isEmpty()) {
                    settingsPanel.clear();
                    settingsPanel.buildEntries(cachedSettings);
                }
                currentSetting = true;
                settingsOpen = true;
                setCanClose(false);
                settingsScroll.resetAll();
            }
        }

        if (settingsOpen && settingAnimation.isDone(Direction.BACKWARDS)) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            if (MouseUtils.isInside(mouseX, mouseY, getX() + 22, getY() + 20, 18, 18) && mouseButton == 0) {
                settingsOpen = false;
                setCanClose(true);
                settingsPanel.clear();
                return;
            }
            int x = getX() - 32, y = getY() - 31, width = getWidth() + 32, height = getHeight() + 31;
            if (!MouseUtils.isInside(mouseX, mouseY, x - 5, y - 5, width + 10, height + 10) && mouseButton == 0) {
                settingsOpen = false;
                setCanClose(true);
                settingsPanel.clear();
                return;
            }

            float headerX = getX() + 15;
            float headerY = getY() + 15;
            float headerWidth = getWidth() - 30;
            float headerHeight = getHeight() - 30;
            float contentX = getX() + 25;
            float contentY = headerY + 32;
            float contentWidth = getWidth() - 50;
            float viewportHeight = headerHeight - 47;

            if (settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, contentX, contentY, contentWidth, viewportHeight, settingsScroll)) {
                return;
            }

            if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 41, getY() + 21, 16, 16) && mouseButton == 0) {
                settingsPanel.resetSettings();
            }
        }

        if (settingsOpen && mouseButton == 3) {
            settingsOpen = false;
            setCanClose(true);
            settingsPanel.clear();
        }
    }

    private void handleTweakerMouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (optimizerToggle != null) {
            optimizerToggle.mouseReleased(mouseX, mouseY, mouseButton);
        }

        if (currentSetting != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingsScroll);
        }
    }

    private void handleTweakerKeyTyped(char typedChar, int keyCode) {
        if (currentSetting != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            settingsPanel.keyTyped(typedChar, keyCode);
        }

        if (settingsOpen && keyCode == Keyboard.KEY_ESCAPE) {
            settingsOpen = false;
            setCanClose(true);
            settingsPanel.clear();
            return;
        }

        if (!settingsOpen) {
            overviewScroll.onKey(keyCode);
            if (keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_ESCAPE) {
                getSearchBox().setFocused(true);
            }
        }
    }

    private void handleProxyMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (warpToggle != null) {
            warpToggle.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private void handleProxyMouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (warpToggle != null) {
            warpToggle.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    private void drawHeroCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, int mouseX, int mouseY, float partialTicks, boolean optimizerActive) {

        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color overlay = ColorUtils.applyAlpha(accent.getColor1(), (int) (heroGlowAnimation.getValue() * 90));
        Color overlay2 = ColorUtils.applyAlpha(accent.getColor2(), (int) (heroGlowAnimation.getValue() * 70));

        nvg.drawRoundedRect(x, y, width, HERO_HEIGHT, 14F, base);
        nvg.drawGradientRoundedRect(x, y, width, HERO_HEIGHT, 14F, overlay, overlay2);
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, HERO_HEIGHT - 2F, 13F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 192));

        float titleY = y + 18F;
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_TOGGLE.getText(), x + 20F, titleY, palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_SUMMARY.getText(), x + 20F, titleY + 16F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), HERO_DESCRIPTION_FONT_SIZE, Fonts.REGULAR);

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
        nvg.drawText(LegacyIcon.SETTINGS, settingsX + 3F, settingsY + 3F, palette.getFontColor(ColorType.DARK), 14F, Fonts.LEGACYICON);

        if (!optimizerActive) {
            Color overlayDisabled = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK),
                    (int) (disabledOverlayAnimation.getValue() * 150));
            nvg.drawRoundedRect(x, y, width, HERO_HEIGHT, 14F, overlayDisabled);
            float iconSize = 18F;
            nvg.drawText(LegacyIcon.ALERT_TRIANGLE, x + width - iconSize - 15F, y + HERO_HEIGHT - iconSize - 14F, ColorUtils.applyAlpha(new Color(255, 199, 104), 230), 16F, Fonts.LEGACYICON);
        }
    }

    private void drawMetricsRow(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width) {

        float metricWidth = (width - CARD_GAP) / 2F;
        drawMetricCard(nvg, palette, accent, x, y, metricWidth, TranslateText.NETWORK_METRIC_LATENCY, snapshot != null ? snapshot.getLatencyFocus() : 0F);
        drawMetricCard(nvg, palette, accent, x + metricWidth + CARD_GAP, y, metricWidth, TranslateText.NETWORK_METRIC_THROUGHPUT, snapshot != null ? snapshot.getThroughputFocus() : 0F);
    }

    private void drawMetricCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, TranslateText label, float value) {

        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220);
        nvg.drawRoundedRect(x, y, width, METRIC_CARD_HEIGHT, 10F, base);

        nvg.drawText(label.getText(), x + 16F, y + 20F, palette.getFontColor(ColorType.DARK), 10F, Fonts.MEDIUM);

        float barX = x + 16F;
        float barY = y + METRIC_CARD_HEIGHT - 22F;
        float barWidth = width - 32F;
        float barHeight = 10F;
        Color background = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 140);
        nvg.drawRoundedRect(barX, barY, barWidth, barHeight, barHeight / 2F, background);

        float filledWidth = barWidth * Math.min(1F, Math.max(0F, value));
        Color gradientStart = accent.getColor1();
        Color gradientEnd = accent.getColor2();
        nvg.drawGradientRoundedRect(barX, barY, filledWidth, barHeight, barHeight / 2F, gradientStart, gradientEnd);
    }

    private void drawRecommendationCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width) {

        float height = 120F;
        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210);
        Color start = ColorUtils.applyAlpha(accent.getColor1(), 90);
        Color end = ColorUtils.applyAlpha(accent.getColor2(), 90);

        nvg.drawRoundedRect(x, y, width, height, 12F, base);
        nvg.drawGradientRoundedRect(x, y, width, height, 12F, start, end);
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, height - 2F, 11F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 235));

        float padding = 22F;
        float headerY = y + padding;

        nvg.drawText(TranslateText.NETWORK_CATEGORY_PROFILE.getText(), x + padding, headerY, palette.getFontColor(ColorType.DARK), 12F, Fonts.MEDIUM);

        if (snapshot != null) {
            String bufferValue = snapshot.getRecommendedBufferKb() + " KB";
            nvg.drawText(bufferValue, x + padding, headerY + 18F, palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);

            String summary = TranslateText.NETWORK_OPTIMIZER_SUMMARY.getText();
            nvg.drawText(summary, x + padding, headerY + 40F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9F, Fonts.REGULAR);
        }

        nvg.drawText(TranslateText.NETWORK_SETTINGS_HINT.getText(), x + padding, headerY + 60F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);
    }

    private void drawSettingsOverlay(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight) {
        settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());

        overlayX = getX() + 15F;
        overlayY = getY() + 15F;
        overlayWidth = getWidth() - 30F;
        overlayHeight = getHeight() - 30F;

        if (MouseUtils.isInside(mouseX, mouseY, overlayX, overlayY, overlayWidth, overlayHeight)) {
            settingsScroll.onScroll();
            settingsScroll.onAnimation();
        }

        nvg.drawShadow(overlayX, overlayY, overlayWidth, overlayHeight, 12F, 7);
        nvg.drawRoundedRect(overlayX, overlayY, overlayWidth, overlayHeight, 12F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));
        nvg.drawRoundedRect(overlayX + 1F, overlayY + 1F, overlayWidth - 2F, overlayHeight - 2F, 11F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));

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

    private void drawWarpProxyCard(NanoVGManager nvg,
                                   ColorPalette palette,
                                   AccentColor accent,
                                   float x,
                                   float y,
                                   float width,
                                   int mouseX,
                                   int mouseY,
                                   float partialTicks) {

        boolean proxyEnabled = snapshot != null && snapshot.isWarpProxyEnabled();
        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color overlay = ColorUtils.applyAlpha(accent.getColor1(), proxyEnabled ? 120 : 80);
        Color overlay2 = ColorUtils.applyAlpha(accent.getColor2(), proxyEnabled ? 90 : 60);

        nvg.drawRoundedRect(x, y, width, WARP_CARD_HEIGHT, 12F, base);
        nvg.drawGradientRoundedRect(x, y, width, WARP_CARD_HEIGHT, 12F, overlay, overlay2);
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, WARP_CARD_HEIGHT - 2F, 11F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 235));

        float contentPadding = 18F;
        nvg.drawText(TranslateText.NETWORK_PROXY_WARP.getText(), x + contentPadding, y + contentPadding,
                palette.getFontColor(ColorType.DARK), 13F, Fonts.MEDIUM);

        String statusLabel = resolveWarpStatusLabel(snapshot != null ? snapshot.getWarpStatus() : null);
        Color statusColor = resolveWarpStatusColor(snapshot != null ? snapshot.getWarpStatus() : null, palette, accent, proxyEnabled);
        nvg.drawText(statusLabel, x + contentPadding, y + contentPadding + 16F, statusColor, 10F, Fonts.REGULAR);

        if (warpToggle != null) {
            warpToggle.setScale(1.05F);
            warpToggle.setX(x + width - warpToggle.getWidth() - 16F);
            warpToggle.setY(y + 16F);
            warpToggle.draw(mouseX, mouseY, partialTicks);
        }

        float infoY = y + WARP_CARD_HEIGHT - 20F;
        nvg.drawText(LegacyIcon.DNS, x + contentPadding, infoY - 2F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.LEGACYICON);
        if (snapshot != null && snapshot.getWarpResolver() != null) {
            nvg.drawText(snapshot.getWarpResolver(), x + contentPadding + 18F, infoY,
                    palette.getFontColor(ColorType.NORMAL), 10F, Fonts.REGULAR);
        } else {
            nvg.drawText(TranslateText.NETWORK_PROXY_WARP_RESOLVER_EMPTY.getText(), x + contentPadding + 18F, infoY,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 180), 10F, Fonts.REGULAR);
        }
    }

    private String resolveWarpStatusLabel(WarpProxyManager.WarpStatus status) {
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
            case BYPASSED:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_BYPASSED.getText();
            case ERROR:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_ERROR.getText();
            case IDLE:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_IDLE.getText();
            case DISABLED:
            default:
                return TranslateText.NETWORK_PROXY_WARP_STATUS_DISABLED.getText();
        }
    }

    private Color resolveWarpStatusColor(WarpProxyManager.WarpStatus status,
                                         ColorPalette palette,
                                         AccentColor accent,
                                         boolean enabled) {
        if (status == null) {
            return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210);
        }
        switch (status) {
            case ACTIVE:
                return ColorUtils.applyAlpha(accent.getColor1(), enabled ? 255 : 160);
            case CACHED:
                return ColorUtils.applyAlpha(accent.getColor2(), enabled ? 255 : 160);
            case RESOLVING:
                return ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 230);
            case BYPASSED:
                return ColorUtils.applyAlpha(new Color(255, 193, 94), enabled ? 240 : 150);
            case ERROR:
                return new Color(216, 92, 104);
            case IDLE:
                return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220);
            case DISABLED:
            default:
                return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200);
        }
    }

    private void activateSection(NetworkSection section) {
        if (activeSection != section) {
            activeSection = section;
            onSectionActivated(section);
        }
    }

    private void onSectionActivated(NetworkSection section) {
        if (section == NetworkSection.TWEAKER) {
            settingsOpen = false;
            settingAnimation = new SmoothStepAnimation(260, 1.0);
            settingAnimation.setValue(1.0);
            setCanClose(true);
            settingsPanel.clear();
            overviewScroll.resetAll();
            settingsScroll.resetAll();
            overlayWidth = overlayHeight = 0F;
        } else if (section == NetworkSection.PROXY) {
            setCanClose(true);
            proxyScroll.resetAll();
        }
    }

    private float getTweakerSlideOffset() {
        if (activeSection != NetworkSection.TWEAKER || settingAnimation == null) {
            return 0F;
        }
        return (float) -(600 - (settingAnimation.getValue() * 600));
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
