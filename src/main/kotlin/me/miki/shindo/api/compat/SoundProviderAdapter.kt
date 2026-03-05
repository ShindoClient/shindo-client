package me.miki.shindo.api.compat

import me.miki.shindo.Shindo
import me.miki.client_api.sound.ISoundProvider
import me.miki.shindo.management.sound.Sound
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import kotlin.math.log10

class SoundProviderAdapter : ISoundProvider {

    override fun playSound(resourcePath: String, volume: Float, pitch: Float) {
        try {
            val location = ResourceLocation(resourcePath)
            val clip = AudioSystem.getClip()
            val mc = Minecraft.getMinecraft()
            val resource = mc.resourceManager.getResource(location)
            clip.open(
                javax.sound.sampled.AudioSystem.getAudioInputStream(
                    java.io.BufferedInputStream(resource.inputStream)
                )
            )
            (clip.getControl(FloatControl.Type.MASTER_GAIN) as? FloatControl)?.let { gain ->
                val dB = (log10(volume.toDouble()) * 20.0).toFloat()
                gain.value = dB
            }
            clip.start()
            clip.addLineListener { e ->
                if (e.type == javax.sound.sampled.LineEvent.Type.STOP) clip.close()
            }
        } catch (_: Exception) {
            // Silently fail - resource may not exist
        }
    }

    override fun playSoundFromFile(filePath: String, volume: Float, pitch: Float) {
        try {
            val file = File(filePath)
            if (!file.exists()) return
            val clip = AudioSystem.getClip()
            clip.open(javax.sound.sampled.AudioSystem.getAudioInputStream(file))
            (clip.getControl(FloatControl.Type.MASTER_GAIN) as? FloatControl)?.let { gain ->
                val dB = (log10(volume.toDouble()) * 20.0).toFloat()
                gain.value = dB
            }
            clip.start()
            clip.addLineListener { e ->
                if (e.type == javax.sound.sampled.LineEvent.Type.STOP) clip.close()
            }
        } catch (_: Exception) {
            // Format may not be supported
        }
    }
}
