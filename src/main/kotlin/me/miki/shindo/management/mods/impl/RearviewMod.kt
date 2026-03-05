package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.*
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.impl.rearview.RearviewCamera
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.TimerUtils

class RearviewMod :
    HUDMod(TranslateText.REARVIEW, TranslateText.REARVIEW_DESCRIPTION, LegacyIcon.MOD_REARVIEW, "", true) {
    private val rearviewCamera = RearviewCamera()
    private val timer = TimerUtils()

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.WIDTH,
        min = 10.0,
        max = 500.0,
        current = 190.0,
        step = 1.0
    )
    private val rearviewWidthSetting = 190

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.HEIGHT,
        min = 10.0,
        max = 500.0,
        current = 100.0,
        step = 1.0
    )
    private val rearviewHeightSetting = 100

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.FPS,
        min = 1.0,
        max = 120.0,
        current = 60.0,
        step = 1.0
    )
    private val fpsSetting = 60

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.FOV,
        min = 30.0,
        max = 120.0,
        current = 70.0,
        step = 1.0
    )
    private val fovSetting = 70

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.LOCK_CAMERA)
    private val lockCameraSetting = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0, max = 1.0, current = 1.0)
    private val alphaSetting = 1.0

    @EventTarget
    fun onRenderTick(event: EventRenderTick?) {
        if (mc.theWorld != null) {
            if (timer.delay((1000 / fpsSetting).toLong())) {
                rearviewCamera.updateMirror()
                timer.reset()
            }
        }
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        nvg!!.setupAndDraw(Runnable { drawNanoVG(nvg) })
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val width = (rearviewWidthSetting * this.getScale()).toInt()
        val height = (rearviewHeightSetting * this.getScale()).toInt()

        rearviewCamera.setFov(fovSetting.toFloat())
        rearviewCamera.setLockCamera(lockCameraSetting)

        nvg.drawShadow(
            this.getX().toFloat(),
            this.getY().toFloat(),
            width.toFloat(),
            height.toFloat(),
            6 * this.getScale()
        )
        nvg.drawRoundedImage(
            rearviewCamera.texture,
            this.getX().toFloat(),
            (this.getY() + height).toFloat(),
            width.toFloat(),
            -height.toFloat(),
            6 * this.getScale(),
            alphaSetting.toFloat()
        )

        this.setWidth((width / this.getScale()).toInt())
        this.setHeight((height / this.getScale()).toInt())
    }

    @EventTarget
    fun onFireOverlay(event: EventFireOverlay) {
        if (rearviewCamera.isRecording) {
            event.setCancelled(true)
        }
    }

    @EventTarget
    fun onWaterOverlay(event: EventWaterOverlay) {
        if (rearviewCamera.isRecording) {
            event.setCancelled(true)
        }
    }

    @EventTarget
    fun onHurtCamera(event: EventHurtCamera) {
        if (rearviewCamera.isRecording) {
            event.intensity = 0f
        }
    }

    @EventTarget
    fun onRenderPumpkinOverlay(event: EventRenderPumpkinOverlay) {
        if (rearviewCamera.isRecording) {
            event.setCancelled(true)
        }
    }
}




