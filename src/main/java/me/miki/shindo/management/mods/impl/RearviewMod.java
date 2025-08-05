package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.*;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.impl.rearview.RearviewCamera;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.TimerUtils;

public class RearviewMod extends HUDMod {

    private final RearviewCamera rearviewCamera = new RearviewCamera();
    private final TimerUtils timer = new TimerUtils();

    private final NumberSetting rearviewWidthSetting = new NumberSetting(TranslateText.WIDTH, this, 190, 10, 500, true);
    private final NumberSetting rearviewHeightSetting = new NumberSetting(TranslateText.HEIGHT, this, 100, 10, 500, true);
    private final NumberSetting fpsSetting = new NumberSetting(TranslateText.FPS, this, 60, 1, 120, true);
    private final NumberSetting fovSetting = new NumberSetting(TranslateText.FOV, this, 70, 30, 120, true);
    private final BooleanSetting lockCameraSetting = new BooleanSetting(TranslateText.LOCK_CAMERA, this, true);
    private final NumberSetting alphaSetting = new NumberSetting(TranslateText.ALPHA, this, 1.0F, 0.0F, 1.0F, false);

    public RearviewMod() {
        super(TranslateText.REARVIEW, TranslateText.REARVIEW_DESCRIPTION, "", true);
    }

    @EventTarget
    public void onRenderTick(EventRenderTick event) {
        if (mc.theWorld != null) {
            if (timer.delay((long) (1000 / fpsSetting.getValue()))) {
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

        int width = (int) (rearviewWidthSetting.getValueInt() * this.getScale());
        int height = (int) (rearviewHeightSetting.getValueInt() * this.getScale());

        rearviewCamera.setFov(fovSetting.getValueFloat());
        rearviewCamera.setLockCamera(lockCameraSetting.isToggled());

        nvg.drawShadow(this.getX(), this.getY(), width, height, 6 * this.getScale());
        nvg.drawRoundedImage(rearviewCamera.getTexture(), this.getX(), this.getY() + height, width, -height, 6 * this.getScale(), alphaSetting.getValueFloat());

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
