package me.miki.shindo.gui.modmenu.category.impl.network.module;

import me.miki.shindo.gui.modmenu.category.impl.network.NetworkSection;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.network.ConnectionTweakerManager.ProfileSnapshot;
import me.miki.shindo.management.network.proxy.WarpProxyManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.awt.Color;

public class WarpProxyModule implements NetworkModule {

    private static final float HERO_PADDING = 20F;
    private static final float WARP_CARD_HEIGHT = 72F;

    private final Scroll proxyScroll = new Scroll();

    @Override
    public NetworkSection getSection() {
        return NetworkSection.PROXY;
    }

    @Override
    public void init(NetworkModuleContext context) {
        proxyScroll.resetAll();
    }

    @Override
    public void draw(NetworkModuleContext context, NanoVGManager nvg, ColorPalette palette, AccentColor accent, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight) {

        float estimated = WARP_CARD_HEIGHT + 36F;
        proxyScroll.setMaxScroll(Math.max(0F, estimated - contentHeight));
        if (MouseUtils.isInside(mouseX, mouseY, context.getCategory().getX(), contentTop,
                context.getCategory().getWidth(), contentHeight)) {
            proxyScroll.onScroll();
        }
        proxyScroll.onAnimation();
        float scrollOffset = proxyScroll.getValue();

        nvg.save();
        nvg.scissor(context.getCategory().getX(), contentTop, context.getCategory().getWidth(), contentHeight);

        float cardX = context.getCategory().getX() + HERO_PADDING;
        float cardY = contentTop + 18F + scrollOffset;
        float cardWidth = context.getCategory().getWidth() - HERO_PADDING * 2F;

        drawWarpProxyCard(context, nvg, palette, accent, cardX, cardY, cardWidth, mouseX, mouseY, partialTicks);

        nvg.restore();
    }

    @Override
    public void mouseClicked(NetworkModuleContext context, int mouseX, int mouseY, int mouseButton) {
        CompToggleButton warpToggle = context.getWarpToggle();
        if (warpToggle != null) {
            warpToggle.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(NetworkModuleContext context, int mouseX, int mouseY, int mouseButton) {
        CompToggleButton warpToggle = context.getWarpToggle();
        if (warpToggle != null) {
            warpToggle.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onSectionActivated(NetworkModuleContext context) {
        proxyScroll.resetAll();
    }

    private void drawWarpProxyCard(NetworkModuleContext context,
                                   NanoVGManager nvg,
                                   ColorPalette palette,
                                   AccentColor accent,
                                   float x,
                                   float y,
                                   float width,
                                   int mouseX,
                                   int mouseY,
                                   float partialTicks) {

        ProfileSnapshot snapshot = context.getSnapshot();
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

        CompToggleButton warpToggle = context.getWarpToggle();
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
}
