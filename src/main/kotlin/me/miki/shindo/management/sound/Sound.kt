package me.miki.shindo.management.sound

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.BufferedInputStream
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.math.log10

class Sound {
    private val mc: Minecraft = Minecraft.getMinecraft()

    val clip: Clip?
        get() = _clip

    private var _clip: Clip? = null

    fun loadClip(location: ResourceLocation) {
        _clip =
            AudioSystem.getClip().apply {
                open(
                    AudioSystem.getAudioInputStream(
                        BufferedInputStream(mc.resourceManager.getResource(location).inputStream),
                    ),
                )
            }
    }

    fun loadClip(file: File) {
        _clip =
            AudioSystem.getClip().apply {
                open(AudioSystem.getAudioInputStream(file))
            }
    }

    fun play() {
        _clip?.let {
            it.stop()
            it.framePosition = 0
            it.start()
        }
    }

    fun setVolume(volume: Float) {
        val current = _clip ?: return
        val gainControl = current.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val dB = (log10(volume.toDouble()) * 20.0).toFloat()
        gainControl.value = dB
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun play(
            sound: Sounds,
            uiSound: Boolean = true,
        ) {
            play(sound.path, uiSound)
        }

        @JvmStatic
        fun play(
            location: String,
            uiSound: Boolean,
        ) {
            val settings = InternalSettingsMod.instance
            if (uiSound && !settings.soundsUISetting) return
            val diskPath = Sound::class.java.classLoader.getResource("assets/minecraft/$location") ?: return

            try {
                val clip = AudioSystem.getClip()
                val audioInputStream: AudioInputStream = AudioSystem.getAudioInputStream(diskPath)
                clip.open(audioInputStream)
                clip.start()

                clip.addLineListener { event ->
                    if (event.type == LineEvent.Type.STOP) {
                        clip.close()
                        try {
                            audioInputStream.close()
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
