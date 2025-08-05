package me.miki.shindo.gui.mainmenu;

import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class MainMenuScene {

    private final GuiShindoMainMenu parent;
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

    public GuiShindoMainMenu getParent() {
        return parent;
    }

    public void setCurrentScene(MainMenuScene scene) {
        parent.setCurrentScene(scene);
    }

    public Color getBackgroundColor() {
        return parent.getBackgroundColor();
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public MainMenuScene getSceneByClass(Class<? extends MainMenuScene> clazz) {
        return parent.getSceneByClass(clazz);
    }

    public void setErrorMessage(String errorMessage) {
        parent.setErrorMessage(errorMessage);
    }
}
