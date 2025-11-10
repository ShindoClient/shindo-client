package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.nanovg.NanoVGManager;

import java.awt.*;
import java.util.ArrayList;

public class ArrayListMod extends HUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    private boolean backgroundEnabled = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HUD)
    private boolean includeHudMods = false;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.RENDER)
    private boolean includeRenderMods = false;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PLAYER)
    private boolean includePlayerMods = false;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.OTHER)
    private boolean includeOtherMods = false;

    @Property(type = PropertyType.COMBO, translate = TranslateText.MODE)
    private Mode modeSetting = Mode.RIGHT;


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

            if (!includeHudMods && m.getCategory().equals(ModCategory.HUD)) {
                continue;
            }

            if (!includeRenderMods && m.getCategory().equals(ModCategory.RENDER)) {
                continue;
            }

            if (!includePlayerMods && m.getCategory().equals(ModCategory.PLAYER)) {
                continue;
            }

            if (!includeOtherMods && m.getCategory().equals(ModCategory.OTHER)) {
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
        boolean isRight = modeSetting == Mode.RIGHT;

        for (Mod m : enabledMods) {

            float nameWidth = this.getTextWidth(m.getName(), 8.5F, getHudFont(1));

            if (backgroundEnabled) {
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

    private enum Mode implements PropertyEnum {
        RIGHT(TranslateText.RIGHT),
        LEFT(TranslateText.LEFT);

        private final TranslateText translate;

        Mode(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
