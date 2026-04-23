package me.miki.shindo.management.mods.impl

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.mods.impl.mechibes.SoundKey
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.sound.Sound
import me.miki.shindo.utils.RandomUtils.getRandomInt
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.lang.Float
import kotlin.Exception
import kotlin.Int
import kotlin.String

class MechvibesMod :
    Mod(TranslateText.MECHVIBES, TranslateText.MECHVIBES_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_MECHVIBES) {
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
        current = 0.5
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
        current = 0.5
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
            if (Float.compare(tempKeyboardVolume, currentKeyboardVolume) != 0) {
                tempKeyboardVolume = currentKeyboardVolume
                for (key in keyMap.values) {
                    key.setVolume(tempKeyboardVolume)
                }
            }

            val currentMouseVolume = mouseVolume.toFloat()
            if (Float.compare(tempMouseVolume, currentMouseVolume) != 0) {
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
                    val key = keyMap.get(keyCode)
                    if (key == null) {
                        continue
                    }
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
                    keyMap.put(Keyboard.KEY_TAB, SoundKey(type, "tab"))
                    continue
                }

                if (keyCode == 14) {
                    keyMap.put(14, SoundKey(type, "backspace"))
                    continue
                }

                if (keyCode == 58) {
                    keyMap.put(58, SoundKey(type, "capslock"))
                    continue
                }

                if (keyCode == 28) {
                    keyMap.put(28, SoundKey(type, "enter"))
                    continue
                }

                if (keyCode == Keyboard.KEY_SPACE) {
                    keyMap.put(Keyboard.KEY_SPACE, SoundKey(type, "space"))
                    continue
                }

                if (keyCode == Keyboard.KEY_LSHIFT) {
                    keyMap.put(Keyboard.KEY_LSHIFT, SoundKey(type, "shift"))
                    continue
                }

                if (keyCode == Keyboard.KEY_RSHIFT) {
                    keyMap.put(Keyboard.KEY_RSHIFT, SoundKey(type, "shift"))
                    continue
                }

                keyMap.put(keyCode, SoundKey(type, getRandomInt(1, 5).toString()))
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

    private enum class KeyType(val resourceFolder: String, private val translate: TranslateText) : PropertyEnum {
        NK_CREAM("nk_cream", TranslateText.NK_CREAM),
        MX_BLUE("mx_blue", TranslateText.MX_BLUE),
        MX_SILVER("mx_silver", TranslateText.MX_SILVER),
        RAZER_GREEN("razer_green", TranslateText.RAZER_GREEN),
        HYPERX_AQUA("hyperx_aqua", TranslateText.HYPERX_AQUA),
        MX_BLACK("mx_black", TranslateText.MX_BLACK),
        TOPRE_PURPLE("topre_purple", TranslateText.TOPRE_PURPLE);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}





