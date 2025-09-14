package me.miki.shindo.gui.mainmenu.impl.welcome;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.gui.mainmenu.impl.login.AccountScene;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class LoginMessageScene extends MainMenuScene {

    private final TimerUtils timer = new TimerUtils();
    private Animation fadeAnimation;

    public LoginMessageScene(GuiShindoMainMenu parent) {
        super(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        String message = "Finally, let's login to Minecraft!";

        BlurUtils.drawBlurScreen(14);

        if (fadeAnimation == null && this.getParent().isDoneBackgroundAnimation()) {
            fadeAnimation = new DecelerateAnimation(800, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
            timer.reset();
        }

        if (fadeAnimation != null) {

            nvg.setupAndDraw(() -> {
                nvg.drawCenteredText(message, sr.getScaledWidth() / 2F,
                        (sr.getScaledHeight() / 2F) - (nvg.getTextHeight(message, 26, Fonts.REGULAR) / 2),
                        new Color(255, 255, 255, (int) (fadeAnimation.getValueFloat() * 255)), 26, Fonts.REGULAR);
            });

            if (timer.delay(5000) && fadeAnimation.getDirection().equals(Direction.FORWARDS)) {
                fadeAnimation.setDirection(Direction.BACKWARDS);
                timer.reset();
            }

            if (fadeAnimation.isDone(Direction.BACKWARDS)) {
                this.setCurrentScene(this.getSceneByClass(AccountScene.class));
            }
        }
    }
}
