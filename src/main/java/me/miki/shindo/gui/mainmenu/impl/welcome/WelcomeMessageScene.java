package me.miki.shindo.gui.mainmenu.impl.welcome;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class WelcomeMessageScene extends MainMenuScene {

    private Animation fadeAnimation;
    private int step;
    private String message;

    private final TimerUtils timer = new TimerUtils();

    public WelcomeMessageScene(GuiShindoMainMenu parent) {
        super(parent);

        step = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        String hello = "Hello!";
        String welcomeMessage = "Welcome to Shindo Client";
        String setupMessage = "An Custom Version of Soar Client";
        String setupMessage2 = "Time to setup Shindo.";

        BlurUtils.drawBlurScreen(14);

        if (fadeAnimation == null && this.getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = new DecelerateAnimation(800, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
            timer.reset();
        }

        if (fadeAnimation != null) {

            switch (step) {
                case 0:
                    message = hello;
                    break;
                case 1:
                    message = welcomeMessage;
                    break;
                case 2:
                    message = setupMessage;
                    break;
                case 3:
                    message = setupMessage2;
                    break;
            }

            nvg.setupAndDraw(() -> {
                nvg.drawCenteredText(message, sr.getScaledWidth() / 2,
                        (sr.getScaledHeight() / 2) - (nvg.getTextHeight(message, 26, Fonts.REGULAR) / 2),
                        new Color(255, 255, 255, (int) (fadeAnimation.getValueFloat() * 255)), 26, Fonts.REGULAR);
            });

            if (timer.delay(2500) && fadeAnimation.getDirection().equals(Direction.FORWARDS)) {
                fadeAnimation.setDirection(Direction.BACKWARDS);
                timer.reset();
            }

            if (fadeAnimation.isDone(Direction.BACKWARDS)) {

                if (step == 3) {
                    this.setCurrentScene(this.getSceneByClass(LanguageSelectScene.class));
                    return;
                }

                step++;
                fadeAnimation.setDirection(Direction.FORWARDS);
            }
        }
    }
}
