package me.miki.shindo.utils;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;

public class TargetUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final TimerUtils timer = new TimerUtils();
    @Getter
    private static AbstractClientPlayer target;

    public static void onUpdate() {

        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.objectMouseOver.entityHit != target) {
            if (mc.objectMouseOver.entityHit instanceof AbstractClientPlayer && ServerUtils.isInTabList(mc.objectMouseOver.entityHit)) {
                target = (AbstractClientPlayer) mc.objectMouseOver.entityHit;
                timer.reset();
            }
        } else if (timer.delay(2500) && mc.objectMouseOver == null) {
            target = null;
            timer.reset();
        }

        if (target != null) {

            if (target.isDead || mc.thePlayer.isDead) {
                target = null;
            } else if (mc.thePlayer != null) {
                if (target.isInvisible() || target.getDistanceToEntity(mc.thePlayer) > 12) {
                    target = null;
                }
            }
        }
    }

}