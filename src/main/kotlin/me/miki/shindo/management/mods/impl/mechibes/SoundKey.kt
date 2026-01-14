package me.miki.shindo.management.mods.impl.mechibes

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.Sound
import net.minecraft.util.ResourceLocation

class SoundKey(mode: String?, key: String?) {
    private val sound: Sound
    var isPressed: Boolean
    var lastPressKey: Int

    init {
        sound = Sound()
        try {
            sound.loadClip(ResourceLocation("shindo/mechvibes/" + mode + "/" + key + ".wav"))
        } catch (e: Exception) {
            ShindoLogger.error("Failed load sound", e)
        }

        this.isPressed = false
        lastPressKey = 0
    }

    fun play() {
        sound.play()
    }

    fun setVolume(volume: Float) {
        sound.setVolume(volume)
    }
}
