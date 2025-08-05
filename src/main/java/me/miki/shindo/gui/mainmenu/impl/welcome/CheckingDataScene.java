package me.miki.shindo.gui.mainmenu.impl.welcome;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class CheckingDataScene extends MainMenuScene {

    private Animation fadeAnimation;

    public CheckingDataScene(GuiShindoMainMenu parent) {
        super(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        String message = "Checking the data...";

        BlurUtils.drawBlurScreen(14);

        if (fadeAnimation == null && this.getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = new DecelerateAnimation(800, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
        }

        if (fadeAnimation != null) {

            nvg.setupAndDraw(() -> {
                nvg.drawCenteredText(message, sr.getScaledWidth() / 2F,
                        (sr.getScaledHeight() / 2F) - (nvg.getTextHeight(message, 26, Fonts.REGULAR) / 2),
                        new Color(255, 255, 255, (int) (fadeAnimation.getValueFloat() * 255)), 26, Fonts.REGULAR);

                float progress = Shindo.getInstance().getDownloadManager().getProgress();
                float widthBar = 300;
                float heightBar = 20;
                float x = sr.getScaledWidth() / 2F - widthBar / 2F;
                float y = sr.getScaledHeight() / 2F + 35;

                nvg.drawRoundedRect(x, y, widthBar, heightBar, 6, new Color(50, 50, 50, 120));
                nvg.drawRoundedRect(x, y, widthBar * progress, heightBar, 6, new Color(0, 160, 255, 200));

                String percentageText = String.format("%.0f%%", progress * 100f);
                nvg.drawCenteredText(percentageText, sr.getScaledWidth() / 2F, y + heightBar / 2F - 8,
                        new Color(255, 255, 255, 200), 16, Fonts.REGULAR);
            });

            if (Shindo.getInstance().getDownloadManager().isDownloaded() && fadeAnimation.getDirection().equals(Direction.FORWARDS)) {
                fadeAnimation.setDirection(Direction.BACKWARDS);
            }

            if (fadeAnimation.isDone(Direction.BACKWARDS)) {
                this.setCurrentScene(this.getSceneByClass(LastMessageScene.class));
            }
        }
    }
}