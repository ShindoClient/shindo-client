package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer;
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.ModManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.profile.Profile;
import me.miki.shindo.management.profile.ProfileIcon;
import me.miki.shindo.management.profile.ProfileManager;
import me.miki.shindo.management.profile.ProfileType;
import me.miki.shindo.ui.comp.impl.field.CompTextBox;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.SearchUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.file.FileUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileCategory extends Category {

    private static final float CARD_HORIZONTAL_PADDING = 18F;
    private static final float CARD_COLUMN_GAP = 18F;
    private static final float CARD_ROW_GAP = 14F;
    private static final float CARD_HEIGHT = 94F;
    private static final float ICON_SIZE = 44F;
    private static final float CHIP_GAP = 8F;

    private final CompTextBox nameBox = new CompTextBox();
    private final CompTextBox serverIpBox = new CompTextBox();
    private final ArrayList<FilterChip> typeChips = new ArrayList<FilterChip>();
    private ProfileType currentType;
    private Animation profileAnimation;
    private boolean openProfile;
    private ProfileIcon currentIcon;
    private boolean useCustomIcon;
    private File selectedCustomIcon;
    private float gridStartY;

    public ProfileCategory(GuiModMenu parent) {
        super(parent, TranslateText.PROFILE, LegacyIcon.EDIT, true, true);
    }

    @Override
    public void initGui() {
        currentType = ProfileType.ALL;
        currentIcon = ProfileIcon.COMMAND;
        openProfile = false;
        profileAnimation = new SmoothStepAnimation(260, 1.0);
        profileAnimation.setValue(1.0);
        useCustomIcon = false;
        selectedCustomIcon = null;
        gridStartY = 0F;
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
        openProfile = false;
        profileAnimation = new SmoothStepAnimation(260, 1.0);
        profileAnimation.setValue(1.0);
        useCustomIcon = false;
        selectedCustomIcon = null;
        gridStartY = 0F;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ProfileManager profileManager = instance.getProfileManager();
        Profile activeProfile = profileManager.getActiveProfile();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        profileAnimation.setDirection(openProfile ? Direction.BACKWARDS : Direction.FORWARDS);

        if (profileAnimation.isDone(Direction.FORWARDS)) {
            nameBox.setText("");
            serverIpBox.setText("");
            this.setCanClose(true);
        }

        ArrayList<Profile> visibleProfiles = collectVisibleProfiles(profileManager);



        float scrollValue = scroll.getValue();


        nvg.save();
        nvg.translate((float) -(600 - (profileAnimation.getValue() * 600)), 0);

        float chipBlockBottom = drawTypeChips(nvg, palette, accentColor, mouseX, mouseY);
        float contentStartY = chipBlockBottom + 24F;
        this.gridStartY = contentStartY;
        float cardWidth = ((this.getWidth() - (CARD_HORIZONTAL_PADDING * 2) - CARD_COLUMN_GAP) / 2F);
        float viewportHeight = this.getHeight() - (contentStartY - this.getY()) - 28F;



        if (!openProfile && MouseUtils.isInside(mouseX, mouseY, this.getX(), contentStartY - 6F, this.getWidth(), this.getHeight() - (contentStartY - this.getY()) + 6F)) {
            scroll.onScroll();
            scroll.onAnimation();
        }

        nvg.save();
        nvg.intersectScissor(this.getX(), contentStartY - 6F, this.getWidth(), this.getHeight() - (contentStartY - this.getY()) + 6F);
        nvg.translate(0, scrollValue);

        for (int i = 0; i < visibleProfiles.size(); i++) {

            Profile profile = visibleProfiles.get(i);
            boolean isCreateCard = profile.getId() == 999;
            boolean isDefault = profile.getId() == -1;
            boolean isActive = activeProfile != null && activeProfile.equals(profile);
            if (!isActive && activeProfile != null && activeProfile.getJsonFile() != null && profile.getJsonFile() != null) {
                isActive = activeProfile.getJsonFile().equals(profile.getJsonFile());
            }

            int column = i % 2;
            int row = i / 2;

            float cardX = this.getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP);
            float cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP);

            if (cardY + scrollValue > this.getY() + this.getHeight() || cardY + scrollValue + CARD_HEIGHT < this.getY()) {
                continue;
            }

            boolean hovered = !openProfile && MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT);

            Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), hovered ? 220 : 190);
            Color overlayStart = ColorUtils.applyAlpha(accentColor.getColor1(), (isActive ? 140 : hovered ? 70 : 35));
            Color overlayEnd = ColorUtils.applyAlpha(accentColor.getColor2(), (isActive ? 140 : hovered ? 70 : 35));

            nvg.drawShadow(cardX, cardY, cardWidth, CARD_HEIGHT, 10, 6);
            nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12F, base);
            nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12F, overlayStart, overlayEnd);

            if (isActive) {
                nvg.drawGradientOutlineRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12F, 2.2F, ColorUtils.applyAlpha(accentColor.getColor1(), 225), ColorUtils.applyAlpha(accentColor.getColor2(), 225));
            }

            if (isCreateCard) {

                nvg.drawCenteredText(LegacyIcon.PLUS, cardX + cardWidth / 2F, cardY + CARD_HEIGHT / 2F - 16F, palette.getFontColor(ColorType.DARK), 24F, Fonts.LEGACYICON);
                nvg.drawCenteredText(TranslateText.ADD_PROFILE.getText(), cardX + cardWidth / 2F, cardY + CARD_HEIGHT / 2F + 6F, palette.getFontColor(ColorType.DARK), 9.5F, Fonts.MEDIUM);
                continue;
            }

            float iconX = cardX + 16F;
            float iconY = cardY + (CARD_HEIGHT - ICON_SIZE) / 2F;

            if (profile.getCustomIcon() != null) {
                nvg.drawRoundedImage(profile.getCustomIcon(), iconX, iconY, ICON_SIZE, ICON_SIZE, 9F);
            } else if (profile.getIcon() != null) {
                nvg.drawRoundedImage(profile.getIcon().getIcon(), iconX, iconY, ICON_SIZE, ICON_SIZE, 9F);
            } else {
                nvg.drawRoundedRect(iconX, iconY, ICON_SIZE, ICON_SIZE, 9F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200));
                nvg.drawCenteredText(LegacyIcon.PLUS, iconX + ICON_SIZE / 2F, iconY + ICON_SIZE / 2F, palette.getFontColor(ColorType.DARK), 14F, Fonts.LEGACYICON);
            }

            float textX = iconX + ICON_SIZE + 14F;
            float textWidth = cardWidth - (textX - cardX) - 24F;
            String profileName = profile.getName().isEmpty() ? (isDefault ? "Default" : "Profile") : profile.getName();
            profileName = nvg.getLimitText(profileName, 12F, Fonts.MEDIUM, textWidth);
            nvg.drawText(profileName, textX, cardY + 20F, palette.getFontColor(ColorType.DARK), 12F, Fonts.MEDIUM);

            String serverInfo = profile.getServerIp() == null || profile.getServerIp().isEmpty()
                    ? TranslateText.AUTO_LOAD.getText() + ": " + TranslateText.NONE.getText()
                    : TranslateText.SERVER_IP.getText() + ": " + profile.getServerIp();

            serverInfo = nvg.getLimitText(serverInfo, 8.5F, Fonts.REGULAR, textWidth);
            nvg.drawText(serverInfo, textX, cardY + 36F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220), 8.5F, Fonts.REGULAR);


            if (!isDefault) {
                float starSize = 18F;
                float startX = cardX + cardWidth - starSize - 18F;
                float startY = cardY + 10F;

                profile.getStarAnimation().setAnimation(profile.getType().equals(ProfileType.FAVORITE) ? 1.0F : 0.0F, 16);

                nvg.drawRoundedRect(startX, startY - 1F, starSize, starSize, 5F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190));

                float starY = startY + starSize + 10F;
                nvg.drawRoundedRect(startX, starY - 1F, starSize, starSize, 5F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190));
                nvg.drawCenteredText(LegacyIcon.STAR, startX + starSize / 2F - 0.5F, starY + 3F, palette.getFontColor(ColorType.NORMAL), 10F, Fonts.LEGACYICON);
                nvg.drawCenteredText(LegacyIcon.STAR_FILL, startX + starSize / 2F, starY + 3F, ColorUtils.applyAlpha(palette.getMaterialYellow(), (int) (profile.getStarAnimation().getValue() * 255)), 10F, Fonts.LEGACYICON);

                float deleteY = starY + starSize + 10F;
                nvg.drawRoundedRect(startX, deleteY - 1F, starSize, starSize, 5F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190));
                nvg.drawCenteredText(LegacyIcon.TRASH, startX + starSize / 2F - 0.5F, deleteY + 3F, palette.getMaterialRed(), 10F, Fonts.LEGACYICON);

            } else {
                float checkSize = 18F;
                float checkX = cardX + cardWidth - checkSize - 18F;
                float checkY = cardY + 10F;

                nvg.drawRoundedRect(checkX, checkY - 1F, checkSize, checkSize, 5F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190));
            }

            if (isActive) {
                float checkSize = 18F;
                float checkX = cardX + cardWidth - checkSize - 18F;
                float checkY = cardY + 10F;

                nvg.drawCenteredText(LegacyIcon.CHECK, checkX + checkSize / 2F - 0.5F, checkY + 3F, palette.getFontColor(ColorType.DARK), 10F, Fonts.LEGACYICON);
            }
        }

        nvg.restore();
        nvg.drawVerticalGradientRect(getX() + CARD_HORIZONTAL_PADDING, this.getY() + 48F, getWidth() - (CARD_HORIZONTAL_PADDING * 2), 14F, palette.getBackgroundColor(ColorType.NORMAL), new Color(0, 0, 0, 0));
        nvg.drawVerticalGradientRect(getX() + CARD_HORIZONTAL_PADDING, this.getY() + this.getHeight() - 16F, getWidth() - (CARD_HORIZONTAL_PADDING * 2), 16F, new Color(0, 0, 0, 0), palette.getBackgroundColor(ColorType.NORMAL));
        nvg.restore();

        nvg.save();
        nvg.translate((float) (profileAnimation.getValue() * 600), 0);

        float panelX = this.getX() + 18F;
        float panelY = this.getY() + 15F;
        float panelWidth = this.getWidth() - 36F;
        float panelHeight = this.getHeight() - 30F;

        nvg.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 12F, palette.getBackgroundColor(ColorType.DARK));

        nvg.drawText(TranslateText.ADD_PROFILE.getText(), panelX + 24F, panelY + 20F, palette.getFontColor(ColorType.DARK), 14F, Fonts.SEMIBOLD);
        nvg.drawText(TranslateText.ICON.getText(), panelX + 24F, panelY + 48F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);

        float iconSelectorX = panelX + 24F;
        float iconSelectorY = panelY + 66F;
        float iconSelectorGap = 12F;
        float iconTileSize = 24F;

        for (ProfileIcon icon : ProfileIcon.values()) {

            boolean selected = !useCustomIcon && currentIcon.equals(icon);
            icon.getAnimation().setAnimation(selected ? 1.0F : 0.0F, 12);
            float alpha = icon.getAnimation().getValue();

            Color iconOverlayStart = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (alpha * 200));
            Color iconOverlayEnd = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (alpha * 200));

            nvg.drawRoundedImage(icon.getIcon(), iconSelectorX, iconSelectorY, iconTileSize, iconTileSize, 8F);
            nvg.drawGradientRoundedRect(iconSelectorX, iconSelectorY, iconTileSize, iconTileSize, 8F, iconOverlayStart, iconOverlayEnd);

            iconSelectorX += iconTileSize + iconSelectorGap;
        }

        float customTileX = panelX + panelWidth - iconTileSize - 24F;
        float customTileY = panelY + 66F;

        Color customBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210);
        nvg.drawRoundedRect(customTileX, customTileY, iconTileSize, iconTileSize, 8F, customBase);

        if (selectedCustomIcon != null) {
            nvg.drawRoundedImage(selectedCustomIcon, customTileX, customTileY, iconTileSize, iconTileSize, 8F);
        } else {
            nvg.drawCenteredText(LegacyIcon.PLUS, customTileX + iconTileSize / 2F, customTileY + iconTileSize / 2F - 6, palette.getFontColor(ColorType.DARK), 12F, Fonts.LEGACYICON);
        }

        if (useCustomIcon) {
            nvg.drawGradientOutlineRoundedRect(customTileX, customTileY, iconTileSize, iconTileSize, 8F, 1.6F, ColorUtils.applyAlpha(accentColor.getColor1(), 220), ColorUtils.applyAlpha(accentColor.getColor2(), 220));
        } else {
            nvg.drawOutlineRoundedRect(customTileX, customTileY, iconTileSize, iconTileSize, 8F, 1.2F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 100));
        }

        float fieldStartY = panelY + 130F;
        float fieldWidth = (panelWidth - 48F) / 2F - 15F;

        nvg.drawText(TranslateText.NAME.getText(), panelX + 24F, fieldStartY, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        nameBox.setPosition(panelX + 24F, fieldStartY + 20F, fieldWidth, 20F);
        nameBox.setDefaultText(TranslateText.NAME.getText());
        nameBox.draw(mouseX, mouseY, partialTicks);

        nvg.drawText(TranslateText.SERVER_IP.getText(), panelX + 24F + fieldWidth + 24F, fieldStartY, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        serverIpBox.setPosition(panelX + 24F + fieldWidth + 24F, fieldStartY + 20F, fieldWidth, 20F);
        serverIpBox.setDefaultText(TranslateText.SERVER_IP.getText());
        serverIpBox.draw(mouseX, mouseY, partialTicks);

        float createButtonWidth = 80F;
        float createButtonHeight = 20F;
        float createButtonX = panelX + panelWidth - createButtonWidth - 30F;
        float createButtonY = panelY + panelHeight - createButtonHeight - 20F;

        nvg.drawRoundedRect(createButtonX, createButtonY, createButtonWidth, createButtonHeight, 8F, palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawCenteredText(TranslateText.CREATE.getText(), createButtonX + createButtonWidth / 2F, createButtonY + createButtonHeight / 2F - 4F, palette.getFontColor(ColorType.DARK), 10F, Fonts.REGULAR);

        nvg.restore();

        float totalRows = (float) Math.ceil(visibleProfiles.size() / 2.0);
        float contentHeight = totalRows * CARD_HEIGHT + Math.max(0, totalRows - 1) * CARD_ROW_GAP;
        scroll.setMaxScroll((int) Math.max(0, contentHeight - viewportHeight));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        Shindo instance = Shindo.getInstance();
        ProfileManager profileManager = instance.getProfileManager();
        ModManager modManager = instance.getModManager();
        FileManager fileManager = instance.getFileManager();

        float scrollValue = scroll.getValue();
        float contentStartY = gridStartY > 0 ? gridStartY : this.getY() + 56F;
        float cardWidth = ((this.getWidth() - (CARD_HORIZONTAL_PADDING * 2) - CARD_COLUMN_GAP) / 2F);

        if (openProfile && profileAnimation.isDone(Direction.BACKWARDS)) {

            float panelX = this.getX() + 18F;
            float panelY = this.getY() + 15F;
            float panelWidth = this.getWidth() - 36F;
            float panelHeight = this.getHeight() - 30F;

            float iconSelectorX = panelX + 24F;
            float iconSelectorY = panelY + 66F;
            float iconSelectorGap = 12F;
            float iconTileSize = 24F;

            for (ProfileIcon icon : ProfileIcon.values()) {
                if (MouseUtils.isInside(mouseX, mouseY, iconSelectorX, iconSelectorY, iconTileSize, iconTileSize) && mouseButton == 0) {
                    currentIcon = icon;
                    useCustomIcon = false;
                }
                iconSelectorX += iconTileSize + iconSelectorGap;
            }

            float customTileX = panelX + panelWidth - iconTileSize - 24F;
            float customTileY = panelY + 66F;


            if (MouseUtils.isInside(mouseX, mouseY, customTileX, customTileY, iconTileSize, iconTileSize) && mouseButton == 0) {
                if (selectedCustomIcon != null && !useCustomIcon) {
                    useCustomIcon = true;
                } else {
                    openCustomIconPicker();
                }
            }

            nameBox.mouseClicked(mouseX, mouseY, mouseButton);
            serverIpBox.mouseClicked(mouseX, mouseY, mouseButton);



            float createButtonWidth = 80F;
            float createButtonHeight = 20F;
            float createButtonX = panelX + panelWidth - createButtonWidth - 30F;
            float createButtonY = panelY + panelHeight - createButtonHeight - 20F;

            if (MouseUtils.isInside(mouseX, mouseY, createButtonX, createButtonY, createButtonWidth, createButtonHeight) && mouseButton == 0) {

                if (!nameBox.getText().isEmpty()) {
                    String serverIp = serverIpBox.getText().isEmpty() ? "" : serverIpBox.getText();
                    File profileFile = new File(fileManager.getProfileDir(), nameBox.getText() + ".json");

                    profileManager.save(profileFile, serverIp, ProfileType.ALL, currentIcon, useCustomIcon ? selectedCustomIcon : null);
                    profileManager.loadProfiles(false);

                    openProfile = false;
                    useCustomIcon = false;
                    selectedCustomIcon = null;
                    currentIcon = ProfileIcon.COMMAND;
                }
            }

            if (!MouseUtils.isInside(mouseX, mouseY, panelX - 6F, panelY - 6F, panelWidth + 12F, panelHeight + 12F) && mouseButton == 0) {
                openProfile = false;
                useCustomIcon = false;
                selectedCustomIcon = null;
            }

        } else {

            if (mouseButton == 0) {
                for (FilterChip chip : typeChips) {
                    if (chip.contains(mouseX, mouseY)) {
                        chip.click();
                        return;
                    }
                }
            }

            ArrayList<Profile> visibleProfiles = collectVisibleProfiles(profileManager);
            for (int i = 0; i < visibleProfiles.size(); i++) {
                Profile profile = visibleProfiles.get(i);
                int column = i % 2;
                int row = i / 2;

                float cardX = this.getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP);
                float cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP) + scrollValue;

                if (!MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, CARD_HEIGHT)) {
                    continue;
                }

                if (mouseButton == 0) {
                    if (profile.getId() == 999) {
                        openProfile = true;
                        this.setCanClose(false);
                        profileAnimation.setDirection(Direction.BACKWARDS);
                        return;
                    }

                    boolean isDefault = profile.getId() == -1;
                    float iconSize = 18F;
                    float iconX = cardX + cardWidth - iconSize - 18F;
                    float startY = cardY + 10F;
                    float starY = startY + iconSize + 10F;
                    float deleteY = starY + iconSize + 10F;

                    if (!isDefault && MouseUtils.isInside(mouseX, mouseY, iconX - 0.5F, starY + 3F, iconSize, iconSize)) {
                        if (profile.getType().equals(ProfileType.FAVORITE)) {
                            profile.setType(ProfileType.ALL);
                        } else {
                            profile.setType(ProfileType.FAVORITE);
                        }
                        profileManager.save(profile.getJsonFile(), profile.getServerIp(), profile.getType(), profile.getIcon(), profile.getCustomIcon());
                        return;
                    }

                    if (!isDefault && MouseUtils.isInside(mouseX, mouseY, iconX - 0.5F, deleteY + 3F, iconSize, iconSize)) {
                        profileManager.delete(profile);
                        profileManager.loadProfiles(false);
                        return;
                    }

                    if (profile.getId() != 999) {
                        modManager.disableAll();
                        profileManager.load(profile.getJsonFile());
                    }
                }
            }
        }

        if (mouseButton == 3) {
            openProfile = false;
            selectedCustomIcon = null;
            useCustomIcon = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        if (openProfile) {

            nameBox.keyTyped(typedChar, keyCode);
            serverIpBox.keyTyped(typedChar, keyCode);

            if (keyCode == Keyboard.KEY_ESCAPE) {
                openProfile = false;
                useCustomIcon = false;
                selectedCustomIcon = null;
            }
        } else {
            if (keyCode != 0xD0 && keyCode != 0xC8 && keyCode != Keyboard.KEY_ESCAPE)
                this.getSearchBox().setFocused(true);
        }
    }

    private float drawTypeChips(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, int mouseX, int mouseY) {

        typeChips.clear();

        float startX = this.getX() + CARD_HORIZONTAL_PADDING;
        float maxX = this.getX() + this.getWidth() - CARD_HORIZONTAL_PADDING;
        float currentX = startX;
        float currentY = this.getY() + 16F;
        float blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT;

        for (ProfileType type : ProfileType.values()) {
            String label = type.getName();
            float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null);

            if (currentX + chipWidth > maxX) {
                currentX = startX;
                currentY += CategoryChipRenderer.CHIP_HEIGHT + CHIP_GAP;
                blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT;
            }

            boolean active = type.equals(currentType);
            boolean hovered = !openProfile && MouseUtils.isInside(mouseX, mouseY, currentX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);

            CategoryChipRenderer.drawChip(nvg, palette, accentColor, currentX, currentY, chipWidth, label, null, active, hovered);

            FilterChip chip = new FilterChip(() -> {
                if (currentType != type) {
                    currentType = type;
                    scroll.resetAll();
                }
            });
            chip.setBounds(currentX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            typeChips.add(chip);

            currentX += chipWidth + CHIP_GAP;
        }

        return blockBottom;
    }

    private void openCustomIconPicker() {
        Multithreading.runAsync(() -> {
            FileManager fileManager = Shindo.getInstance().getFileManager();

            File file = FileUtils.selectImageFile();
            File iconDir = fileManager.getProfileIconDir();

            if (file != null && iconDir.exists() && file.exists() && FileUtils.getExtension(file).equals("png")) {
                File destFile = new File(iconDir, file.getName());

                try {
                    FileUtils.copyFile(file, destFile);
                    File previousIcon = selectedCustomIcon;
                    selectedCustomIcon = destFile;
                    useCustomIcon = true;
                    if (previousIcon != null && previousIcon.exists()) {
                        previousIcon.delete();
                    }
                } catch (IOException e) {
                    ShindoLogger.error("Failed to copy custom profile icon", e);
                }
            }
        });
    }

    private ArrayList<Profile> collectVisibleProfiles(ProfileManager profileManager) {
        ArrayList<Profile> visible = new ArrayList<Profile>();
        for (Profile profile : profileManager.getProfiles()) {
            if (profile.getId() == 999) {
                visible.add(profile);
                continue;
            }
            if (!filter(profile)) {
                visible.add(profile);
            }
        }
        return visible;
    }

    private boolean filter(Profile profile) {

        if (currentType.equals(ProfileType.FAVORITE) && !profile.getType().equals(ProfileType.FAVORITE)) {
            return true;
        }

        return !this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimilar(profile.getName(), this.getSearchBox().getText());
    }
}
