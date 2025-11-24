package me.miki.shindo.gui.modmenu.category.impl.network.module;

import me.miki.shindo.gui.modmenu.category.impl.network.NetworkSection;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.network.ConnectionTweakerManager.ProfileSnapshot;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.util.List;

public class TweakerOverviewModule implements NetworkModule {

    private static final float HERO_HEIGHT = 122F;
    private static final float HERO_PADDING = 20F;
    private static final float CARD_GAP = 12F;
    private static final float METRIC_CARD_HEIGHT = 60F;
    private static final float HERO_DESCRIPTION_FONT_SIZE = 9.5F;

    private final Scroll overviewScroll = new Scroll();
    private final Scroll settingsScroll = new Scroll();
    private final SimpleAnimation heroGlowAnimation = new SimpleAnimation();
    private final SimpleAnimation disabledOverlayAnimation = new SimpleAnimation();
    private final Rect settingsButtonBounds = new Rect();
    private boolean settingsOpen;
    private float overlayX;
    private float overlayY;
    private float overlayWidth;
    private float overlayHeight;

    @Override
    public NetworkSection getSection() {
        return NetworkSection.TWEAKER;
    }

    @Override
    public void init(NetworkModuleContext context) {
        settingsOpen = false;
        overviewScroll.resetAll();
        settingsScroll.resetAll();
        overlayWidth = overlayHeight = 0F;
    }

    @Override
    public void draw(NetworkModuleContext context, NanoVGManager nvg, ColorPalette palette, AccentColor accent, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight) {

        ProfileSnapshot snapshot = context.getSnapshot();
        boolean optimizerActive = snapshot != null && snapshot.isOptimizerEnabled();
        heroGlowAnimation.setAnimation(optimizerActive ? 1F : 0.75F, 18);
        disabledOverlayAnimation.setAnimation(optimizerActive ? 0F : 1F, 16);

        float estimated = HERO_HEIGHT + METRIC_CARD_HEIGHT + 120F;
        overviewScroll.setMaxScroll(Math.max(0F, estimated - contentHeight + 40F));

        if (!settingsOpen && MouseUtils.isInside(mouseX, mouseY, context.getCategory().getX(), contentTop,
                context.getCategory().getWidth(), contentHeight)) {
            overviewScroll.onScroll();
        }
        overviewScroll.onAnimation();
        float scrollValue = overviewScroll.getValue();

        nvg.save();
        nvg.scissor(context.getCategory().getX(), contentTop, context.getCategory().getWidth(), contentHeight);
        nvg.translate(0, scrollValue);

        float contentX = context.getCategory().getX() + HERO_PADDING;
        float contentWidth = context.getCategory().getWidth() - HERO_PADDING * 2F;

        float heroY = contentTop + 18F;
        drawHeroCard(context, nvg, palette, accent, contentX, heroY, contentWidth, mouseX, mouseY, partialTicks, optimizerActive);

        float metricY = heroY + HERO_HEIGHT + CARD_GAP;
        drawMetricsRow(context, nvg, palette, accent, contentX, metricY, contentWidth);

        float recommendationY = metricY + METRIC_CARD_HEIGHT + CARD_GAP;
        drawRecommendationCard(context, nvg, palette, accent, contentX, recommendationY, contentWidth);

        nvg.restore();

        if (settingsOpen) {
            drawSettingsOverlay(context, nvg, palette, mouseX, mouseY, partialTicks, contentTop, contentHeight);
        }
    }

    @Override
    public void mouseClicked(NetworkModuleContext context, int mouseX, int mouseY, int mouseButton) {
        if (settingsOpen) {
            handleSettingsInteraction(context, mouseX, mouseY, mouseButton);
            return;
        }

        CompToggleButton optimizerToggle = context.getOptimizerToggle();
        if (optimizerToggle != null) {
            optimizerToggle.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (mouseButton == 0 && settingsButtonBounds.contains(mouseX, mouseY)) {
            List<Setting> settings = context.getCachedSettings();
            SettingsPanel panel = context.getSettingsPanel();
            if (settings != null && !settings.isEmpty()) {
                panel.clear();
                panel.buildEntries(settings);
            }
            settingsOpen = true;
            settingsScroll.resetAll();
        }
    }

    @Override
    public void mouseReleased(NetworkModuleContext context, int mouseX, int mouseY, int mouseButton) {
        if (context.getOptimizerToggle() != null) {
            context.getOptimizerToggle().mouseReleased(mouseX, mouseY, mouseButton);
        }
        if (settingsOpen) {
            context.getSettingsPanel().mouseReleased(mouseX, mouseY, mouseButton, settingsScroll);
        }
    }

    @Override
    public void keyTyped(NetworkModuleContext context, char typedChar, int keyCode) {
        if (!settingsOpen) {
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            settingsOpen = false;
            context.getSettingsPanel().clear();
            overlayWidth = overlayHeight = 0F;
            return;
        }
        context.getSettingsPanel().keyTyped(typedChar, keyCode);
        settingsScroll.onKey(keyCode);
    }

    @Override
    public void onSectionActivated(NetworkModuleContext context) {
        settingsOpen = false;
        context.getSettingsPanel().clear();
        overviewScroll.resetAll();
        settingsScroll.resetAll();
        overlayWidth = overlayHeight = 0F;
    }

    private void drawHeroCard(NetworkModuleContext context,
                              NanoVGManager nvg,
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
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, HERO_HEIGHT - 2F, 13F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 192));

        float titleY = y + 18F;
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_TOGGLE.getText(), x + 20F, titleY,
                palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.NETWORK_OPTIMIZER_SUMMARY.getText(), x + 20F, titleY + 16F,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                HERO_DESCRIPTION_FONT_SIZE, Fonts.REGULAR);

        CompToggleButton optimizerToggle = context.getOptimizerToggle();
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

    private void drawMetricsRow(NetworkModuleContext context, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width) {

        ProfileSnapshot snapshot = context.getSnapshot();
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

    private void drawRecommendationCard(NetworkModuleContext context, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width) {

        ProfileSnapshot snapshot = context.getSnapshot();
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

    private void drawSettingsOverlay(NetworkModuleContext context, NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight) {
        SettingsPanel panel = context.getSettingsPanel();
        panel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());

        overlayX = context.getCategory().getX() + 15F;
        overlayY = contentTop + 15F;
        overlayWidth = context.getCategory().getWidth() - 30F;
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
        panel.draw(mouseX, mouseY, partialTicks, contentX, contentY, contentWidth, viewportHeight, nvg, palette, settingsScroll);
        nvg.restore();
    }

    private void handleSettingsInteraction(NetworkModuleContext context, int mouseX, int mouseY, int mouseButton) {
        float headerX = overlayWidth > 0F ? overlayX : context.getCategory().getX() + 15F;
        float headerY = overlayHeight > 0F ? overlayY : context.getCategory().getY() + 27F;
        float headerWidth = overlayWidth > 0F ? overlayWidth : context.getCategory().getWidth() - 30F;
        float headerHeight = overlayHeight > 0F ? overlayHeight : context.getCategory().getHeight() - (headerY - context.getCategory().getY()) - 27F;
        float contentX = headerX + 10F;
        float contentY = headerY + 32F;
        float contentWidth = headerWidth - 20F;
        float viewportHeight = headerHeight - 47F;
        SettingsPanel panel = context.getSettingsPanel();

        if (mouseButton == 0) {
            if (MouseUtils.isInside(mouseX, mouseY, headerX + 6F, headerY + 6F, 20F, 18F)) {
                settingsOpen = false;
                panel.clear();
                overlayWidth = overlayHeight = 0F;
                return;
            }
            if (MouseUtils.isInside(mouseX, mouseY, headerX + headerWidth - 28F, headerY + 6F, 18F, 18F)) {
                panel.resetSettings();
                return;
            }
        }

        if (!MouseUtils.isInside(mouseX, mouseY, headerX - 4F, headerY - 4F, headerWidth + 8F, headerHeight + 8F) && mouseButton == 0) {
            settingsOpen = false;
            panel.clear();
            overlayWidth = overlayHeight = 0F;
            return;
        }

        if (!panel.mouseClicked(mouseX, mouseY, mouseButton, contentX, contentY, contentWidth, viewportHeight, settingsScroll)) {
            // Nothing consumed
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
