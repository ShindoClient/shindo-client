package me.miki.shindo.gui.mainmenu.impl.welcome;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.impl.login.AccountScene;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.buffer.ScreenAlpha;
import me.miki.shindo.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

public class FirstLoginScene extends AccountScene {

    private final ScreenAlpha screenAlpha = new ScreenAlpha();
    private Animation fadeAnimation;

    public FirstLoginScene(GuiShindoMainMenu parent) {
        super(parent);
    }

    @Override
    public void initScene() {
        super.initScene();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        if (fadeAnimation == null) {
            fadeAnimation = new DecelerateAnimation(800, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
        }

        if (fadeAnimation.isDone(Direction.BACKWARDS)) {
            if (Shindo.getInstance().getDownloadManager().isDownloaded()) {
                this.setCurrentScene(this.getSceneByClass(LastMessageScene.class));
            } else {
                this.setCurrentScene(this.getSceneByClass(CheckingDataScene.class));
            }
        }

        BlurUtils.drawBlurScreen(14);

        screenAlpha.wrap(() -> this.drawNanoVG(mouseX, mouseY, partialTicks, sr, instance, nvg), fadeAnimation.getValueFloat());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        nameBox.keyTyped(typedChar, keyCode);
    }
}