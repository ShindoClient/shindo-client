package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import me.miki.shindo.management.nanovg.NanoVGManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayListMod extends HUDMod {

    private final BooleanSetting backgroundSetting = new BooleanSetting(TranslateText.BACKGROUND, this, true);

    private final BooleanSetting hudSetting = new BooleanSetting(TranslateText.HUD, this, false);
    private final BooleanSetting renderSetting = new BooleanSetting(TranslateText.RENDER, this, false);
    private final BooleanSetting playerSetting = new BooleanSetting(TranslateText.PLAYER, this, false);
    private final BooleanSetting otherSetting = new BooleanSetting(TranslateText.OTHER, this, false);

    private final ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.RIGHT, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.RIGHT), new Option(TranslateText.LEFT))));


    public ArrayListMod() {
        super(TranslateText.ARRAY_LIST, TranslateText.ARRAY_LIST_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG());
    }

    private void drawNanoVG() {

        Shindo instance = Shindo.getInstance();
        AccentColor currentColor = instance.getColorManager().getCurrentColor();

        ArrayList<Mod> enabledMods = new ArrayList<Mod>();
        int maxWidth = 0;

        for (Mod m : instance.getModManager().getMods()) {

            if (!hudSetting.isToggled() && m.getCategory().equals(ModCategory.HUD)) {
                continue;
            }

            if (!renderSetting.isToggled() && m.getCategory().equals(ModCategory.RENDER)) {
                continue;
            }

            if (!playerSetting.isToggled() && m.getCategory().equals(ModCategory.PLAYER)) {
                continue;
            }

            if (!otherSetting.isToggled() && m.getCategory().equals(ModCategory.OTHER)) {
                continue;
            }

            if (m.isToggled() && !m.isHide()) {

                float nameWidth = this.getTextWidth(m.getName(), 8.5F, getHudFont(1));

                enabledMods.add(m);

                if (maxWidth < nameWidth) {
                    maxWidth = (int) nameWidth;
                }
            }
        }

        enabledMods.sort((m1, m2) -> (int) this.getTextWidth(m2.getName(), 8.5F, getHudFont(1)) - (int) this.getTextWidth(m1.getName(), 8.5F, getHudFont(1)));

        int y = 0;
        int colorIndex = 0;
        boolean isRight = modeSetting.getOption().getTranslate().equals(TranslateText.RIGHT);

        for (Mod m : enabledMods) {

            float nameWidth = this.getTextWidth(m.getName(), 8.5F, getHudFont(1));

            if (backgroundSetting.isToggled()) {
                this.drawRect((isRight ? (maxWidth - nameWidth) : 0), y, nameWidth + 5, 12, new Color(0, 0, 0, 100));
            }

            this.drawText(m.getName(), 3 + (isRight ? (maxWidth - nameWidth) : 0),
                    y + 2.5F, 8.5F, getHudFont(1), currentColor.getInterpolateColor(colorIndex));

            y += 12;
            colorIndex -= 10;
        }

        this.setWidth(maxWidth + 4);
        this.setHeight(y);
    }
}
