package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene;
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.AppearanceScene;
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.GeneralScene;
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.LanguageScene;
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.LayoutScene;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class SettingCategory extends Category {

    private final ArrayList<SettingScene> scenes = new ArrayList<SettingScene>();
    private Animation sceneAnimation;
    private SettingScene currentScene;

    public SettingCategory(GuiModMenu parent) {
        super(parent, TranslateText.SETTINGS, LegacyIcon.SETTINGS, false, false);

        scenes.add(new AppearanceScene(this));
        scenes.add(new LanguageScene(this));
        scenes.add(new GeneralScene(this));
        scenes.add(new LayoutScene(this));
    }

    @Override
    public void initGui() {
        sceneAnimation = new SmoothStepAnimation(260, 1.0);
        sceneAnimation.setValue(1.0);

        for (SettingScene scene : scenes) {
            scene.initGui();
        }
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
        sceneAnimation = new SmoothStepAnimation(260, 1.0);
        sceneAnimation.setValue(1.0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorPalette palette = instance.getColorManager().getPalette();
        AccentColor accent = instance.getColorManager().getCurrentColor();

        final float cardHeight = 52F;
        final float cardSpacing = 10F;
        float offsetY = 15;
        float scrollValue = scroll.getValue();

        if (sceneAnimation.isDone(Direction.FORWARDS)) {
            this.setCanClose(true);
            currentScene = null;
        }

        if (currentScene == null && MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
            scroll.onScroll();
            scroll.onAnimation();
        }

        nvg.save();
        nvg.translate((float) -(600 - (sceneAnimation.getValue() * 600)), 0);
        nvg.save();
        nvg.translate(0, scrollValue);

        for (SettingScene scene : scenes) {

            float cardX = this.getX() + 18;
            float cardY = this.getY() + offsetY;
            float cardWidth = this.getWidth() - 36;
            boolean isActive = currentScene == scene && !sceneAnimation.isDone(Direction.FORWARDS);
            boolean hovered = currentScene == null && MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, cardHeight);

            Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), isActive ? 220 : 180);
            Color overlayStart = ColorUtils.applyAlpha(accent.getColor1(), hovered || isActive ? 70 : 35);
            Color overlayEnd = ColorUtils.applyAlpha(accent.getColor2(), hovered || isActive ? 70 : 35);

            nvg.drawShadow(cardX, cardY, cardWidth, cardHeight, 10, 5);
            nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 10, base);
            nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, cardHeight, 10, overlayStart, overlayEnd);

            float iconSize = 28F;
            float iconX = cardX + 18;
            float iconY = cardY + (cardHeight - iconSize) / 2F;

            nvg.drawGradientRoundedRect(iconX, iconY, iconSize, iconSize, 8,
                    ColorUtils.applyAlpha(accent.getColor1(), 160),
                    ColorUtils.applyAlpha(accent.getColor2(), 160));

            nvg.drawCenteredText(scene.getIcon(), iconX + (iconSize / 2F) - 1, iconY + (iconSize / 2F) - 8, Color.WHITE, 18, Fonts.LEGACYICON);

            float textStartX = iconX + iconSize + 14;
            float textWidth = cardWidth - (textStartX - cardX) - 34;
            String displayName = nvg.getLimitText(scene.getName(), 11.5F, Fonts.MEDIUM, textWidth);
            String displayDescription = nvg.getLimitText(scene.getDescription(), 8.5F, Fonts.REGULAR, textWidth);

            nvg.drawText(displayName, textStartX, cardY + 16, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.MEDIUM);
            nvg.drawText(displayDescription, textStartX, cardY + 30, palette.getFontColor(ColorType.NORMAL), 8.5F, Fonts.REGULAR);

            nvg.drawCenteredText(LegacyIcon.CHEVRON_RIGHT, cardX + cardWidth - 22, cardY + (cardHeight / 2F) - (nvg.getTextHeight(LegacyIcon.CHEVRON_RIGHT, 12, Fonts.LEGACYICON) / 2), palette.getFontColor(ColorType.NORMAL), 12, Fonts.LEGACYICON);

            offsetY += cardHeight + cardSpacing;
        }

        nvg.restore();
        nvg.restore();

        nvg.save();
        nvg.translate((float) (sceneAnimation.getValue() * 600), 0);

        if (currentScene != null) {
            currentScene.drawScreen(mouseX, mouseY, partialTicks);

            float headerX = this.getX() + 18F;
            float headerY = this.getY() + 14F;
            float iconSize = 34F;
            float iconRadius = 10F;
            float textX = headerX + iconSize + 16F;
            float textWidth = this.getWidth() - (textX - this.getX()) - 24F;

            Color iconStart = ColorUtils.applyAlpha(accent.getColor1(), 200);
            Color iconEnd = ColorUtils.applyAlpha(accent.getColor2(), 200);
            Color titleColor = palette.getFontColor(ColorType.DARK);
            Color subtitleColor = palette.getFontColor(ColorType.NORMAL);

            nvg.drawGradientRoundedRect(headerX, headerY, iconSize, iconSize, iconRadius, iconStart, iconEnd);
            nvg.drawCenteredText(currentScene.getIcon(), headerX + (iconSize / 2F) - 1, headerY + (iconSize / 2F) - 10F, Color.WHITE, 22F, Fonts.LEGACYICON);

            String title = nvg.getLimitText(currentScene.getName(), 13.5F, Fonts.MEDIUM, textWidth);
            nvg.drawText(title, textX, headerY + 7F, titleColor, 13.5F, Fonts.MEDIUM);

            String description = currentScene.getDescription();
            if (description != null && !"null".equalsIgnoreCase(description)) {
                String clippedDescription = nvg.getLimitText(description, 9F, Fonts.REGULAR, textWidth);
                nvg.drawText(clippedDescription, textX, headerY + 24F, subtitleColor, 9F, Fonts.REGULAR);
            }
        }

        nvg.restore();

        if (currentScene == null) {
            float contentHeight = 15 + scenes.size() * cardHeight + Math.max(0, scenes.size() - 1) * cardSpacing;
            float viewportHeight = getHeight() - 30F;
            scroll.setMaxScroll(Math.max(0, contentHeight - viewportHeight));
        } else {
            scroll.setMaxScroll(0);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        final float cardHeight = 52F;
        final float cardSpacing = 10F;
        float offsetY = 15;
        float scrollValue = scroll.getValue();

        for (SettingScene scene : scenes) {

            float cardX = this.getX() + 18;
            float cardY = this.getY() + offsetY + scrollValue;
            float cardWidth = this.getWidth() - 36;
            if (MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight) && mouseButton == 0 && currentScene == null) {
                currentScene = scene;
                this.setCanClose(false);
                sceneAnimation.setDirection(Direction.BACKWARDS);
                break;
            }

            offsetY += cardHeight + cardSpacing;
        }

        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (!MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight()) && mouseButton == 0) {
            sceneAnimation.setDirection(Direction.FORWARDS);
        }

        if (currentScene != null && mouseButton == 3) {
            sceneAnimation.setDirection(Direction.FORWARDS);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        if (currentScene != null && keyCode == Keyboard.KEY_ESCAPE) {
            sceneAnimation.setDirection(Direction.FORWARDS);
        }
        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene.keyTyped(typedChar, keyCode);
        }
    }

    public Scroll getScroll() {
        return scroll;
    }

    public int getSceneX() {
        return getX() + 15;
    }

    public int getSceneY() {
        return getY() + 15;
    }

    public int getSceneWidth() {
        return getWidth() - 30;
    }

    public int getSceneHeight() {
        return getHeight() - 30;
    }
}
