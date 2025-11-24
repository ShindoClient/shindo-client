package me.miki.shindo.gui.mainmenu;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;

import java.awt.Color;

public class MainMenuScene {

    private static final ColorPalette FALLBACK_PALETTE = new ColorPalette();
    private static final AccentColor FALLBACK_ACCENT = new AccentColor(
            "Default",
            new Color(170, 255, 169),
            new Color(17, 255, 189)
    );

    @Getter
    private final GuiShindoMainMenu parent;
    @Getter
    private final SimpleAnimation animation = new SimpleAnimation();

    public Minecraft mc = Minecraft.getMinecraft();

    public MainMenuScene(GuiShindoMainMenu parent) {
        this.parent = parent;
    }

    public void initScene() {
    }

    public void initGui() {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void keyTyped(char typedChar, int keyCode) {
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void handleInput() {
    }

    public void onGuiClosed() {
    }

    public void onSceneClosed() {
    }

    public void setCurrentScene(MainMenuScene scene) {
        parent.setCurrentScene(scene);
    }

    public Color getBackgroundColor() {
        return getMenuPalette().getBackgroundColor(ColorType.DARK);
    }

    protected Color getPanelColor() {
        return getMenuPalette().getBackgroundColor(ColorType.MID);
    }

    protected Color getControlColor() {
        return getMenuPalette().getBackgroundColor(ColorType.NORMAL);
    }

    protected ColorPalette getMenuPalette() {
        ColorManager manager = getColorManager();
        return manager != null ? manager.getPalette() : FALLBACK_PALETTE;
    }

    protected AccentColor getMenuAccent() {
        ColorManager manager = getColorManager();
        AccentColor accent = manager != null ? manager.getCurrentColor() : null;
        return accent != null ? accent : FALLBACK_ACCENT;
    }

    private ColorManager getColorManager() {
        Shindo instance = Shindo.getInstance();
        return instance != null ? instance.getColorManager() : null;
    }

    public MainMenuScene getSceneByClass(Class<? extends MainMenuScene> clazz) {
        return parent.getSceneByClass(clazz);
    }
}
