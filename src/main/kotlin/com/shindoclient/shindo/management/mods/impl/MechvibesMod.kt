package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventTick
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.mods.impl.mechibes.SoundKey
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.sound.Sound
import com.shindoclient.shindo.utils.RandomUtils.getRandomInt
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

@Suppress("UNUSED")
class MechvibesMod : Mod(TranslateText.MECHVIBES, TranslateText.MECHVIBES_DESCRIPTION, ModCategory.OTHER, Shinconic.MOD_MECHVIBES) {
    private val mouseLeftSound = Sound()
    private val mouseRightSound = Sound()

    private val keyMap = HashMap<Int?, SoundKey>()

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.KEYBOARD)
    private val keyboardEnabled = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private val keyType = KeyType.NK_CREAM

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.KEYBOARD_VOLUME,
        min = 0.0,
        max = 1.0,
        step = 0.05,
        current = 0.5,
    )
    private val keyboardVolume = 0.5

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.MOUSE)
    private val mouseEnabled = true

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MOUSE_VOLUME,
        min = 0.0,
        max = 1.0,
        step = 0.05,
        current = 0.5,
    )
    private val mouseVolume = 0.5
    private var tempKeyboardVolume = 0f
    private var tempKeyboardMode: String? = null
    private var tempMouseVolume = 0f
    private var mouseLeftPress = false
    private var mouseRightPress = false
    private var loaded = false

    override fun setup() {
        loaded = false
    }

    override fun onEnable() {
        super.onEnable()

        loadKeyboardSounds(keyType.resourceFolder)
        loadMouseSounds()
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        if (loaded) {
            val mode = keyType.resourceFolder

            if (mode != tempKeyboardMode) {
                tempKeyboardMode = mode
                loadKeyboardSounds(mode)
            }

            val currentKeyboardVolume = keyboardVolume.toFloat()
            if (tempKeyboardVolume.compareTo(currentKeyboardVolume) != 0) {
                tempKeyboardVolume = currentKeyboardVolume
                for (key in keyMap.values) {
                    key.setVolume(tempKeyboardVolume)
                }
            }

            val currentMouseVolume = mouseVolume.toFloat()
            if (tempMouseVolume.compareTo(currentMouseVolume) != 0) {
                tempMouseVolume = currentMouseVolume
                mouseLeftSound.setVolume(tempMouseVolume)
                mouseRightSound.setVolume(tempMouseVolume)
            }

            for (entry in keyMap.entries) {
                val key = entry.value
                if (key.isPressed && !Keyboard.isKeyDown(entry.key!!)) {
                    key.isPressed = false
                }
            }

            if (keyboardEnabled) {
                for (keyCode in 0..255) {
                    if (!Keyboard.isKeyDown(keyCode)) {
                        continue
                    }
                    val key = keyMap[keyCode] ?: continue
                    if (!key.isPressed) {
                        key.play()
                        key.isPressed = true
                    }
                }
            }

            if (mouseEnabled) {
                if (Mouse.isButtonDown(0) && !mouseLeftPress) {
                    mouseLeftPress = true
                    mouseLeftSound.play()
                }

                if (!Mouse.isButtonDown(0) && mouseLeftPress) {
                    mouseLeftPress = false
                }

                if (Mouse.isButtonDown(1) && !mouseRightPress) {
                    mouseRightPress = true
                    mouseRightSound.play()
                }

                if (!Mouse.isButtonDown(1) && mouseRightPress) {
                    mouseRightPress = false
                }
            }
        }
    }

    private fun loadKeyboardSounds(type: String?) {
        TaskExecutor.runAsync(ThreadPoolType.IO) {
            for (keyCode in 0..255) {
                if (keyCode == Keyboard.KEY_TAB) {
                    keyMap[Keyboard.KEY_TAB] = SoundKey(type, "tab")
                    continue
                }

                if (keyCode == 14) {
                    keyMap[14] = SoundKey(type, "backspace")
                    continue
                }

                if (keyCode == 58) {
                    keyMap[58] = SoundKey(type, "capslock")
                    continue
                }

                if (keyCode == 28) {
                    keyMap[28] = SoundKey(type, "enter")
                    continue
                }

                if (keyCode == Keyboard.KEY_SPACE) {
                    keyMap[Keyboard.KEY_SPACE] = SoundKey(type, "space")
                    continue
                }

                if (keyCode == Keyboard.KEY_LSHIFT) {
                    keyMap[Keyboard.KEY_LSHIFT] = SoundKey(type, "shift")
                    continue
                }

                if (keyCode == Keyboard.KEY_RSHIFT) {
                    keyMap[Keyboard.KEY_RSHIFT] = SoundKey(type, "shift")
                    continue
                }

                keyMap[keyCode] = SoundKey(type, getRandomInt(1, 5).toString())
            }
        }
    }

    private fun loadMouseSounds() {
        TaskExecutor.runAsync(ThreadPoolType.IO) {
            try {
                mouseLeftSound.loadClip(ResourceLocation("shindo/mechvibes/mouse.wav"))
                mouseRightSound.loadClip(ResourceLocation("shindo/mechvibes/mouse.wav"))
            } catch (e: Exception) {
                ShindoLogger.error("An error occurred while loading mouse sounds", e)
            }
            mouseLeftSound.setVolume(mouseVolume.toFloat())
            mouseRightSound.setVolume(mouseVolume.toFloat())
            loaded = true
        }
    }

    private enum class KeyType(
        val resourceFolder: String,
        private val translate: TranslateText,
    ) : PropertyEnum {
        NK_CREAM("nk_cream", TranslateText.NK_CREAM),
        MX_BLUE("mx_blue", TranslateText.MX_BLUE),
        MX_SILVER("mx_silver", TranslateText.MX_SILVER),
        RAZER_GREEN("razer_green", TranslateText.RAZER_GREEN),
        HYPERX_AQUA("hyperx_aqua", TranslateText.HYPERX_AQUA),
        MX_BLACK("mx_black", TranslateText.MX_BLACK),
        TOPRE_PURPLE("topre_purple", TranslateText.TOPRE_PURPLE),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
