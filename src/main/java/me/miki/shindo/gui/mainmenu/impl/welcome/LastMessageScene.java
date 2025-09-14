package me.miki.shindo.gui.mainmenu.impl.welcome;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.gui.mainmenu.impl.MainScene;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.Sound;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class LastMessageScene extends MainMenuScene {

    private final Animation blurAnimation;
    private final TimerUtils timer = new TimerUtils();
    private Animation fadeAnimation;
    private int step;
    private String message;
    private boolean soundPlayed = false;

    public LastMessageScene(GuiShindoMainMenu parent) {
        super(parent);
        step = 0;
        blurAnimation = new DecelerateAnimation(800, 13);
        blurAnimation.setValue(13);
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        String compMessage = "Setup is complete!";
        String welcomeMessage = "Thank you for choosing Shindo Client!";

        BlurUtils.drawBlurScreen(1 + blurAnimation.getValueFloat());

        if (fadeAnimation == null && this.getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = new DecelerateAnimation(800, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
            timer.reset();
        }

        if (blurAnimation.isDone(Direction.BACKWARDS)) {
            Shindo.getInstance().getShindoAPI().createFirstLoginFile();
            this.setCurrentScene(this.getSceneByClass(MainScene.class));
        }

        if (fadeAnimation != null) {

            switch (step) {
                case 0:
                    message = compMessage;
                    break;
                case 1:
                    message = welcomeMessage;
                    break;
            }
            if (!soundPlayed) {
                Sound.play("shindo/audio/success.wav", true);
                soundPlayed = true;
            }
            nvg.setupAndDraw(() -> {
                nvg.drawCenteredText(message, sr.getScaledWidth() / 2,
                        (sr.getScaledHeight() / 2) - (nvg.getTextHeight(message, 26, Fonts.REGULAR) / 2),
                        new Color(255, 255, 255, (int) (fadeAnimation.getValueFloat() * 255)), 26, Fonts.REGULAR);
            });

            if (timer.delay(3000) && fadeAnimation.getDirection().equals(Direction.FORWARDS)) {
                fadeAnimation.setDirection(Direction.BACKWARDS);
                timer.reset();
            }

            if (fadeAnimation.isDone(Direction.BACKWARDS)) {

                if (step == 1) {
                    blurAnimation.setDirection(Direction.BACKWARDS);
                    return;
                }

                step++;
                fadeAnimation.setDirection(Direction.FORWARDS);
            }
        }
    }
}