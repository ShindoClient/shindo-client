package me.miki.shindo.ui;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn;

import java.util.ArrayList;

public class ClickEffects {

    @Getter
    private static ClickEffects instance;

    private final ArrayList<ClickEffect> effects = new ArrayList<ClickEffect>();
    private final ArrayList<ClickEffect> removeEffects = new ArrayList<ClickEffect>();

    public ClickEffects() {
        instance = this;
    }

    public void drawClickEffects() {

        for (ClickEffect ce : effects) {

            if (ce.isDone()) {
                removeEffects.add(ce);
            }

            ce.draw();
        }

        effects.removeAll(removeEffects);
    }

    public void addClickEffect(int mouseX, int mouseY) {
        effects.add(new ClickEffect(mouseX, mouseY));
    }

    private static class ClickEffect {

        private final Animation animation;
        private final int x;
        private final int y;

        private ClickEffect(int x, int y) {
            this.x = x;
            this.y = y;
            this.animation = new EaseBackIn(650, 1, 0.0F);
        }

        public void draw() {

            Shindo instance = Shindo.getInstance();
            NanoVGManager nvg = instance.getNanoVGManager();
            AccentColor currentColor = instance.getColorManager().getCurrentColor();

            nvg.setupAndDraw(() -> {
                nvg.drawArc(x, y, animation.getValueFloat() * 8, 0, 360, 2, ColorUtils.applyAlpha(currentColor.getInterpolateColor(0), (int) (255 - (animation.getValueFloat() * 255))));
            });
        }

        public boolean isDone() {
            return animation.isDone(Direction.FORWARDS);
        }
    }
}
