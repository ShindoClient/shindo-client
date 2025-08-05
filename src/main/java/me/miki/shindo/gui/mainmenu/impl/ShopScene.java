package me.miki.shindo.gui.mainmenu.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.easing.EaseInOutCirc;
import me.miki.shindo.utils.buffer.ScreenAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class ShopScene extends MainMenuScene {

    private final ScreenAnimation screenAnimation = new ScreenAnimation();
    private Animation introAnimation;
    private final ArrayList<TranslateText> goldFeatures = new ArrayList<>();
    //private ArrayList<TranslateText> diamondFeatures = new ArrayList<>();
    private final TranslateText info;

    public ShopScene(GuiShindoMainMenu parent) {
        super(parent);

        goldFeatures.add(TranslateText.SPECIAL_BADGE);
        goldFeatures.add(TranslateText.SPECIAL_CAPE);

        //diamondFeatures.add(TranslateText.SPECIAL_BADGE);
        //diamondFeatures.add(TranslateText.SPECIAL_CAPE);
        //diamondFeatures.add(TranslateText.CUSTOM_CAPE);

        info = TranslateText.PURCHASE;
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

        int acWidth = 220;
        int acHeight = 190;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

        int offsetY = 0;

        nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.PRICING_PLANS.getText(), acX + (acWidth / 2F), acY + 12, Color.WHITE, 14, Fonts.MEDIUM);
        nvg.drawCenteredText(TranslateText.PRICING_PLANS_DESCRIPTION.getText(), acX + (acWidth / 2F), acY + 30, Color.WHITE, 9, Fonts.REGULAR);
        nvg.drawRoundedRect(acX + 20, acY + 50, 82, 128, 6, this.getBackgroundColor());
        nvg.drawRoundedRect(acX + acWidth - (82) - 20, acY + 50, 82, 128, 6, this.getBackgroundColor());

        nvg.drawCenteredText(TranslateText.PREMIUM.getText(), acX + 20 + (82 / 2F), acY + 58, Color.WHITE, 12, Fonts.MEDIUM);
        nvg.drawCenteredText("20$ / " + TranslateText.LIFETIME.getText(), acX + 20 + (82 / 2F), acY + 71, Color.WHITE, 8, Fonts.REGULAR);
        nvg.drawRect(acX + 20, acY + 80, 82, 1, Color.WHITE);

        nvg.drawRoundedRect(acX + 25, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
        nvg.drawRoundedRect(acX + 25, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
        nvg.drawCenteredText(info.getText(), acX + 25 + (72 / 2F), acY + 159, Color.WHITE, 10, Fonts.MEDIUM);

        for (TranslateText t : goldFeatures) {

            nvg.drawText(LegacyIcon.CHECK_CIRCLE, acX + 25, acY + 87 + offsetY, Color.WHITE, 9, Fonts.LEGACYICON);
            nvg.drawText(t.getText(), acX + 36, acY + 88 + offsetY, Color.WHITE, 8, Fonts.REGULAR);

            offsetY += 12;
        }

        nvg.drawCenteredText(TranslateText.SOON.getText(), acX + acWidth - (82) - 20 + (82 / 2F), acY + 58, Color.WHITE, 12, Fonts.MEDIUM);
        nvg.drawCenteredText("?? / " + TranslateText.MONTH.getText(), acX + acWidth - (82) - 20 + (82 / 2F), acY + 71, Color.WHITE, 8, Fonts.REGULAR);
        nvg.drawRect(acX + acWidth - (82) - 20, acY + 80, 82, 1, Color.WHITE);

        nvg.drawRoundedRect(acX + acWidth - (82) - 20 + 5, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
        nvg.drawRoundedRect(acX + acWidth - (82) - 20 + 5, acY + 158 - 5, 72, 20, 6, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.SOON.getText(), acX + acWidth - (82) - 15 + (72 / 2F), acY + 159, Color.WHITE, 10, Fonts.MEDIUM);

        //offsetY = 0;
        //for(TranslateText t : diamondFeatures) {
        //
        //    nvg.drawText(LegacyIcon.CHECK_CIRCLE, acX + acWidth - (82) - 20 + 5, acY + 87 + offsetY, Color.WHITE, 9, Fonts.LEGACYICON);
        //    nvg.drawText(t.getText(), acX + acWidth - (82) - 20 + 16, acY + 88 + offsetY, Color.WHITE, 8, Fonts.REGULAR);
        //
        //    offsetY+=12;
        //}
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(mc);

        int acWidth = 220;
        int acHeight = 190;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);


        if (!MouseUtils.isInside(mouseX, mouseY, acX, acY, acWidth, acHeight) && !MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 3), 6, 22, 22)) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }
    }
}