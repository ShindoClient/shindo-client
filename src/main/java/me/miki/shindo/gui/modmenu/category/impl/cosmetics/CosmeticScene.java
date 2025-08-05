package me.miki.shindo.gui.modmenu.category.impl.cosmetics;

import eu.shoroa.contrib.render.ShBlur;
import me.miki.shindo.gui.modmenu.category.impl.CosmeticsCategory;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.animation.ColorAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.awt.*;

public class CosmeticScene {

    private final CosmeticsCategory parent;
    private final String icon;
    private final String name;
    private final String description;

    private final SimpleAnimation backgroundAnimation;
    private final ColorAnimation textColorAnimation;

    public CosmeticScene(CosmeticsCategory parent, String name, String description, String icon) {
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.icon = icon;

        this.backgroundAnimation = new SimpleAnimation();
        this.textColorAnimation = new ColorAnimation();
    }

    public void initGui() {
    }

    public void initScene() {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void keyTyped(char typedChar, int keyCode) {
    }

    public void handleInput() {
    }

    public void onGuiClosed() {
    }

    public void onSceneClosed() {
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getX() {
        return parent.getSceneX();
    }

    public int getY() {
        return parent.getSceneY();
    }

    public int getWidth() {
        return parent.getSceneWidth();
    }

    public int getHeight() {
        return parent.getSceneHeight();
    }

    public CosmeticsCategory getParent() {
        return parent;
    }

    public CosmeticScene getSceneByClass(Class<? extends CosmeticScene> clazz) {
        return parent.getSceneByClass(clazz);
    }

    public SimpleAnimation getBackgroundAnimation() {
        return backgroundAnimation;
    }

    public ColorAnimation getTextColorAnimation() {
        return textColorAnimation;
    }

    public void drawBackground(NanoVGManager nvg, ColorPalette palette) {
        if (InternalSettingsMod.getInstance().getBlurSetting().isToggled()) {
            ShBlur.getInstance().drawBlur(() -> nvg.drawRect(getX(), getY(), getWidth(), getHeight(), palette.getBackgroundColor(ColorType.DARK)));
            Color colsidebar = palette.getBackgroundColor(ColorType.DARK);
            nvg.drawRect(getX(), getY(), getWidth(), getHeight(), new Color(colsidebar.getRed(), colsidebar.getGreen(), colsidebar.getBlue(), 210));
        } else {
            nvg.drawRect(getX(), getY(), getWidth(), getHeight(), palette.getBackgroundColor(ColorType.DARK));
        }
    }
}