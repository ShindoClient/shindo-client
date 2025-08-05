package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventPlayerHeadRotation;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import net.minecraft.util.MathHelper;

public class MouseStrokesMod extends HUDMod {

    private float mouseX, mouseY, lastMouseX, lastMouseY;

    public MouseStrokesMod() {
        super(TranslateText.MOUSE_STROKES, TranslateText.MOUSE_STROKES_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> {
            float calculatedMouseX = (lastMouseX + ((mouseX - lastMouseX) * event.getPartialTicks()));
            float calculatedMouseY = (lastMouseY + ((mouseY - lastMouseY) * event.getPartialTicks()));

            this.drawBackground(58, 58);
            this.drawRoundedRect(calculatedMouseX + 28 - 3.5F, calculatedMouseY + 28 - 3.5F, 9, 9, 9 / 2);
        });

        this.setWidth(58);
        this.setHeight(58);
    }

    @EventTarget
    public void onPlayerHeadRotation(EventPlayerHeadRotation event) {
        mouseX += event.getYaw() / 40F;
        mouseY -= event.getPitch() / 40F;
        mouseX = MathHelper.clamp_float(mouseX, -18, 18);
        mouseY = MathHelper.clamp_float(mouseY, -18, 18);
    }

    @EventTarget
    public void onTick(EventTick event) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        mouseX *= 0.75F;
        mouseY *= 0.75F;
    }
}
