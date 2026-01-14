package me.miki.shindo.management.mods.impl

import com.google.common.collect.Lists
import com.google.gson.JsonParser
import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.impl.subtitle.Subtitle
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.ISound
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class SoundSubtitlesMod :
    HUDMod(TranslateText.SOUND_SUBTITLES, TranslateText.SOUND_SUBTITLES_DESCRIPTION, LegacyIcon.MOD_SOUND_SUBTITLES) {
    private val subtitles: MutableList<Subtitle> = Lists.newArrayList<Subtitle?>()
    private val soundMap = HashMap<String?, String?>()

    @Property(type = PropertyType.NUMBER, translate = TranslateText.MAX, min = 1, max = 10, current = 3, step = 1)
    private val maxSetting = 3

    private val backgroundAnimation = SimpleAnimation(0.0f)

    init {
        instance = this

        val mapped = ResourceLocation("shindo/soundtitles/data.json")

        try {
            val obj = JsonParser.parseString(read(mc.getResourceManager().getResource(mapped).getInputStream()))
                .getAsJsonObject()
            for (entry in obj.entrySet()) {
                soundMap.put(entry.key, entry.value.getAsString())
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load sound subtitles", e)
        }
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        nvg!!.setupAndDraw(Runnable { this.drawNanoVG() })
    }

    private fun drawNanoVG() {
        val Vec3 =
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight().toDouble(), mc.thePlayer.posZ)
        val Vec31 = (Vec3(0.0, 0.0, -1.0)).rotatePitch(-mc.thePlayer.rotationPitch * 0.017453292f)
            .rotateYaw(-mc.thePlayer.rotationYaw * 0.017453292f)
        val Vec32 = (Vec3(0.0, 1.0, 0.0)).rotatePitch(-mc.thePlayer.rotationPitch * 0.017453292f)
            .rotateYaw(-mc.thePlayer.rotationYaw * 0.017453292f)
        val Vec33 = Vec31.crossProduct(Vec32)

        val subtitleWidth = 120
        val subtitleHeight = (if (this.isEditing()) 3 else subtitles.size) * 16

        val removeList = ArrayList<Subtitle?>()

        for (subtitle in subtitles) {
            if (subtitle.getStartTime() + 3000L <= Minecraft.getSystemTime()) {
                subtitle.setRemove(true)
            }

            if (subtitle.isRemove() && subtitle.isDone()) {
                removeList.add(subtitle)
            }
        }

        subtitles.removeAll(removeList)

        backgroundAnimation.setAnimation(subtitleHeight.toFloat(), 20)

        if (backgroundAnimation.value > 1) {
            val fakeSubtitle = ArrayList<Subtitle?>()

            this.drawBackground(subtitleWidth.toFloat(), backgroundAnimation.value)

            var index = 1

            if (this.isEditing()) {
                val posX = mc.thePlayer.posX
                val posY = mc.thePlayer.posY
                val posZ = mc.thePlayer.posZ

                fakeSubtitle.add(Subtitle("Sound 1", Vec3(posX, posY, posZ)))
                fakeSubtitle.add(Subtitle("Sound 2", Vec3(posX, posY, posZ)))
                fakeSubtitle.add(Subtitle("Sound 3", Vec3(posX, posY, posZ)))
            }

            for (subtitle in (if (this.isEditing()) fakeSubtitle else subtitles)) {
                val Vec34 = subtitle!!.getLocation().subtract(Vec3).normalize()
                val d0 = -Vec33.dotProduct(Vec34)
                val d1 = -Vec31.dotProduct(Vec34)
                val flag = d1 > 0.5

                subtitle.animation.setAnimation((if (subtitle.isRemove()) 0 else 1).toFloat(), 17)

                if (subtitle.animation.value < 0.1 && subtitle.isRemove()) {
                    subtitle.setDone(true)
                }

                val opacity = if (this.isEditing()) 255 else (subtitle.animation.value * 255).toInt()
                var animationOffsetY =
                    (((index - 2) * 16) + (if (this.isEditing()) 1f else subtitle.animation.value) * 16)

                if (index == 1) {
                    animationOffsetY = 0f
                }

                this.drawCenteredText(
                    subtitle.getString(),
                    subtitleWidth / 2f,
                    animationOffsetY + 4,
                    9f,
                    getHudFont(1),
                    this.getFontColor(opacity)
                )

                if (!flag) {
                    if (d0 > 0.0) {
                        this.drawText(
                            ">",
                            subtitleWidth - this.getTextWidth("<", 9f, getHudFont(1))!! - 4.5f,
                            animationOffsetY + 4.5f,
                            9f,
                            getHudFont(1),
                            this.getFontColor(opacity)
                        )
                    } else if (d0 < 0.0) {
                        this.drawText("<", 4.5f, animationOffsetY + 4.5f, 9f, getHudFont(1), this.getFontColor(opacity))
                    }
                }

                index++
            }

            this.setWidth(subtitleWidth)
            this.setHeight(subtitleHeight)
        }
    }

    fun soundPlay(soundIn: ISound) {
        if (subtitles.size >= maxSetting) {
            return
        }

        var s = getSoundName(soundIn.getSoundLocation())

        if (s == null) {
            s = soundIn.getSoundLocation().getResourcePath()
        }

        if (s.isEmpty()) {
            return
        }

        if (!this.subtitles.isEmpty()) {
            for (subtitle in subtitles) {
                if (subtitle.getString() == s) {
                    subtitle.refresh(
                        Vec3(
                            soundIn.getXPosF().toDouble(),
                            soundIn.getYPosF().toDouble(),
                            soundIn.getZPosF().toDouble()
                        )
                    )
                    return
                }
            }
        }

        this.subtitles.add(
            Subtitle(
                s,
                Vec3(soundIn.getXPosF().toDouble(), soundIn.getYPosF().toDouble(), soundIn.getZPosF().toDouble())
            )
        )
    }

    private fun getSoundName(location: ResourceLocation): String? {
        return soundMap.get(location.getResourcePath())
    }

    @Throws(IOException::class)
    private fun read(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val stringBuilder = StringBuilder()

        var line: String?

        while ((reader.readLine().also { line = it }) != null) {
            stringBuilder.append(line)
        }

        return stringBuilder.toString()
    }

    companion object {
        @JvmField
        var instance: SoundSubtitlesMod? = null
    }
}




