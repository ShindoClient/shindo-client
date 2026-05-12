package me.miki.shindo.management.sound

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.BufferedInputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.math.log10

class Sound {

    private val mc: Minecraft = Minecraft.getMinecraft()

    private var clip: Clip? = null

    fun loadClip(location: ResourceLocation) {
        clip = SoundCache.getOrLoad(location.toString()) {
            loadFromStream(
                BufferedInputStream(
                    mc.resourceManager.getResource(location).inputStream
                )
            )
        }
    }

    fun loadClip(file: File) {
        clip = SoundCache.getOrLoad(file.absolutePath) {
            loadFromStream(file.inputStream().buffered())
        }
    }

    fun play() {
        val c = clip ?: return

        if (c.isRunning) {
            c.stop()
        }

        c.framePosition = 0
        c.start()
    }

    fun setVolume(volume: Float) {
        val c = clip ?: return

        val gain = c.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val dB = (log10(volume.toDouble()) * 20.0).toFloat()

        gain.value = dB.coerceIn(gain.minimum, gain.maximum)
    }

    private fun loadFromStream(stream: BufferedInputStream): Clip {
        stream.use { input ->

            val audio = AudioSystem.getAudioInputStream(input)
            val clip = AudioSystem.getClip()

            clip.open(audio)

            return clip
        }
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun play(sound: Sounds, uiSound: Boolean = true) {
            play(sound.path, uiSound)
        }

        @JvmStatic
        fun play(location: String, uiSound: Boolean) {

            val settings = InternalSettingsMod.instance
            if (uiSound && !settings.soundsUISetting) return

            try {
                val stream = Sound::class.java.classLoader
                    .getResourceAsStream("assets/minecraft/$location")
                    ?: return

                val audio = AudioSystem.getAudioInputStream(stream)
                val clip = AudioSystem.getClip()

                clip.open(audio)
                clip.start()

                clip.addLineListener { event ->
                    if (event.type == LineEvent.Type.STOP) {
                        clip.close()
                    }
                }

            } catch (_: Exception) {
            }
        }
    }
}

private object SoundCache {

    private val cache = ConcurrentHashMap<String, Clip>()

    fun getOrLoad(key: String, loader: () -> Clip): Clip {
        return cache.computeIfAbsent(key) {
            loader()
        }
    }

    fun clear() {
        cache.values.forEach {
            try {
                it.close()
            } catch (_: Exception) {}
        }
        cache.clear()
    }
}