package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.impl.InternalSettingsMod.HudTheme
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.ui.animation.value.SimpleAnimation
import org.lwjgl.input.Keyboard
import java.awt.Color

class KeystrokesMod :
    HUDMod(TranslateText.KEYSTROKES, TranslateText.KEYSTROKES_DESCRIPTION, LegacyIcon.MOD_KEYSTROKES) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SPACE)
    private val spaceSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.UNMARKED)
    private val unmarkedSetting = false

    private val animations = arrayOfNulls<SimpleAnimation>(5)

    init {
        for (i in 0..4) {
            animations[i] = SimpleAnimation()
        }
    }

    @EventTarget
    fun onRender2D(event: EventNVG?) {

        val openGui = mc.currentScreen != null

        animations[0]!!.setAnimation(
            if (!openGui && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) 1.0f else 0.0f,
            16
        )
        animations[1]!!.setAnimation(
            if (!openGui && Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode)) 1.0f else 0.0f,
            16
        )
        animations[2]!!.setAnimation(
            if (!openGui && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode)) 1.0f else 0.0f,
            16
        )
        animations[3]!!.setAnimation(
            if (!openGui && Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode)) 1.0f else 0.0f,
            16
        )
        animations[4]!!.setAnimation(
            if (!openGui && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)) 1.0f else 0.0f,
            16
        )

        this.drawBackground(32f, 0f, 28f, 28f)

        this.drawBackground(0f, 32f, 28f, 28f)

        this.drawBackground(32f, 32f, 28f, 28f)

        this.drawBackground(64f, 32f, 28f, 28f)

        this.save()
        this.scale(32f, 0f, 28f, 28f, animations[0]!!.value)
        this.drawHighlight(32f, 0f, 28f, 28f, 6f, this.getFontColor((120 * animations[0]!!.value).toInt()))
        this.restore()

        this.save()
        this.scale(0f, 32f, 28f, 28f, animations[1]!!.value)
        this.drawHighlight(0f, 32f, 28f, 28f, 6f, this.getFontColor((120 * animations[1]!!.value).toInt()))
        this.restore()

        this.save()
        this.scale(32f, 32f, 28f, 28f, animations[2]!!.value)
        this.drawHighlight(32f, 32f, 28f, 28f, 6f, this.getFontColor((120 * animations[2]!!.value).toInt()))
        this.restore()

        this.save()
        this.scale(64f, 32f, 28f, 28f, animations[3]!!.value)
        this.drawHighlight(64f, 32f, 28f, 28f, 6f, this.getFontColor((120 * animations[3]!!.value).toInt()))
        this.restore()

        if (!unmarkedSetting) {
            this.drawCenteredText(
                Keyboard.getKeyName(mc.gameSettings.keyBindForward.keyCode),
                32 + (28 / 2f),
                (28 / 2f) - 4,
                12f,
                getHudFont(1)
            )
            this.drawCenteredText(
                Keyboard.getKeyName(mc.gameSettings.keyBindLeft.keyCode),
                (28 / 2f),
                32 + (28 / 2f) - 4,
                12f,
                getHudFont(1)
            )
            this.drawCenteredText(
                Keyboard.getKeyName(mc.gameSettings.keyBindBack.keyCode),
                32 + (28 / 2f),
                32 + (28 / 2f) - 4,
                12f,
                getHudFont(1)
            )
            this.drawCenteredText(
                Keyboard.getKeyName(mc.gameSettings.keyBindRight.keyCode),
                64 + (28 / 2f),
                32 + (28 / 2f) - 4,
                12f,
                getHudFont(1)
            )
        }

        if (spaceSetting) {
            this.drawBackground(0f, 64f, ((28 * 3) + 8).toFloat(), 22f)

            this.save()
            this.scale(0f, 64f, ((28 * 3) + 8).toFloat(), 22f, animations[4]!!.value)
            this.drawHighlight(
                0f,
                64f,
                ((28 * 3) + 8).toFloat(),
                22f,
                6f,
                this.getFontColor((120 * animations[4]!!.value).toInt())
            )
            this.restore()

            if (!unmarkedSetting) {
                this.drawRoundedRect(10f, 74f, ((26 * 3) - 6).toFloat(), 2f, 1f)
            }
        }

        this.setWidth(28 * 3 + 8)
        this.setHeight(if (spaceSetting) 64 + 22 else 32 + 28)
    }

    private fun drawHighlight(addX: Float, addY: Float, width: Float, height: Float, radius: Float, color: Color) {
        val rect = InternalSettingsMod.instance.hudTheme == HudTheme.RECT
        if (!rect) this.drawRoundedRect(addX, addY, width, height, radius, color)
        else this.drawRect(addX, addY, width, height, color)
    }
}




