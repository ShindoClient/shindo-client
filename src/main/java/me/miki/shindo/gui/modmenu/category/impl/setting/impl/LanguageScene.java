package me.miki.shindo.gui.modmenu.category.impl.setting.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.SettingCategory;
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.Language;
import me.miki.shindo.management.language.LanguageManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LanguageScene extends SettingScene {

    private static final float OUTER_PADDING = 26F;
    private static final float ROW_GAP = 14F;
    private static final float CARD_RADIUS = 10F;

    private final Scroll languageScroll = new Scroll();
    private final List<LanguageCard> languageCards = new ArrayList<LanguageCard>();

    private float viewportX;
    private float viewportY;
    private float viewportWidth;
    private float viewportHeight;
    private int columns;
    private float cardWidth;
    private float cardHeight;

    public LanguageScene(SettingCategory parent) {
        super(parent, TranslateText.LANGUAGE, TranslateText.LANGUAGE_DESCRIPTION, LegacyIcon.GLOBE);
    }

    @Override
    public void initGui() {
        languageScroll.resetAll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();
        LanguageManager languageManager = instance.getLanguageManager();

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();
        if (baseHeight <= 0F || baseWidth <= 0F) {
            return;
        }
        float containerRadius = 12F;

        languageCards.clear();

        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, containerRadius, 7);
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, containerRadius,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));

        viewportX = baseX + OUTER_PADDING;
        viewportY = baseY + OUTER_PADDING;
        viewportWidth = baseWidth - OUTER_PADDING * 2F;
        viewportHeight = baseHeight - OUTER_PADDING * 2F;

        columns = viewportWidth > 420F ? 2 : 1;
        cardWidth = (viewportWidth - (ROW_GAP * (columns - 1))) / columns;
        float estimatedRows = Math.max(1F, (float) Language.values().length / (float) columns);
        cardHeight = Math.max(66F, Math.min(86F, viewportHeight / estimatedRows));

        float totalContentHeight = calculateTotalContentHeight(Language.values().length);


        float scrollOffset = languageScroll.getValue();

        nvg.save();
        nvg.scissor(viewportX, viewportY, viewportWidth, viewportHeight);
        nvg.translate(0, scrollOffset);

        int index = 0;
        for (Language language : Language.values()) {
            int row = index / columns;
            int column = index % columns;

            float cardX = viewportX + column * (cardWidth + ROW_GAP);
            float cardY = viewportY + row * (cardHeight + ROW_GAP);

            boolean cardHovered = MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight);
            boolean selected = language.equals(languageManager.getCurrentLanguage());

            language.getAnimation().setAnimation(selected ? 1.0F : 0.0F, 16);

            drawLanguageCard(nvg, palette, accentColor, language, cardX, cardY, cardWidth, cardHeight,
                    cardHovered, selected);

            languageCards.add(new LanguageCard(language, cardX, cardY, cardWidth, cardHeight));
            index++;
        }

        nvg.restore();

        languageScroll.onScroll();
        languageScroll.onAnimation();
        languageScroll.setMaxScroll(Math.max(0F, totalContentHeight - viewportHeight));
    }

    private float calculateTotalContentHeight(int languageCount) {
        int rows = (int) Math.ceil(languageCount / (float) columns);
        return rows * cardHeight + Math.max(0, rows - 1) * ROW_GAP;
    }

    private void drawLanguageCard(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, Language language, float x, float y, float width, float height, boolean hovered, boolean selected) {

        Color baseColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), hovered || selected ? 210 : 175);
        nvg.drawRoundedRect(x, y, width, height, CARD_RADIUS, baseColor);
        nvg.drawGradientRoundedRect(x, y, width, height, CARD_RADIUS, ColorUtils.applyAlpha(accentColor.getColor1(), hovered || selected ? 45 : 25), ColorUtils.applyAlpha(accentColor.getColor2(), hovered || selected ? 45 : 25));

        float flagSize = Math.min(56F, height - 24F);
        float flagX = x + 16F;
        float flagY = y + (height - flagSize) / 2F;
        drawFlag(nvg, language.getFlag(), flagX, flagY, flagSize);

        float textX = flagX + flagSize + 34F;
        float textWidth = width - (textX - x) - 20F;
        textWidth = Math.max(120F, textWidth);

        String languageName = nvg.getLimitText(language.getName(), 11F, Fonts.MEDIUM, textWidth);
        nvg.drawText(languageName, textX, y + 20F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        nvg.drawText(language.getId().toUpperCase(), textX, y + 34F, palette.getFontColor(ColorType.NORMAL), 8.5F, Fonts.REGULAR);

        if (selected) {
            nvg.drawText(LegacyIcon.CHECK, x + width - 22F, y + 18F, ColorUtils.applyAlpha(accentColor.getInterpolateColor(), (int) (language.getAnimation().getValue() * 255)), 13F, Fonts.LEGACYICON);
        } else if (hovered) {
            nvg.drawOutlineRoundedRect(x, y, width, height, CARD_RADIUS, 1.4F, ColorUtils.applyAlpha(accentColor.getColor2(), 160));
        }
    }

    private void drawFlag(NanoVGManager nvg, ResourceLocation flag, float x, float y, float size) {
        float flagWidth = size * 1.6F;
        float flagHeight = size;
        nvg.drawRoundedImage(flag, x, y, flagWidth, flagHeight, 6F);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();
        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            return;
        }

        Shindo instance = Shindo.getInstance();
        LanguageManager languageManager = instance.getLanguageManager();

        for (LanguageCard card : languageCards) {
            if (MouseUtils.isInside(mouseX, mouseY, card.x, card.y, card.width, card.height)) {
                languageManager.setCurrentLanguage(card.language);
                break;
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        languageScroll.onKey(keyCode);
    }

    private static class LanguageCard {
        final Language language;
        final float x;
        final float y;
        final float width;
        final float height;

        LanguageCard(Language language, float x, float y, float width, float height) {
            this.language = language;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
