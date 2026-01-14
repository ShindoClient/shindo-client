package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.TargetUtils.target
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.buffer.ScreenAnimation
import net.minecraft.util.ResourceLocation
import kotlin.math.min

class TargetInfoMod : HUDMod(
    TranslateText.TARGET_INFO,
    TranslateText.TARGET_INFO_DESCRIPTION,
    LegacyIcon.MOD_TARGET_INFO,
    "targethud",
    true
) {
    private val healthAnimation = SimpleAnimation()
    private val armorAnimation = SimpleAnimation()
    private val screenAnimation = ScreenAnimation()
    private var introAnimation: Animation? = null

    private var name: String? = null
    private var health = 0f
    private var armor = 0f
    private var head: ResourceLocation? = null

    public override fun setup() {
        introAnimation = EaseBackIn(320, 1.0, 2.0f)
        introAnimation!!.setDirection(Direction.BACKWARDS)
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        var target = target

        if (this.isEditing()) {
            target = mc.thePlayer
        }

        introAnimation!!.setDirection(if (target == null) Direction.BACKWARDS else Direction.FORWARDS)

        if (target != null) {
            name = target.getName()
            health = min(target.getHealth(), 20f)
            armor = min(target.getTotalArmorValue(), 20).toFloat()
            head = target.getLocationSkin()
        }

        if (name != null && head != null) {
            screenAnimation.wrap(
                Runnable { this.drawNanoVG() },
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                2 - introAnimation!!.getValueFloat(),
                introAnimation!!.getValueFloat()
            )
        }
    }

    private fun drawNanoVG() {
        val nameWidth: Float = this.getTextWidth(name!!, 10.2f, getHudFont(2))!!
        var width = 140

        if (nameWidth + 48f > width) {
            width = (width + nameWidth - 89).toInt()
        }

        healthAnimation.setAnimation(health, 16)
        armorAnimation.setAnimation(armor, 16)

        this.drawBackground(width.toFloat(), 46f)
        this.drawPlayerHead(head!!, 5f, 5f, 36f, 36f, 6f)
        this.drawText(name!!, 45.5f, 8f, 10.2f, getHudFont(2))

        this.drawText(LegacyIcon.HEART_FILL, 52f, 26.5f, 9f, Fonts.LEGACYICON)
        this.drawArc(56.5f, 30.5f, 9f, -90f, -90f + 360, 1.6f, this.getFontColor(120))
        this.drawArc(56.5f, 30.5f, 9f, -90f, -90f + (18 * healthAnimation.value), 1.6f)

        this.drawText(LegacyIcon.SHIELD_FILL, 76f, 26.5f, 9f, Fonts.LEGACYICON)
        this.drawArc(80.5f, 30.5f, 9f, -90f, -90f + 360, 1.6f, this.getFontColor(120))
        this.drawArc(80.5f, 30.5f, 9f, -90f, -90f + (18 * armorAnimation.value), 1.6f)

        this.setWidth(width)
        this.setHeight(46)
    }
}



