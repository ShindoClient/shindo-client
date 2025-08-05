package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.cosmetics.CosmeticScene;
import me.miki.shindo.gui.modmenu.category.impl.cosmetics.impl.CapesScene;
import me.miki.shindo.gui.modmenu.category.impl.cosmetics.impl.WingsScene;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.ModManager;
import me.miki.shindo.management.music.MusicManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;
import java.util.ArrayList;

public class CosmeticsCategory extends Category {

    private final ArrayList<CosmeticScene> scenes = new ArrayList<>();
    private CosmeticScene currentScene;

    public CosmeticsCategory(GuiModMenu parent) {
        super(parent, TranslateText.COSMETICS, LegacyIcon.SHOPPING, true, true);

        scenes.add(new CapesScene(this));
        scenes.add(new WingsScene(this));

        if (currentScene == null) {
            currentScene = getSceneByClass(CapesScene.class);
        }
    }

    @Override
    public void initGui() {
        currentScene.initGui();
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
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

        nvg.save();
        nvg.scissor(getX(), getY(), getWidth(), getHeight());

        for (CosmeticScene scene : scenes) {
            float textWidth = nvg.getTextWidth(scene.getName(), 9, Fonts.MEDIUM);
            boolean isCurrentCategory = scene.equals(currentScene);

            scene.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);

            Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
            Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (scene.getBackgroundAnimation().getValue() * 255));
            Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (scene.getBackgroundAnimation().getValue() * 255));
            Color textColor = scene.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

            nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
            nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);

            nvg.drawText(scene.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);

            offsetX += (int) (textWidth + 28);
        }
        nvg.restore();

        nvg.save();
        nvg.scissor(getSceneX(), getSceneY(), getSceneWidth(), getSceneHeight());

        if (currentScene != null) {
            currentScene.drawScreen(mouseX, mouseY, partialTicks);
        }

        nvg.restore();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        MusicManager musicManager = instance.getMusicManager();

        int offsetX = 0;
        float offsetY = 13;

        for (CosmeticScene scene : scenes) {

            float textWidth = nvg.getTextWidth(scene.getName(), 9, Fonts.MEDIUM);

            if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {

                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
                    currentScene = scene;
                }
            }

            offsetX += (int) (textWidth + 28);
        }


        currentScene.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        currentScene.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        currentScene.keyTyped(typedChar, keyCode);
    }

    public int getSceneX() {
        return getX();
    }

    public int getSceneY() {
        return getY() + 30;
    }

    public int getSceneWidth() {
        return getWidth();
    }

    public int getSceneHeight() {
        return getHeight() - 40;
    }

    public CosmeticScene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(CosmeticScene currentScene) {

        if (this.currentScene != null) {
            this.currentScene.onSceneClosed();
        }

        this.currentScene = currentScene;

        if (this.currentScene != null) {
            this.currentScene.initScene();
        }
    }

    public CosmeticScene getSceneByClass(Class<? extends CosmeticScene> clazz) {

        for (CosmeticScene s : scenes) {
            if (s.getClass().equals(clazz)) {
                return s;
            }
        }

        return null;
    }
}
