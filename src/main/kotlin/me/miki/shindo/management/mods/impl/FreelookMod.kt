package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventCameraRotation
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventPlayerHeadRotation
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.gui.Gui
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard

class FreelookMod : Mod(
    TranslateText.FREELOOK,
    TranslateText.FREELOOK_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_FREELOOK,
    "perspectivemod",
    true
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.INVERT_YAW)
    private val invertYawSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.INVERT_PITCH)
    private val invertPitchSetting = false

    @Property(type = PropertyType.COMBO, translate = TranslateText.MODE)
    private val modeSetting = Mode.KEYDOWN

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_V)
    private val keybindSetting = Keyboard.KEY_V

    var isActive: Boolean = false
        private set
    var cameraYaw: Float = 0f
        private set
    var cameraPitch: Float = 0f
        private set
    private var previousPerspective = 0
    private var toggleActive = false

    init {
        instance = this
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        val mode = modeSetting

        if (mode == Mode.KEYDOWN) {
            if (this.isKeyBindDown) {
                start()
            } else {
                stop()
            }
        }

        if (mode == Mode.TOGGLE) {
            if (toggleActive) {
                start()
            } else {
                stop()
            }
        }
    }

    @EventTarget
    fun onKey(event: EventKey) {
        val mode = modeSetting

        if (mode == Mode.TOGGLE) {
            if (event.keyCode == keybindSetting && mc.currentScreen == null) {
                toggleActive = !toggleActive
            }
        }

        if (event.keyCode == mc.gameSettings.keyBindTogglePerspective.keyCode) {
            toggleActive = false
        }
    }

    @EventTarget
    fun onCameraRotation(event: EventCameraRotation) {
        if (this.isActive) {
            event.setYaw(this.cameraYaw)
            event.setPitch(this.cameraPitch)
        }
    }

    @EventTarget
    fun onPlayerHeadRotation(event: EventPlayerHeadRotation) {
        if (this.isActive) {
            var yaw = event.getYaw()
            var pitch = event.getPitch()
            event.setCancelled(true)
            pitch = -pitch

            if (!invertPitchSetting) {
                pitch = -pitch
            }

            if (invertYawSetting) {
                yaw = -yaw
            }

            this.cameraYaw += yaw * 0.15f
            this.cameraPitch = MathHelper.clamp_float(this.cameraPitch + (pitch * 0.15f), -90f, 90f)
            mc.renderGlobal.setDisplayListEntitiesDirty()
        }
    }

    private fun start() {
        if (!this.isActive) {
            this.isActive = true
            previousPerspective = mc.gameSettings.thirdPersonView
            mc.gameSettings.thirdPersonView = 3
            val renderView = mc.renderViewEntity
            this.cameraYaw = renderView.rotationYaw
            this.cameraPitch = renderView.rotationPitch
        }
    }

    private fun stop() {
        if (this.isActive) {
            this.isActive = false
            mc.gameSettings.thirdPersonView = previousPerspective
            mc.renderGlobal.setDisplayListEntitiesDirty()
        }
    }

    private val isKeyBindDown: Boolean
        get() = Keyboard.isKeyDown(keybindSetting) && mc.currentScreen !is Gui

    private enum class Mode(private val translate: TranslateText) : PropertyEnum {
        TOGGLE(TranslateText.TOGGLE),
        KEYDOWN(TranslateText.KEYDOWN);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }

    companion object {
        @JvmField
        var instance: FreelookMod? = null
    }
}




