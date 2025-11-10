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
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.ModManager;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.SearchUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class ModuleCategory extends Category {

    private final Scroll settingScroll = new Scroll();
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final ArrayList<ModuleCard> moduleCardCache = new ArrayList<ModuleCard>();
    Color noColour = new Color(0, 0, 0, 0);
    private ModCategory currentCategory;
    private boolean openSetting;
    private Animation settingAnimation;
    private Mod currentMod;
    private float moduleContentHeight;

    public ModuleCategory(GuiModMenu parent) {
        super(parent, TranslateText.MODULE, LegacyIcon.ARCHIVE, true, true);
    }

    @Override
    public void initGui() {
        currentCategory = ModCategory.ALL;
        openSetting = false;
        settingAnimation = new SmoothStepAnimation(260, 1.0);
        settingAnimation.setValue(1.0);
        settingsPanel.clear();
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
        openSetting = false;
        settingAnimation = new SmoothStepAnimation(260, 1.0);
        settingAnimation.setValue(1.0);
        settingsPanel.clear();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ModManager modManager = instance.getModManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();

        int offsetX = 0;
        float offsetY = 13;
        float scrollValue = scroll.getValue();
        int moduleColumns = resolveModuleColumns();
        CardStyle cardStyle = getCardStyle(moduleColumns);

        settingAnimation.setDirection(openSetting ? Direction.BACKWARDS : Direction.FORWARDS);

        if (settingAnimation.isDone(Direction.FORWARDS)) {
            this.setCanClose(true);
            currentMod = null;
            settingsPanel.clear();
        }

        nvg.save();
        nvg.translate((float) -(600 - (settingAnimation.getValue() * 600)), 0);

        //Draw mod scene

        nvg.save();
        nvg.translate(0, scrollValue);

        for (ModCategory c : ModCategory.values()) {

            float textWidth = nvg.getTextWidth(c.getName(), 9, Fonts.MEDIUM);
            boolean isCurrentCategory = c.equals(currentCategory);

            c.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);

            Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
            Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (c.getBackgroundAnimation().getValue() * 255));
            Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (c.getBackgroundAnimation().getValue() * 255));
            Color textColor = c.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

            nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
            nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);

            nvg.drawText(c.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);

            offsetX += (int) (textWidth + 28);
        }

        offsetY = offsetY + 23;

        rebuildModuleCards(modManager, offsetY, moduleColumns);

        for (ModuleCard card : moduleCardCache) {

            if (card.y + scrollValue + card.height > 0 && card.y + scrollValue < this.getHeight()) {

                float cardY = this.getY() + card.y;
                float radius = moduleColumns == 3 ? 7F : 9F;

                float iconX = card.x + cardStyle.leftPadding;
                float iconY = cardY + (card.height - cardStyle.iconSize) / 2F;

                boolean hasSettings = modManager.getSettingsByMod(card.mod) != null;
                float settingsX = card.x + card.width - cardStyle.settingsSize - cardStyle.settingsPadding;
                float settingsY = cardY + (card.height - cardStyle.settingsSize) / 2F;
                float textSpacing = moduleColumns == 3 ? 8F : 10F;
                float textX = iconX + cardStyle.iconSize + textSpacing;
                float textRight = card.x + card.width - cardStyle.textRightPadding;
                if (hasSettings) {
                    textRight -= (cardStyle.settingsSize + cardStyle.settingsPadding);
                }
                float textWidth = Math.max(80F, textRight - textX);

                boolean hovered = MouseUtils.isInside(mouseX, mouseY, card.x, cardY + scrollValue, card.width, card.height) && !MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY + scrollValue, cardStyle.settingsSize, cardStyle.settingsSize);
                card.mod.getHoverAnimation().setAnimation(hovered ? 1.0F : 0.0F, 18);
                float hoverProgress = card.mod.getHoverAnimation().getValue();

                boolean settingsHover = MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY + scrollValue, cardStyle.settingsSize, cardStyle.settingsSize);
                card.mod.getSettingsHoverAnimation().setAnimation(settingsHover ? 1.0F : 0.0F, 18);
                float settingsHoverAnimation = card.mod.getAnimation().getValue();

                int overlayAlpha = (int) (18 + (hoverProgress * 26));
                int fillAlpha = (int) (220 + (hoverProgress * 32));
                int outlineAlpha = (int) (hoverProgress * 220);

                nvg.drawRoundedRect(card.x, cardY, card.width, card.height, 8F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), fillAlpha));
                nvg.drawGradientRoundedRect(card.x, cardY, card.width, card.height, 8F, ColorUtils.applyAlpha(accentColor.getColor1(), overlayAlpha), ColorUtils.applyAlpha(accentColor.getColor2(), overlayAlpha));

                if (outlineAlpha > 0) {
                    nvg.drawOutlineRoundedRect(card.x, cardY, card.width, card.height, 8F, 1.0F, ColorUtils.applyAlpha(accentColor.getColor2(), outlineAlpha));
                }

                nvg.drawRoundedRect(iconX, iconY, cardStyle.iconSize, cardStyle.iconSize, 6F, palette.getBackgroundColor(ColorType.NORMAL));

                card.mod.getAnimation().setAnimation(card.mod.isToggled() ? 1.0F : 0.0F, 16);

                nvg.save();
                nvg.scale(iconX, iconY, cardStyle.iconSize, cardStyle.iconSize, card.mod.getAnimation().getValue());
                nvg.drawGradientRoundedRect(iconX, iconY, cardStyle.iconSize, cardStyle.iconSize, 6F, ColorUtils.applyAlpha(accentColor.getColor1(), (int) (card.mod.getAnimation().getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (card.mod.getAnimation().getValue() * 255)));
                nvg.restore();



                String modName = nvg.getLimitText(card.mod.getName(), 11.5F, Fonts.MEDIUM, textWidth);
                nvg.drawText(modName, textX, cardY + 14F, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.MEDIUM);

                if (card.mod.isRestricted()) {
                    String warning = "Restricted on some servers";
                    nvg.drawText(nvg.getLimitText(warning, 8F, Fonts.REGULAR, textWidth), textX + 10F, cardY + 36F, new Color(255, 180, 90), 8F, Fonts.REGULAR);
                    nvg.drawText(LegacyIcon.INFO, textX, cardY + card.height - 17F, new Color(255, 180, 90), 8.5F, Fonts.LEGACYICON);
                }

                String description = nvg.getLimitText(card.mod.getDescription(), 8.5F, Fonts.REGULAR, textWidth);
                nvg.drawText(description, textX, cardY + 26F, palette.getFontColor(ColorType.NORMAL), 8.5F, Fonts.REGULAR);

                if (hasSettings) {
                    nvg.drawRoundedRect(settingsX, settingsY, cardStyle.settingsSize, cardStyle.settingsSize, 5F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 180));
                    nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + cardStyle.settingsSize / 2F - 1F, settingsY + cardStyle.settingsSize / 2F - 6F, palette.getFontColor(ColorType.DARK), 14F, Fonts.LEGACYICON);



                    nvg.drawGradientOutlineRoundedRect(settingsX, settingsY, cardStyle.settingsSize, cardStyle.settingsSize, 5F, 1.0F, ColorUtils.applyAlpha(accentColor.getColor1(), (int) (settingsHoverAnimation * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (settingsHoverAnimation * 255)));

                }
            }
        }

        nvg.restore();
        nvg.drawVerticalGradientRect(getX() + 15, this.getY(), getWidth() - 30, 12, palette.getBackgroundColor(ColorType.NORMAL), noColour); //top
        nvg.drawVerticalGradientRect(getX() + 15, this.getY() + this.getHeight() - 12, getWidth() - 30, 12, noColour, palette.getBackgroundColor(ColorType.NORMAL)); // bottom
        nvg.restore();


        //Draw mod setting scene

        nvg.save();
        nvg.translate((float) (settingAnimation.getValue() * 600), 0);

        if (currentMod != null) {

            if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
                settingScroll.onScroll();
                settingScroll.onAnimation();
            }

            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());

            float headerX = this.getX() + 15;
            float headerY = this.getY() + 15;
            float headerWidth = this.getWidth() - 30;
            float headerHeight = this.getHeight() - 30;

            nvg.drawRoundedRect(headerX, headerY, headerWidth, headerHeight, 10, palette.getBackgroundColor(ColorType.DARK));
            nvg.drawText(LegacyIcon.CHEVRON_LEFT, headerX + 10, headerY + 8, palette.getFontColor(ColorType.DARK), 13, Fonts.LEGACYICON);
            nvg.drawText(currentMod.getName(), headerX + 27, headerY + 9, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
            nvg.drawText(LegacyIcon.REFRESH, headerX + headerWidth - 24, headerY + 7.5F, palette.getFontColor(ColorType.DARK), 13, Fonts.LEGACYICON);

            float contentX = this.getX() + 25;
            float contentY = headerY + 32;
            float contentWidth = this.getWidth() - 50;
            float viewportHeight = headerHeight - 47;

            nvg.save();
            nvg.scissor(headerX + 5, contentY - 5, headerWidth - 10, viewportHeight + 10);
            settingsPanel.draw(mouseX, mouseY, partialTicks, contentX, contentY, contentWidth, viewportHeight, nvg, palette, settingScroll);
            nvg.restore();
        }

        nvg.restore();

        float viewportHeight = this.getHeight() - 26F;
        scroll.setMaxScroll(Math.max(0F, moduleContentHeight - viewportHeight));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ModManager modManager = instance.getModManager();

        int offsetX = 0;
        float offsetY = 13 + scroll.getValue();

        if (!openSetting) {
            for (ModCategory c : ModCategory.values()) {

                float textWidth = nvg.getTextWidth(c.getName(), 9, Fonts.MEDIUM);

                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
                    currentCategory = c;
                    scroll.reset();
                }

                offsetX += (int) (textWidth + 28);
            }

            offsetY = offsetY + 23;

            if (moduleCardCache.isEmpty()) {
                rebuildModuleCards(modManager, 36F, resolveModuleColumns());
            }

            CardStyle cardStyle = getCardStyle(resolveModuleColumns());

            for (ModuleCard card : moduleCardCache) {

                float cardY = this.getY() + card.y + scroll.getValue();

                if (!MouseUtils.isInside(mouseX, mouseY, card.x, cardY, card.width, card.height)) {
                    continue;
                }

                if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight()) && mouseButton == 0) {

                    float settingsX = card.x + card.width - cardStyle.settingsSize - cardStyle.settingsPadding;
                    float settingsY = cardY + (card.height - cardStyle.settingsSize) / 2F;

                    if (MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, cardStyle.settingsSize, cardStyle.settingsSize) && !openSetting) {

                        ArrayList<Setting> settings = modManager.getSettingsByMod(card.mod);

                        if (settings != null) {

                            settingsPanel.buildEntries(settings);
                            settingScroll.resetAll();
                            currentMod = card.mod;
                            openSetting = true;
                            this.setCanClose(false);
                        }
                        continue;
                    }

                    float toggleX = card.x + cardStyle.leftPadding;
                    float toggleWidth = card.width - (cardStyle.leftPadding + cardStyle.settingsSize + cardStyle.settingsPadding + 10F);

                    if (MouseUtils.isInside(mouseX, mouseY, toggleX, cardY, toggleWidth, card.height)) {
                        card.mod.toggle();
                    }
                }
            }
        }

        if (openSetting && settingAnimation.isDone(Direction.BACKWARDS)) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 22, this.getY() + 20, 18, 18) && mouseButton == 0) {
                openSetting = false;
                settingsPanel.clear();
                return;
            }
            int x = getX() - 32, y = getY() - 31, width = getWidth() + 32, height = getHeight() + 31;
            if (!MouseUtils.isInside(mouseX, mouseY, x - 5, y - 5, width + 10, height + 10) && mouseButton == 0) {
                openSetting = false;
                settingsPanel.clear();
                return;
            }

            float headerY = this.getY() + 15;
            float headerHeight = this.getHeight() - 30;
            float contentX = this.getX() + 25;
            float contentY = headerY + 32;
            float contentWidth = this.getWidth() - 50;
            float viewportHeight = headerHeight - 47;

            if (settingsPanel.mouseClicked(mouseX, mouseY, mouseButton, contentX, contentY, contentWidth, viewportHeight, settingScroll)) {
                return;
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 41, this.getY() + 21, 16, 16) && mouseButton == 0) {
                settingsPanel.resetSettings();
            }
        }

        if (openSetting && mouseButton == 3) {
            openSetting = false;
            settingsPanel.clear();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (currentMod != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (currentMod != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            settingsPanel.keyTyped(typedChar, keyCode);
        }

        if (openSetting && keyCode == Keyboard.KEY_ESCAPE) {
            openSetting = false;
            settingsPanel.clear();
            return;
        }

        if (!openSetting) {
            scroll.onKey(keyCode);
            if (keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_UP && keyCode != Keyboard.KEY_ESCAPE) {
                this.getSearchBox().setFocused(true);
            }
        }
    }

    private boolean filterMod(Mod m) {

        if (m.isHide()) {
            return true;
        }

        if (!m.getAllowed()) {
            return true;
        }

        if (!currentCategory.equals(ModCategory.ALL) && !m.getCategory().equals(currentCategory)) {
            return true;
        }

        return !this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimilar(Shindo.getInstance().getModManager().getWords(m), this.getSearchBox().getText());
    }

    private void rebuildModuleCards(ModManager modManager, float startOffset, int columns) {
        moduleCardCache.clear();

        int normalizedColumns = Math.max(1, Math.min(columns, 2));
        float spacingX = normalizedColumns > 1 ? 24F : 0F;
        float spacingY = 14F;
        float cardHeight = 54F;
        float availableWidth = this.getWidth() - 30F;
        float cardWidth = normalizedColumns == 1 ? availableWidth : (availableWidth - spacingX) / 2F;

        int columnIndex = 0;
        float rowY = startOffset;

        for (Mod m : modManager.getMods()) {
            if (filterMod(m)) {
                continue;
            }

            float cardX = this.getX() + 15 + columnIndex * (cardWidth + spacingX);
            moduleCardCache.add(new ModuleCard(m, cardX, rowY, cardWidth, cardHeight));

            columnIndex++;
            if (columnIndex >= normalizedColumns) {
                columnIndex = 0;
                rowY += cardHeight + spacingY;
            }
        }

        if (moduleCardCache.isEmpty()) {
            moduleContentHeight = Math.max(0F, startOffset - 13F);
            return;
        }

        ModuleCard last = moduleCardCache.get(moduleCardCache.size() - 1);
        float lastBottom = last.y + last.height;

        moduleContentHeight = Math.max(0F, lastBottom - 13F);
    }

    private int resolveModuleColumns() {
        return Math.max(1, Math.min(2, InternalSettingsMod.getInstance().getModuleGridColumns()));
    }

    private CardStyle getCardStyle(int columns) {
        switch (columns) {
            case 1:
                return new CardStyle(28F, 20F, 18F, 14F, 18F);
            case 2:
                return new CardStyle(28F, 18F, 18F, 12F, 16F);
        }
        return new CardStyle(26F, 20F, 18F, 14F, 18F);
    }

    private static class ModuleCard {
        final Mod mod;
        final float x;
        final float y;
        final float width;
        final float height;

        ModuleCard(Mod mod, float x, float y, float width, float height) {
            this.mod = mod;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class CardStyle {
        final float iconSize;
        final float leftPadding;
        final float settingsSize;
        final float settingsPadding;
        final float textRightPadding;

        CardStyle(float iconSize, float leftPadding, float settingsSize, float settingsPadding, float textRightPadding) {
            this.iconSize = iconSize;
            this.leftPadding = leftPadding;
            this.settingsSize = settingsSize;
            this.settingsPadding = settingsPadding;
            this.textRightPadding = textRightPadding;
        }
    }
}
