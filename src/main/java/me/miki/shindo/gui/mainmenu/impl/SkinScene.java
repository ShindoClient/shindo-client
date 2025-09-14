package me.miki.shindo.gui.mainmenu.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.skin.Skin;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.easing.EaseInOutCirc;
import me.miki.shindo.utils.buffer.ScreenAnimation;
import me.miki.shindo.utils.mouse.Scroll;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class SkinScene extends MainMenuScene {

    private final ScreenAnimation screenAnimation = new ScreenAnimation();
    private final Scroll scroll = new Scroll();
    private Animation introAnimation;
    private Skin currentSkin;

    public SkinScene(GuiShindoMainMenu parent) {
        super(parent);
    }

    @Override
    public void initScene() {
        introAnimation = new EaseInOutCirc(250, 1.0F);
        introAnimation.setDirection(Direction.FORWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        screenAnimation.wrap(() -> drawNanoVG(mouseX, mouseY, sr, instance, nvg), 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), false);
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            this.setCurrentScene(this.getSceneByClass(MainScene.class));
        }
    }

    private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Shindo instance, NanoVGManager nvg) {
        ColorPalette palette = instance.getColorManager().getPalette();

        int acWidth = 400;
        int acHeight = 300;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
        int offsetX = 0;
        int offsetY = 0;
        int index = 1;
        int prevIndex = 1;

        scroll.onScroll();
        scroll.onAnimation();

        nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
        nvg.drawCenteredText("Skins", acX + (acWidth / 2F), acY + 8, Color.WHITE, 14, Fonts.SEMIBOLD);

        nvg.save();
        nvg.scissor(acX, acY + 25, acWidth, acHeight - 25);
        nvg.translate(0, scroll.getValue());


        nvg.restore();
    }
}
