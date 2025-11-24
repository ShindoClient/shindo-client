package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer;
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip;
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
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddonCategory extends Category {

    private final Scroll settingScroll = new Scroll();
    private final SettingsPanel settingsPanel = new SettingsPanel();
    Color noColour = new Color(0, 0, 0, 0);
    private static final float TYPE_CHIP_GAP = 8F;
    private static final float CHIP_HORIZONTAL_PADDING = 12F;
    private static final float CARD_HORIZONTAL_PADDING = 18F;
    private static final float CARD_COLUMN_GAP = 16F;
    private static final float CARD_ROW_GAP = 16F;
    private static final float CARD_HEIGHT = 122F;
    private static final float CARD_RADIUS = 14F;
    private static final float TOGGLE_WIDTH = 58F;
    private static final float TOGGLE_HEIGHT = 26F;
    private static final float SETTINGS_BUTTON_SIZE = 24F;
    private final Map<Addon, CardLayout> cardLayouts = new HashMap<>();
    private final ArrayList<FilterChip> typeChips = new ArrayList<>();
    private AddonType currentType;
    private boolean openSetting;
    private Animation settingAnimation;
    private Addon currentAddon;


    public AddonCategory(GuiModMenu parent) {
        super(parent, TranslateText.ADDONS, LegacyIcon.LAYOUT_2, true, true);

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
        drawTypeChips(nvg, palette, accentColor, scrollValue, mouseX, mouseY);

        cardLayouts.clear();

        for (int i = 0; i < visibleAddons.size(); i++) {
            Addon addon = visibleAddons.get(i);
            int column = i % 2;
            int row = i / 2;

            float cardX = this.getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP);
            float cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP);

            if (cardY + scrollValue > this.getY() + this.getHeight() || cardY + scrollValue + CARD_HEIGHT < this.getY()) {
                continue;
            }

            ArrayList<Setting> settings = addonManager.getSettingByAddon(addon);
            boolean hasSettings = settings != null && !settings.isEmpty();

            CardLayout layout = new CardLayout();
            layout.cardX = cardX;
            layout.cardY = cardY + scrollValue;
            layout.cardWidth = cardWidth;
            layout.cardHeight = CARD_HEIGHT;
            layout.toggleX = cardX + cardWidth - TOGGLE_WIDTH - 18F;
            layout.toggleY = cardY + CARD_HEIGHT - TOGGLE_HEIGHT - 18F + scrollValue;
            layout.toggleWidth = TOGGLE_WIDTH;
            layout.toggleHeight = TOGGLE_HEIGHT;
            if (hasSettings) {
                layout.hasSettings = true;
                layout.settingsSize = SETTINGS_BUTTON_SIZE;
                layout.settingsX = cardX + cardWidth - SETTINGS_BUTTON_SIZE - 18F;
                layout.settingsY = cardY + 12F + scrollValue;
            }
            cardLayouts.put(addon, layout);

            boolean hovered = !openSetting && layout.contains(mouseX, mouseY);
            drawAddonCard(nvg, palette, accentColor, addon, cardX, cardY, cardWidth, hovered, hasSettings, mouseX, mouseY);
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
        AddonManager addonManager = instance.getAddonManager();

        if (!openSetting && mouseButton == 0) {
            for (FilterChip chip : typeChips) {
                if (chip.contains(mouseX, mouseY)) {
                    chip.click();
                    return;
                }
            }
        }

        if (!openSetting && mouseButton == 0) {
            ArrayList<Addon> visibleAddons = collectVisibleAddons(addonManager);
            for (Addon addon : visibleAddons) {
                CardLayout layout = cardLayouts.get(addon);
                if (layout == null || !layout.contains(mouseX, mouseY)) {
                    continue;
                }

                if (layout.insideToggle(mouseX, mouseY)) {
                    addon.toggle();
                    return;
                }

                ArrayList<Setting> settings = addonManager.getSettingByAddon(addon);
                if (settings != null && !settings.isEmpty() && layout.insideSettings(mouseX, mouseY)) {
                    settingsPanel.buildEntries(settings);
                    settingScroll.resetAll();
                    currentAddon = addon;
                    openSetting = true;
                    this.setCanClose(false);
                    return;
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

    private void drawAddonCard(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, Addon addon, float cardX, float cardY, float cardWidth, boolean hovered, boolean hasSettings, int mouseX, int mouseY) {

        addon.getAnimation().setAnimation(addon.isToggled() ? 1.0F : 0.0F, 16);

        Color cardBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered ? 236 : 206);
        Color overlayStart = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (addon.getAnimation().getValue() * 70));
        Color overlayEnd = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (addon.getAnimation().getValue() * 70));

        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, CARD_RADIUS, cardBase);
        if (addon.getAnimation().getValue() > 0F) {
            nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, CARD_RADIUS, overlayStart, overlayEnd);
        }

        float padding = 18F;
        float titleX = cardX + padding;
        float titleY = cardY + padding;
        float textWidth = cardWidth - (padding * 2);

        nvg.drawText(addon.getName(), titleX, titleY, palette.getFontColor(ColorType.DARK), 12F, Fonts.SEMIBOLD);

        String description = addon.getDescription() == null ? "" : addon.getDescription();
        String wrapped = nvg.getLimitText(description, 8.6F, Fonts.REGULAR, textWidth);
        nvg.drawText(wrapped, titleX, titleY + 20F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 8.6F, Fonts.REGULAR);

        String typeName = addon.getType().getName();
        float chipWidth = Math.max(48F, nvg.getTextWidth(typeName, 8F, Fonts.MEDIUM) + 18F);
        float chipX = cardX + cardWidth - chipWidth - 18F;
        if (hasSettings) {
            chipX -= SETTINGS_BUTTON_SIZE + 10F;
        }
        chipX = Math.max(chipX, titleX);
        //float chipY = cardY + padding - 4F;
        //Color chipBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 225);
        //nvg.drawRoundedRect(chipX, chipY, chipWidth, 18F, 9F, chipBase);
       // nvg.drawCenteredText(typeName, chipX + chipWidth / 2F, chipY + 9F, palette.getFontColor(ColorType.DARK), 8F, Fonts.MEDIUM);

        if (hasSettings) {
            float settingsX = cardX + cardWidth - SETTINGS_BUTTON_SIZE - 18F;
            float settingsY = cardY + padding - 6F;

            Color settingsBg = palette.getBackgroundColor(ColorType.DARK);

            nvg.drawRoundedRect(settingsX, settingsY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, 8F, settingsBg);
            nvg.drawCenteredText(LegacyIcon.SETTINGS, settingsX + (SETTINGS_BUTTON_SIZE / 2F) - 1F, settingsY + 5F, palette.getFontColor(ColorType.DARK), 14F, Fonts.LEGACYICON);

            if (MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE)) {
                nvg.drawGradientOutlineRoundedRect(settingsX, settingsY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, 8F, 2F, accentColor.getColor1(), accentColor.getColor2());
            }
        }

        //float statusY = cardY + CARD_HEIGHT - 34F;
        //Color statusColor = addon.isToggled() ? new Color(96, 209, 153) : new Color(216, 118, 118);
        //nvg.drawCircle(titleX, statusY + 3F, 3.5F, statusColor);
        //String statusText = addon.isToggled() ? TranslateText.STATUS_ENABLED.getText() : TranslateText.STATUS_DISABLED.getText();
        //nvg.drawText(statusText, titleX + 10F, statusY - 4F, palette.getFontColor(ColorType.DARK), 8.5F, Fonts.MEDIUM);

        float toggleX = cardX + cardWidth - TOGGLE_WIDTH - 18F;
        float toggleY = cardY + CARD_HEIGHT - TOGGLE_HEIGHT - 18F;
        float toggleRadius = TOGGLE_HEIGHT / 2F;

        Color toggleBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210);
        nvg.drawRoundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleRadius, toggleBase);

        if (addon.getAnimation().getValue() > 0F) {
            nvg.drawGradientRoundedRect(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, toggleRadius,
                    ColorUtils.applyAlpha(accentColor.getColor1(), (int) (addon.getAnimation().getValue() * 255)),
                    ColorUtils.applyAlpha(accentColor.getColor2(), (int) (addon.getAnimation().getValue() * 255)));
        }

        float knobSize = TOGGLE_HEIGHT - 8F;
        float knobX = toggleX + 4F + addon.getAnimation().getValue() * (TOGGLE_WIDTH - knobSize - 8F);
        float knobY = toggleY + 4F;
        nvg.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2F, Color.WHITE);
    }

    private void drawTypeChips(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, float scrollOffset, int mouseX, int mouseY) {

        typeChips.clear();

        float startX = this.getX() + CARD_HORIZONTAL_PADDING;
        float maxX = this.getX() + this.getWidth() - CARD_HORIZONTAL_PADDING;
        float currentX = startX;
        float currentY = this.getY() + 12F;

        for (AddonType type : AddonType.values()) {
            String label = type.getName();
            float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null);

            if (currentX + chipWidth > maxX) {
                currentX = startX;
                currentY += CategoryChipRenderer.CHIP_HEIGHT + TYPE_CHIP_GAP;
            }

            boolean active = type.equals(currentType);
            boolean hovered = !openSetting && MouseUtils.isInside(mouseX, mouseY, currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);

            CategoryChipRenderer.drawChip(nvg, palette, accentColor, currentX, currentY, chipWidth, label, null, active, hovered);

            FilterChip chip = new FilterChip(() -> {
                if (currentType != type) {
                    currentType = type;
                    scroll.resetAll();
                }
            });
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            typeChips.add(chip);

            currentX += chipWidth + TYPE_CHIP_GAP;
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

    private static class CardLayout {
        float cardX;
        float cardY;
        float cardWidth;
        float cardHeight;
        float toggleX;
        float toggleY;
        float toggleWidth;
        float toggleHeight;
        float settingsX;
        float settingsY;
        float settingsSize;
        boolean hasSettings;

        boolean contains(int mouseX, int mouseY) {
            return MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight);
        }

        boolean insideToggle(int mouseX, int mouseY) {
            return MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, toggleWidth, toggleHeight);
        }

        boolean insideSettings(int mouseX, int mouseY) {
            return hasSettings && MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, settingsSize, settingsSize);
        }
    }

}
