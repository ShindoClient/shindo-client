package me.miki.shindo.utils

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.TaskPriority
import me.miki.shindo.utils.concurrent.ThreadPoolType
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.io.BufferedInputStream
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
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
        // loadClip precisa ser síncrono para manter compatibilidade
        // A otimização está apenas no Sound.play() que é usado para sons de UI
        _clip = AudioSystem.getClip().apply {
            open(
                AudioSystem.getAudioInputStream(
                    BufferedInputStream(mc.resourceManager.getResource(location).inputStream)
                )
            )
        }
    }

    fun loadClip(file: File) {
        _clip = AudioSystem.getClip().apply {
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
        fun play(location: String, uiSound: Boolean) {
            val settings = InternalSettingsMod.instance
            if (uiSound && settings != null && !settings.soundsUISetting) return
            val diskPath = Sound::class.java.classLoader.getResource("assets/minecraft/$location") ?: return

            try {
                if (settings?.soundOptimizationSetting == true) {
                    // Carrega o AudioInputStream em paralelo
                    val future = SoundCache.getOrLoadAudioStream(location) { loc ->
                        AudioSystem.getAudioInputStream(
                            Sound::class.java.classLoader.getResource("assets/minecraft/$loc")
                                ?: throw IllegalStateException("Resource not found: $loc")
                        )
                    }
                    
                    // Cria o Clip no thread principal após carregar
                    future.thenAccept { audioInputStream ->
                        TaskExecutor.runOnMainThread {
                            try {
                                val clip = AudioSystem.getClip()
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
                            } catch (e: Exception) {
                                // Fallback para carregamento síncrono
                                try {
                                    val clip = AudioSystem.getClip()
                                    val fallbackStream = AudioSystem.getAudioInputStream(diskPath)
                                    clip.open(fallbackStream)
                                    clip.start()

                                    clip.addLineListener { event ->
                                        if (event.type == LineEvent.Type.STOP) {
                                            clip.close()
                                            try {
                                                fallbackStream.close()
                                            } catch (_: Exception) {
                                            }
                                        }
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }
                } else {
                    // Comportamento original quando otimização está desabilitada
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
                }
            } catch (_: Exception) {
            }
        }
    }
}

/**
 * Cache para AudioInputStream de sons, permitindo carregamento paralelo.
 */
private object SoundCache {
    private val cache = ConcurrentHashMap<String, CompletableFuture<AudioInputStream>>()

    /**
     * Obtém ou carrega um AudioInputStream de forma assíncrona.
     * Se já estiver carregando, retorna o Future existente.
     */
    fun <T> getOrLoadAudioStream(key: T, loader: (T) -> AudioInputStream): CompletableFuture<AudioInputStream> {
        val cacheKey = key.toString()
        return cache.computeIfAbsent(cacheKey) {
            TaskExecutor.runAsync(ThreadPoolType.IO, TaskPriority.NORMAL) {
                loader(key)
            }
        }
    }

    /**
     * Limpa o cache (útil para liberar memória).
     */
    fun clear() {
        cache.clear()
    }
}
