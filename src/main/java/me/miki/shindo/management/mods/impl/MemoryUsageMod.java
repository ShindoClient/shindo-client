package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

public class MemoryUsageMod extends SimpleHUDMod {

    private final SimpleAnimation animation = new SimpleAnimation();

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN, category = "Display")
    private Design design = Design.SIMPLE;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON, category = "Display")
    private boolean iconSetting = true;

    public MemoryUsageMod() {
        super(TranslateText.MEMORY_USAGE, TranslateText.MEMORY_USAGE_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        if (design == Design.SIMPLE) {
            this.draw();
        } else {
            nvg.setupAndDraw(() -> drawNanoVG(nvg));
        }
    }

    private void drawNanoVG(NanoVGManager nvg) {

        animation.setAnimation(((this.getUsingMemory() / 100F) * 360), 16);

        this.drawBackground(54, 60);
        this.drawCenteredText("Memory", 54 / 2, 6, 9, getHudFont(1));
        this.drawCenteredText(this.getUsingMemory() + "%", 54 / 2, 32, 9, getHudFont(1));

        this.drawArc(27, 35.5F, 16.5F, -90, 360, 1.6F, this.getFontColor(120));
        this.drawArc(27, 35.5F, 16.5F, -90, animation.getValue() - 90, 1.6F, this.getFontColor());

        this.setWidth(54);
        this.setHeight(60);
    }

    @Override
    public String getText() {

        String mem = "Mem: " + getUsingMemory() + "%";

        return mem;
    }

    @Override
    public String getIcon() {
        return iconSetting ? LegacyIcon.SERVER : null;
    }

    private long getUsingMemory() {

        Runtime runtime = Runtime.getRuntime();

        return (runtime.totalMemory() - runtime.freeMemory()) * 100L / runtime.maxMemory();
    }

    private enum Design implements PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY);

        private final TranslateText translate;

        Design(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}
