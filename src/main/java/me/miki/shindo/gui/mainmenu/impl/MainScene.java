package me.miki.shindo.gui.mainmenu.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.ShindoAPI;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class MainScene extends MainMenuScene {

    private final SimpleAnimation singlePlayerAnimation = new SimpleAnimation();
    private final SimpleAnimation multiplayerAnimation = new SimpleAnimation();
    private final SimpleAnimation optionsAnimation = new SimpleAnimation();
    boolean isConnected = false;

    public MainScene(GuiShindoMainMenu parent) {
        super(parent);
    }

    @Override
    public void initGui() {
        if (!isConnected) {
            if (Shindo.getInstance().getAccountManager().getCurrentAccount() != null && mc.getSession().getProfile().getId() != null) {
                ShindoAPI api = Shindo.getInstance().getShindoAPI();
                api.start();
                Shindo.getInstance().getNotificationManager().post("[API]", "Is now Connected", NotificationType.INFO);
                isConnected = true;
            }
        }
    }

    /**
     * Renders the current scene of the main menu.
     * <p>
     * This method checks for special application states (such as update needed)
     * and switches the scene accordingly. It then prepares the NanoVG context and delegates
     * the actual drawing to the {@code drawNanoVG} method.
     *
     * @param mouseX       The current X coordinate of the mouse cursor.
     * @param mouseY       The current Y coordinate of the mouse cursor.
     * @param partialTicks The partial tick time (for smooth animations).
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();

        if (instance.getUpdateNeeded()) {
            instance.setUpdateNeeded(false);
            this.setCurrentScene(this.getSceneByClass(UpdateScene.class));
        }
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(nvg, mouseX, mouseY));
    }

    /**
     * Renders the main menu UI using NanoVG, including the logo and interactive buttons.
     * The buttons (Singleplayer, Multiplayer, Settings) animate when hovered by the mouse.
     *
     * @param nvg    The NanoVGManager instance used for rendering.
     * @param mouseX The current X coordinate of the mouse cursor.
     * @param mouseY The current Y coordinate of the mouse cursor.
     */
    private void drawNanoVG(NanoVGManager nvg, int mouseX, int mouseY) {

        ScaledResolution sr = new ScaledResolution(mc);

        float yPos = sr.getScaledHeight() / 2F - 22;

        nvg.drawCenteredText(LegacyIcon.SHINDO, sr.getScaledWidth() / 2F, sr.getScaledHeight() / 2F - (nvg.getTextHeight(LegacyIcon.SHINDO, 54, Fonts.LEGACYICON) / 2) - 60, Color.WHITE, 54, Fonts.LEGACYICON);

        singlePlayerAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2F - (180 / 2F), yPos, 180, 20) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() / 2F - (180 / 2F), yPos, 180, 20, 4.5F, new Color(230 - (int) (singlePlayerAnimation.getValue() * 20), 230 - (int) (singlePlayerAnimation.getValue() * 20), 230 - (int) (singlePlayerAnimation.getValue() * 20), 120));
        nvg.drawCenteredText(TranslateText.SINGLEPLAYER.getText(), sr.getScaledWidth() / 2F, yPos + 6.5F, Color.WHITE, 9.5F, Fonts.REGULAR);

        multiplayerAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2F - (180 / 2F), yPos + 26, 180, 20) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() / 2F - (180 / 2F), yPos + 26, 180, 20, 4.5F, new Color(230 - (int) (multiplayerAnimation.getValue() * 20), 230 - (int) (multiplayerAnimation.getValue() * 20), 230 - (int) (multiplayerAnimation.getValue() * 20), 120));
        nvg.drawCenteredText(TranslateText.MULTIPLAYER.getText(), sr.getScaledWidth() / 2F, yPos + 6.5F + 26, Color.WHITE, 9.5F, Fonts.REGULAR);

        optionsAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2F - (180 / 2F), yPos + (26 * 2), 180, 20) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() / 2F - (180 / 2F), yPos + (26 * 2), 180, 20, 4.5F, new Color(230 - (int) (optionsAnimation.getValue() * 20), 230 - (int) (optionsAnimation.getValue() * 20), 230 - (int) (optionsAnimation.getValue() * 20), 120));
        nvg.drawCenteredText(TranslateText.SETTINGS.getText(), sr.getScaledWidth() / 2F, yPos + 6.5F + (26 * 2), Color.WHITE, 9.5F, Fonts.REGULAR);
    }

    /**
     * Handles mouse click events for the main menu UI.
     * <p>
     * This method checks if the left mouse button was clicked and determines if the click
     * occurred within the bounds any of the main menu buttons (Singleplayer, Multiplayer, Settings).
     * If a button is clicked, it opens the corresponding screen.
     *
     * @param mouseX      The X coordinate of the mouse cursor when the click occurred.
     * @param mouseY      The Y coordinate of the mouse cursor when the click occurred.
     * @param mouseButton The mouse button that was pressed (0 = left click).
     */
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        ScaledResolution sr = new ScaledResolution(mc);

        float yPos = sr.getScaledHeight() / 2F - 22;

        if (mouseButton == 0) {

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2F - (160 / 2F), yPos, 160, 20)) {
                mc.displayGuiScreen(new GuiSelectWorld(this.getParent()));
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2F - (180 / 2F), yPos + 26, 180, 20)) {
                mc.displayGuiScreen(new GuiMultiplayer(getParent()));
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() / 2F - (180 / 2F), yPos + (26 * 2), 180, 20)) {
                mc.displayGuiScreen(new GuiOptions(this.getParent(), mc.gameSettings));
            }
        }
    }
}
