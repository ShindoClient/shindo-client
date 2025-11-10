package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonManager;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.SearchUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class AddonCategory extends Category {

    private final Scroll settingScroll = new Scroll();
    private final SettingsPanel settingsPanel = new SettingsPanel();
    Color noColour = new Color(0, 0, 0, 0);
    private static final float TYPE_CHIP_HEIGHT = 22F;
    private static final float TYPE_CHIP_GAP = 8F;
    private static final float CHIP_HORIZONTAL_PADDING = 12F;
    private static final float CARD_HORIZONTAL_PADDING = 18F;
    private static final float CARD_COLUMN_GAP = 16F;
    private static final float CARD_ROW_GAP = 16F;
    private static final float CARD_HEIGHT = 108F;
    private AddonType currentType;
    private boolean openSetting;
    private Animation settingAnimation;
    private Addon currentAddon;


    public AddonCategory(GuiModMenu parent) {
        super(parent, TranslateText.ADDONS, LegacyIcon.PIECE, true, true);

    }

    @Override
    public void initGui() {
        currentType = AddonType.ALL;
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
        AddonManager addonManager = instance.getAddonManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();

        float scrollValue = scroll.getValue();

        settingAnimation.setDirection(openSetting ? Direction.BACKWARDS : Direction.FORWARDS);

        if (settingAnimation.isDone(Direction.FORWARDS)) {
            this.setCanClose(true);
            currentAddon = null;
            settingsPanel.clear();
        }

        ArrayList<Addon> visibleAddons = collectVisibleAddons(addonManager);
        float contentStartY = this.getY() + 52F;
        float cardWidth = ((this.getWidth() - (CARD_HORIZONTAL_PADDING * 2) - CARD_COLUMN_GAP) / 2F);
        float viewportHeight = this.getHeight() - (contentStartY - this.getY()) - 24F;

        nvg.save();
        nvg.translate((float) -(600 - (settingAnimation.getValue() * 600)), 0);

        nvg.save();
        nvg.translate(0, scrollValue);
        // draw filter chips
        float chipX = this.getX() + CARD_HORIZONTAL_PADDING;
        float chipY = this.getY() + 16F;
        for (AddonType type : AddonType.values()) {
            float labelWidth = nvg.getTextWidth(type.getName(), 9.5F, Fonts.MEDIUM);
            float chipWidth = labelWidth + (CHIP_HORIZONTAL_PADDING * 2);
            boolean isCurrent = type.equals(currentType);
            boolean hovered = !openSetting && MouseUtils.isInside(mouseX, mouseY, chipX, chipY - 4F, chipWidth, TYPE_CHIP_HEIGHT);

            type.getBackgroundAnimation().setAnimation(isCurrent ? 1.0F : 0.0F, 16);

            Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered || isCurrent ? 210 : 168);
            Color start = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (type.getBackgroundAnimation().getValue() * 255));
            Color end = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (type.getBackgroundAnimation().getValue() * 255));
            Color textColor = type.getTextColorAnimation().getColor(isCurrent ? Color.WHITE : palette.getFontColor(ColorType.DARK), 18);

            nvg.drawRoundedRect(chipX, chipY - 4F, chipWidth, TYPE_CHIP_HEIGHT, 6F, base);
            nvg.drawGradientRoundedRect(chipX, chipY - 4F, chipWidth, TYPE_CHIP_HEIGHT, 6F, start, end);
            nvg.drawCenteredText(type.getName(), chipX + chipWidth / 2F, chipY + 4F, textColor, 9.5F, Fonts.MEDIUM);

            chipX += chipWidth + TYPE_CHIP_GAP;
        }

        for (int i = 0; i < visibleAddons.size(); i++) {
            Addon addon = visibleAddons.get(i);
            int column = i % 2;
            int row = i / 2;

            float cardX = this.getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP);
            float cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP);

            if (cardY + scrollValue > this.getY() + this.getHeight() || cardY + scrollValue + CARD_HEIGHT < this.getY()) {
                continue;
            }

            boolean hovered = !openSetting && MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT);
            addon.getAnimation().setAnimation(addon.isToggled() ? 1.0F : 0.0F, 16);

            Color cardBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered ? 224 : 188);
            Color overlayStart = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (addon.getAnimation().getValue() * 60));
            Color overlayEnd = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (addon.getAnimation().getValue() * 60));

            //nvg.drawShadow(cardX, cardY, cardWidth, CARD_HEIGHT, 10, 6);
            nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12F, cardBase);
            nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12F, overlayStart, overlayEnd);

            float titleX = cardX + 16F;
            float titleY = cardY + 18F;
            float textWidth = cardWidth - 32F;

            nvg.drawText(addon.getName(), titleX, titleY, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.MEDIUM);

            String description = addon.getDescription() == null ? "" : addon.getDescription();
            String wrapped = nvg.getLimitText(description, 8.5F, Fonts.REGULAR, textWidth);
            nvg.drawText(wrapped, titleX, titleY + 16F, palette.getFontColor(ColorType.NORMAL), 8.5F, Fonts.REGULAR);

            String typeName = addon.getType().getName();
            //float badgeWidth = Math.max(42F, nvg.getTextWidth(typeName, 8F, Fonts.MEDIUM) + 20F);
            //float badgeX = cardX + 16F;
            //float badgeY = cardY + CARD_HEIGHT - 30F;

            //Color badgeBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 206);
            //nvg.drawRoundedRect(badgeX, badgeY, badgeWidth, 18F, 6F, badgeBase);
            //nvg.drawCenteredText(typeName, badgeX + badgeWidth / 2F, badgeY + 9F, palette.getFontColor(ColorType.DARK), 8F, Fonts.MEDIUM);

            float toggleWidth = 68F;
            float toggleHeight = 22F;
            float toggleX = cardX + cardWidth - toggleWidth - 16F;
            float toggleY = cardY + CARD_HEIGHT - toggleHeight - 18F;

            Color toggleBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 204);
            Color toggleActiveStart = ColorUtils.applyAlpha(accentColor.getColor1(), 200);
            Color toggleActiveEnd = ColorUtils.applyAlpha(accentColor.getColor2(), 200);

            nvg.drawRoundedRect(toggleX, toggleY, toggleWidth, toggleHeight, 12F, toggleBase);

            nvg.save();
            nvg.scale(toggleX, toggleY, toggleWidth, toggleHeight, addon.getAnimation().getValue());
            nvg.drawGradientRoundedRect(toggleX, toggleY, toggleWidth, toggleHeight, 12F, toggleActiveStart, toggleActiveEnd);
            nvg.restore();

            String toggleLabel = addon.isToggled() ? TranslateText.STATUS_ENABLED.getText() : TranslateText.STATUS_DISABLED.getText();
            nvg.drawCenteredText(toggleLabel, toggleX + toggleWidth / 2F, toggleY + toggleHeight / 2F, palette.getFontColor(ColorType.DARK), 8.5F, Fonts.MEDIUM);

            ArrayList<Setting> settings = addonManager.getSettingByAddon(addon);
            if (settings != null && !settings.isEmpty()) {
                float settingsSize = 20F;
                float settingsX = cardX + cardWidth - settingsSize - 16F;
                float settingsY = cardY + 14F;

                Color settingsBg = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered ? 208 : 176);
                nvg.drawRoundedRect(settingsX, settingsY, settingsSize, settingsSize, 6F, settingsBg);
                nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + settingsSize / 2F, settingsY + settingsSize / 2F, palette.getFontColor(ColorType.DARK), 10F, Fonts.LEGACYICON);
            }
        }

        nvg.restore();
        nvg.drawVerticalGradientRect(getX() + 15, this.getY(), getWidth() - 30, 12, palette.getBackgroundColor(ColorType.NORMAL), noColour); //top
        nvg.drawVerticalGradientRect(getX() + 15, this.getY() + this.getHeight() - 12, getWidth() - 30, 12, noColour, palette.getBackgroundColor(ColorType.NORMAL)); // bottom
        nvg.restore();


        nvg.save();
        nvg.translate((float) (settingAnimation.getValue() * 600), 0);

        if (currentAddon != null) {

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
            //nvg.drawRoundedRect(headerX + 1F, headerY + 1F, headerWidth - 2F, headerHeight - 2F, 9F,ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));
            nvg.drawText(LegacyIcon.CHEVRON_LEFT, headerX + 10, headerY + 8, palette.getFontColor(ColorType.DARK), 13, Fonts.LEGACYICON);
            nvg.drawText(currentAddon.getName(), headerX + 27, headerY + 9, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
            nvg.drawText(LegacyIcon.REFRESH, headerX + headerWidth - 24, headerY + 7.5F, palette.getFontColor(ColorType.DARK), 13, Fonts.LEGACYICON);

            float contentX = this.getX() + 25;
            float contentY = headerY + 32;
            float contentWidth = this.getWidth() - 50;
            float viewportHeight2 = headerHeight - 47;

            nvg.save();
            nvg.scissor(headerX + 5, contentY - 5, headerWidth - 10, viewportHeight2 + 10);
            settingsPanel.draw(mouseX, mouseY, partialTicks, contentX, contentY, contentWidth, viewportHeight2, nvg, palette, settingScroll);
            nvg.restore();
        }

        nvg.restore();

        int scrollMax = 0;

        if (!visibleAddons.isEmpty()) {
            float totalRows = (float) Math.ceil(visibleAddons.size() / 2.0);
            float contentHeight = totalRows * CARD_HEIGHT + Math.max(0, totalRows - 1) * CARD_ROW_GAP;
            scrollMax = (int) Math.max(0, contentHeight - viewportHeight);
        }

        scroll.setMaxScroll(scrollMax);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        AddonManager addonManager = instance.getAddonManager();

        float scrollValue = scroll.getValue();
        float contentStartY = this.getY() + 52F;
        float cardWidth = ((this.getWidth() - (CARD_HORIZONTAL_PADDING * 2) - CARD_COLUMN_GAP) / 2F);

        if (!openSetting) {
            float chipX = this.getX() + CARD_HORIZONTAL_PADDING;
            float chipY = this.getY() + 16F;
            for (AddonType t : AddonType.values()) {

                float textWidth = nvg.getTextWidth(t.getName(), 9.5F, Fonts.MEDIUM);
                float chipWidth = textWidth + (CHIP_HORIZONTAL_PADDING * 2);

                if (MouseUtils.isInside(mouseX, mouseY, chipX, chipY - 4F, chipWidth, TYPE_CHIP_HEIGHT) && mouseButton == 0) {
                    currentType = t;
                    scroll.reset();
                    scroll.onAnimation();
                }

                chipX += chipWidth + TYPE_CHIP_GAP;
            }

            ArrayList<Addon> visibleAddons = collectVisibleAddons(addonManager);
            for (int i = 0; i < visibleAddons.size(); i++) {
                Addon addon = visibleAddons.get(i);
                int column = i % 2;
                int row = i / 2;

                float cardX = this.getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP);
                float cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP);

                if (!MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT)) {
                    continue;
                }

                if (mouseButton == 0) {
                    float toggleWidth = 68F;
                    float toggleHeight = 22F;
                    float toggleX = cardX + cardWidth - toggleWidth - 16F;
                    float toggleY = cardY + CARD_HEIGHT - toggleHeight - 18F + scrollValue;

                    if (MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, toggleWidth, toggleHeight)) {
                        addon.toggle();
                        return;
                    }

                    ArrayList<Setting> settings = addonManager.getSettingByAddon(addon);
                    if (settings != null && !settings.isEmpty()) {
                        float settingsSize = 20F;
                        float settingsX = cardX + cardWidth - settingsSize - 16F;
                        float settingsY = cardY + 14F + scrollValue;

                        if (MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, settingsSize, settingsSize)) {

                            settingsPanel.buildEntries(settings);
                            settingScroll.resetAll();
                            currentAddon = addon;
                            openSetting = true;
                            this.setCanClose(false);
                            return;
                        }
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

            float headerX = this.getX() + 15;
            float headerY = this.getY() + 15;
            float headerWidth = this.getWidth() - 30;
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
        if (currentAddon != null) {
            settingsPanel.setLayoutMode(InternalSettingsMod.getInstance().getSettingsLayoutMode());
            settingsPanel.mouseReleased(mouseX, mouseY, mouseButton, settingScroll);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (currentAddon != null) {
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

    private boolean filterAddon(Addon a) {

        if (!currentType.equals(AddonType.ALL) && !a.getType().equals(currentType)) {
            return true;
        }

        return !this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimilar(Shindo.getInstance().getAddonManager().getWords(a), this.getSearchBox().getText());
    }

    private ArrayList<Addon> collectVisibleAddons(AddonManager addonManager) {
        ArrayList<Addon> visible = new ArrayList<Addon>();
        for (Addon addon : addonManager.getAddons()) {
            if (!filterAddon(addon)) {
                visible.add(addon);
            }
        }
        return visible;
    }

}
