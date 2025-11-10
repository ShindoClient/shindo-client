package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.*;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.impl.rearview.RearviewCamera;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.TimerUtils;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class RearviewMod extends HUDMod {

    private final RearviewCamera rearviewCamera = new RearviewCamera();
    private final TimerUtils timer = new TimerUtils();

    @Property(type = PropertyType.NUMBER, translate = TranslateText.WIDTH, min = 10, max = 500, current = 190, step = 1)
    private int rearviewWidthSetting = 190;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.HEIGHT, min = 10, max = 500, current = 100, step = 1)
    private int rearviewHeightSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.FPS, min = 1, max = 120, current = 60, step = 1)
    private int fpsSetting = 60;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.FOV, min = 30, max = 120, current = 70, step = 1)
    private int fovSetting = 70;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LOCK_CAMERA)
    private boolean lockCameraSetting = true;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0F, max = 1.0F, current = 1.0F)
    private double alphaSetting = 1.0F;

    public RearviewMod() {
        super(TranslateText.REARVIEW, TranslateText.REARVIEW_DESCRIPTION, "", true);
    }

    @EventTarget
    public void onRenderTick(EventRenderTick event) {
        if (mc.theWorld != null) {
            if (timer.delay((long) (1000 / fpsSetting))) {
                rearviewCamera.updateMirror();
                timer.reset();
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(nvg));
    }

    private void drawNanoVG(NanoVGManager nvg) {

        int width = (int) (rearviewWidthSetting * this.getScale());
        int height = (int) (rearviewHeightSetting * this.getScale());

        rearviewCamera.setFov(fovSetting);
        rearviewCamera.setLockCamera(lockCameraSetting);

        nvg.drawShadow(this.getX(), this.getY(), width, height, 6 * this.getScale());
        nvg.drawRoundedImage(rearviewCamera.getTexture(), this.getX(), this.getY() + height, width, -height, 6 * this.getScale(), (float) alphaSetting);

        this.setWidth((int) (width / this.getScale()));
        this.setHeight((int) (height / this.getScale()));
    }

    @EventTarget
    public void onFireOverlay(EventFireOverlay event) {
        if (rearviewCamera.isRecording()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onWaterOverlay(EventWaterOverlay event) {
        if (rearviewCamera.isRecording()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHurtCamera(EventHurtCamera event) {
        if (rearviewCamera.isRecording()) {
            event.setIntensity(0);
        }
    }

    @EventTarget
    public void onRenderPumpkinOverlay(EventRenderPumpkinOverlay event) {
        if (rearviewCamera.isRecording()) {
            event.setCancelled(true);
        }
    }
}
