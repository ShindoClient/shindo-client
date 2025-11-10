package me.miki.shindo.gui.modmenu.category.impl.setting.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.SettingCategory;
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.Theme;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.ui.comp.impl.CompComboBox;
import me.miki.shindo.ui.comp.impl.CompSettingButton;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class AppearanceScene extends SettingScene {

    private static final float OUTER_PADDING = 36F;
    private static final float SECTION_SPACING = 40F;
    private static final float INNER_PADDING = 18F;
    private static final float THEME_ITEM_WIDTH = 112F;
    private static final float THEME_ITEM_SPACING = 18F;
    private static final float ACCENT_ITEM_WIDTH = 96F;
    private static final float ACCENT_ITEM_SPACING = 16F;

    private final Scroll contentScroll = new Scroll();
    private final Scroll themeScroll = new Scroll();
    private final Scroll accentScroll = new Scroll();
    private final List<CardHitbox<Theme>> themeHitboxes = new ArrayList<CardHitbox<Theme>>();
    private final List<CardHitbox<AccentColor>> accentHitboxes = new ArrayList<CardHitbox<AccentColor>>();

    private float themeSectionX;
    private float themeSectionY;
    private float themeSectionWidth;
    private float themeSectionHeight;

    private float accentSectionX;
    private float accentSectionY;
    private float accentSectionWidth;
    private float accentSectionHeight;

    private float cardX;
    private float cardY;
    private float cardWidth;
    private float cardHeight;


    private CompComboBox modTheme;
    private CompToggleButton uiBlur;

    private final List<CompSettingButton> settingCards = new ArrayList<CompSettingButton>();

    public AppearanceScene(SettingCategory parent) {
        super(parent, TranslateText.APPEARANCE, TranslateText.APPEARANCE_DESCRIPTION, LegacyIcon.MONITOR);
    }

    @Override
    public void initGui() {
        modTheme = new CompComboBox(110, InternalSettingsMod.getInstance().getModThemeSetting());
        uiBlur = new CompToggleButton(InternalSettingsMod.getInstance().getBlurSetting());


        settingCards.clear();

        settingCards.add(new CompSettingButton(0F, TranslateText.HUD_THEME::getText, TranslateText.STYLE::getText)
                .trailing(modTheme));

        settingCards.add(new CompSettingButton(0F, TranslateText.UI_BLUR::getText, TranslateText.SMOOTH::getText)
                .trailing(uiBlur)
                .onClick(() -> uiBlur.getSetting().setToggled(!uiBlur.getSetting().isToggled())));


        contentScroll.resetAll();
        themeScroll.resetAll();
        accentScroll.resetAll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor currentAccent = colorManager.getCurrentColor();
        Theme currentTheme = colorManager.getTheme();

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (baseWidth <= 0F || baseHeight <= 0F) {
            return;
        }

        themeHitboxes.clear();
        accentHitboxes.clear();

        float containerRadius = 12F;
        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, containerRadius, 7);
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, containerRadius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));
        nvg.drawRoundedRect(baseX + 1F, baseY + 1F, baseWidth - 2F, baseHeight - 2F, containerRadius - 1F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));

        float top = baseY + OUTER_PADDING;
        float themeHeight = 122F;
        float accentHeight = 120F;
        float controlHeight = 54F;

        themeSectionX = baseX + OUTER_PADDING;
        themeSectionY = top;
        themeSectionWidth = Math.max(0F, baseWidth - OUTER_PADDING * 2F);
        themeSectionHeight = themeHeight;

        accentSectionX = themeSectionX;
        accentSectionY = themeSectionY + themeSectionHeight + SECTION_SPACING;
        accentSectionWidth = themeSectionWidth;
        accentSectionHeight = accentHeight;

        cardX = themeSectionX;
        cardY = accentSectionY + accentSectionHeight + 10F;
        cardWidth = (themeSectionWidth);
        cardHeight = controlHeight;


        float contentHeight = OUTER_PADDING + themeSectionHeight + SECTION_SPACING + accentSectionHeight + 10F + ((controlHeight * settingCards.size()) + 18F) + OUTER_PADDING;
        contentScroll.setMaxScroll(Math.max(0F, contentHeight - baseHeight));



        float rawVertical = contentScroll.getValue();
        float themeAreaTop = themeSectionY + rawVertical;
        float accentAreaTop = accentSectionY + rawVertical;

        if (MouseUtils.isInside(mouseX, mouseY, themeSectionX, themeAreaTop, themeSectionWidth, themeSectionHeight)) {
            themeScroll.onScroll();
        }
        if (MouseUtils.isInside(mouseX, mouseY, accentSectionX, accentAreaTop, accentSectionWidth, accentSectionHeight)) {
            accentScroll.onScroll();
        }

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)&& !MouseUtils.isInside(mouseX, mouseY, themeSectionX, themeAreaTop, themeSectionWidth, themeSectionHeight) && !MouseUtils.isInside(mouseX, mouseY, accentSectionX, accentAreaTop, accentSectionWidth, accentSectionHeight)) {
            contentScroll.onScroll();
        }

        contentScroll.onAnimation();
        themeScroll.onAnimation();
        accentScroll.onAnimation();

        float verticalScroll = contentScroll.getValue();
        float themeScreenY = themeSectionY + verticalScroll;
        float accentScreenY = accentSectionY + verticalScroll;
        float controlsScreenY = cardY + verticalScroll;

        nvg.save();
        nvg.scissor(baseX, baseY, baseWidth, baseHeight);

        drawSectionTitle(nvg, TranslateText.THEME.getText(), TranslateText.THEME_DESCRIPTION.getText(), themeSectionX, themeScreenY - 26F, palette);
        drawThemeCarousel(mouseX, mouseY, partialTicks, nvg, colorManager, palette, currentTheme, currentAccent, themeScreenY);

        drawSectionTitle(nvg, TranslateText.ACCENT_COLOR.getText(), TranslateText.DESIGN.getText(), accentSectionX, accentScreenY - 26F, palette);
        drawAccentCarousel(mouseX, mouseY, partialTicks, nvg, colorManager, palette, currentAccent, accentScreenY);

        drawControlCards(mouseX, mouseY, partialTicks, controlsScreenY);
        nvg.resetScissor();
        nvg.restore();

        drawScrollbar(nvg, palette, currentAccent, baseX, baseY, baseWidth, baseHeight, contentHeight, verticalScroll);
    }

    private void drawSectionTitle(NanoVGManager nvg, String title, String subtitle, float x, float y, ColorPalette palette) {
        nvg.drawText(title, x, y, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);
        if (subtitle != null && !subtitle.isEmpty() && !"null".equalsIgnoreCase(subtitle)) {
            nvg.drawText(subtitle, x, y + 12F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220), 8.5F, Fonts.REGULAR);
        }
    }

    private void drawThemeCarousel(int mouseX, int mouseY, float partialTicks, NanoVGManager nvg, ColorManager colorManager, ColorPalette palette, Theme currentTheme, AccentColor accent, float sectionY) {

        float radius = 10F;
        nvg.drawRoundedRect(themeSectionX, sectionY, themeSectionWidth, themeSectionHeight, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 170));
        nvg.drawGradientRoundedRect(themeSectionX, sectionY, themeSectionWidth, themeSectionHeight, radius, ColorUtils.applyAlpha(accent.getColor1(), 35), ColorUtils.applyAlpha(accent.getColor2(), 35));

        float innerX = themeSectionX + INNER_PADDING;
        float innerY = sectionY + INNER_PADDING;
        float visibleWidth = themeSectionWidth - INNER_PADDING * 2F;

        float totalWidth = Theme.values().length * THEME_ITEM_WIDTH + (Theme.values().length - 1) * THEME_ITEM_SPACING;
        themeScroll.setMaxScroll(Math.max(0F, totalWidth - visibleWidth));

        float scroll = themeScroll.getValue();
        float itemHeight = Math.min(88F, themeSectionHeight - INNER_PADDING * 2F);

        nvg.save();
        nvg.intersectScissor(themeSectionX, sectionY, themeSectionWidth, themeSectionHeight);

        float cardX = innerX + scroll;
        for (Theme theme : Theme.values()) {
            float screenX = cardX;
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, THEME_ITEM_WIDTH, itemHeight);
            boolean selected = theme.equals(colorManager.getTheme());

            theme.getAnimation().setAnimation(selected ? 1.0F : 0.0F, 18);

            Color baseColor = ColorUtils.applyAlpha(theme.getNormalBackgroundColor(), hovered || selected ? 240 : 205);
            Color overlayColor = ColorUtils.applyAlpha(theme.getDarkBackgroundColor(), hovered || selected ? 220 : 185);

            nvg.drawRoundedRect(screenX, innerY, THEME_ITEM_WIDTH, itemHeight, 10F, baseColor);
            nvg.drawGradientRoundedRect(screenX, innerY, THEME_ITEM_WIDTH, itemHeight, 10F, baseColor, overlayColor);

            nvg.drawRoundedRect(screenX + 12F, innerY + 16F, THEME_ITEM_WIDTH - 24F, 12F, 4F, ColorUtils.applyAlpha(theme.getDarkFontColor(), 210));
            nvg.drawRoundedRect(screenX + 12F, innerY + 34F, THEME_ITEM_WIDTH - 24F, 7F, 3F, ColorUtils.applyAlpha(theme.getNormalFontColor(), 190));

            String label = nvg.getLimitText(theme.getName(), 9.5F, Fonts.MEDIUM, THEME_ITEM_WIDTH - 24F);
            nvg.drawText(label, screenX + 12F, innerY + itemHeight - 22F, Color.WHITE, 9.5F, Fonts.MEDIUM);

            if (selected) {
                nvg.drawText(LegacyIcon.CHECK, screenX + THEME_ITEM_WIDTH - 18F, innerY + 12F, new Color(255, 255, 255, (int) Math.min(255, 180 + theme.getAnimation().getValue() * 60F)), 12F, Fonts.LEGACYICON);
            } else if (hovered) {
                nvg.drawOutlineRoundedRect(screenX, innerY, THEME_ITEM_WIDTH, itemHeight, 10F, 2,ColorUtils.applyAlpha(accent.getColor2(), 160));
            }

            themeHitboxes.add(new CardHitbox<Theme>(theme, screenX, innerY, THEME_ITEM_WIDTH, itemHeight));
            cardX += THEME_ITEM_WIDTH + THEME_ITEM_SPACING;
        }
        nvg.resetScissor();
        nvg.restore();
    }

    private void drawAccentCarousel(int mouseX, int mouseY, float partialTicks, NanoVGManager nvg, ColorManager colorManager, ColorPalette palette, AccentColor currentAccent, float sectionY) {

        float radius = 10F;
        nvg.drawRoundedRect(accentSectionX, sectionY, accentSectionWidth, accentSectionHeight, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 165));
        nvg.drawGradientRoundedRect(accentSectionX, sectionY, accentSectionWidth, accentSectionHeight, radius, ColorUtils.applyAlpha(currentAccent.getColor1(), 28), ColorUtils.applyAlpha(currentAccent.getColor2(), 28));

        float innerX = accentSectionX + INNER_PADDING;
        float innerY = sectionY + INNER_PADDING;
        float visibleWidth = accentSectionWidth - INNER_PADDING * 2F;

        float totalWidth = colorManager.getColors().size() * ACCENT_ITEM_WIDTH + (colorManager.getColors().size() - 1) * ACCENT_ITEM_SPACING;
        accentScroll.setMaxScroll(Math.max(0F, totalWidth - visibleWidth));

        float scroll = accentScroll.getValue();
        float itemHeight = 76F;
        nvg.save();
        nvg.intersectScissor(accentSectionX, sectionY, accentSectionWidth, accentSectionHeight);

        float cardX = innerX + scroll;
        for (AccentColor accent : colorManager.getColors()) {
            float screenX = cardX;
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight);
            boolean selected = accent.equals(currentAccent);

            accent.getAnimation().setAnimation(selected ? 1.0F : 0.0F, 18);

            nvg.drawRoundedRect(screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight, 10F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered || selected ? 220 : 190));
            nvg.drawGradientRoundedRect(screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight, 10F, ColorUtils.applyAlpha(accent.getColor1(), selected ? 220 : 185), ColorUtils.applyAlpha(accent.getColor2(), selected ? 220 : 185));

            if (selected) {
                nvg.drawText(LegacyIcon.CHECK, screenX + ACCENT_ITEM_WIDTH - 18F, innerY + 10F, new Color(255, 255, 255, (int) (accent.getAnimation().getValue() * 255)), 12F, Fonts.LEGACYICON);
            } else if (hovered) {
                nvg.drawOutlineRoundedRect(screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight, 10F, 2, ColorUtils.applyAlpha(accent.getColor2(), 160));
            }

            String label = nvg.getLimitText(accent.getName(), 8.5F, Fonts.MEDIUM, ACCENT_ITEM_WIDTH - 16F);
            nvg.drawCenteredText(label, screenX + ACCENT_ITEM_WIDTH / 2F, innerY + itemHeight - 18F, Color.WHITE, 8.5F, Fonts.MEDIUM);

            accentHitboxes.add(new CardHitbox<AccentColor>(accent, screenX, innerY, ACCENT_ITEM_WIDTH, itemHeight));
            cardX += ACCENT_ITEM_WIDTH + ACCENT_ITEM_SPACING;
        }
        nvg.resetScissor();
        nvg.restore();
    }

    private void drawControlCards(int mouseX, int mouseY, float partialTicks, float sectionY) {

        float currentY = sectionY;
        float cardW = cardWidth - 28F;

        for (CompSettingButton card : settingCards) {
            card.setBounds(cardX + 14F, currentY, cardW, cardHeight);
            card.draw(mouseX, mouseY, partialTicks);
            currentY += cardHeight + 18F;
        }
    }

    private void drawScrollbar(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float baseX, float baseY, float baseWidth, float baseHeight, float contentHeight, float scrollValue) {

        float maxScroll = Math.max(0F, contentHeight - baseHeight);
        if (maxScroll <= 0F) {
            return;
        }

        float trackX = baseX + baseWidth - 10F;
        float trackY = baseY + 12F;
        float trackWidth = 4F;
        float trackHeight = baseHeight - 24F;

        nvg.drawRoundedRect(trackX, trackY, trackWidth, trackHeight, 2F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 130));

        float visibleRatio = Math.min(1F, baseHeight / contentHeight);
        float handleHeight = Math.max(30F, trackHeight * visibleRatio);
        float scrollOffset = -scrollValue;
        float handleY = trackY + (trackHeight - handleHeight) * (scrollOffset / maxScroll);

        nvg.drawGradientRoundedRect(trackX - 1F, handleY, trackWidth + 2F, handleHeight, 3F,
                ColorUtils.applyAlpha(accent.getColor1(), 190), ColorUtils.applyAlpha(accent.getColor2(), 190));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            return;
        }

        for (CompSettingButton card : settingCards) {
            card.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (mouseButton != 0) {
            return;
        }

        ColorManager colorManager = Shindo.getInstance().getColorManager();

        for (CardHitbox<Theme> hitbox : themeHitboxes) {
            if (MouseUtils.isInside(mouseX, mouseY, hitbox.x, hitbox.y, hitbox.width, hitbox.height)) {
                colorManager.setTheme(hitbox.data);
                return;
            }
        }

        for (CardHitbox<AccentColor> hitbox : accentHitboxes) {
            if (MouseUtils.isInside(mouseX, mouseY, hitbox.x, hitbox.y, hitbox.width, hitbox.height)) {
                colorManager.setCurrentColor(hitbox.data);
                return;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (CompSettingButton card : settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (CompSettingButton card : settingCards) {
            card.keyTyped(typedChar, keyCode);
        }

        contentScroll.onKey(keyCode);
    }

    private static class CardHitbox<T> {
        final T data;
        final float x;
        final float y;
        final float width;
        final float height;

        CardHitbox(T data, float x, float y, float width, float height) {
            this.data = data;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
