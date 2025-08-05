package me.miki.shindo.gui.modmenu.category.impl.game;

import eu.shoroa.contrib.render.ShBlur;
import me.miki.shindo.gui.modmenu.category.impl.GamesCategory;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;

import java.awt.*;

public class GameScene {

    private final GamesCategory parent;
    private final String icon;
    private final String name;
    private final String description;

    public GameScene(GamesCategory parent, String name, String description, String icon) {
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public static float anim(float prevVal, float finalVal, float rate, float dt) {
        if (prevVal == finalVal || rate == 0) {
            return finalVal;
        }
        float difference = finalVal - prevVal;
        float nextValue = prevVal + (difference * (dt * rate));
        return finalVal > prevVal
                ? Math.min(nextValue, finalVal)
                : Math.max(nextValue, finalVal);
    }

    public void initGui() {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void keyTyped(char typedChar, int keyCode) {
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
