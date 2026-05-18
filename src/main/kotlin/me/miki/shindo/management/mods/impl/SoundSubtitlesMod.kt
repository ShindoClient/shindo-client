package me.miki.shindo.management.mods.impl

import com.google.common.collect.Lists
import com.google.gson.JsonParser
import me.miki.extensions.ui.animation.setAnimation
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
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
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
    private val subtitles: MutableList<Subtitle> = Lists.newArrayList()
    private val soundMap = HashMap<String, String>()

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MAX,
        min = 1.0,
        max = 10.0,
        current = 3.0,
        step = 1.0,
    )
    private val maxSetting = 3

    private val backgroundAnimation = SimpleAnimation(0.0f)

    init {
        instance = this

        val mapped = ResourceLocation("shindo/soundtitles/data.json")

        try {
            val obj =
                JsonParser
                    .parseString(read(mc.resourceManager.getResource(mapped).inputStream))
                    .asJsonObject
            for (entry in obj.entrySet()) {
                soundMap.put(entry.key, entry.value.asString)
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
        val vec3 =
            Vec3(
                mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.eyeHeight.toDouble(),
                mc.thePlayer.posZ,
            )
        val vec31 =
            (Vec3(0.0, 0.0, -1.0))
                .rotatePitch(-mc.thePlayer.rotationPitch * 0.017453292f)
                .rotateYaw(-mc.thePlayer.rotationYaw * 0.017453292f)
        val vec32 =
            (Vec3(0.0, 1.0, 0.0))
                .rotatePitch(-mc.thePlayer.rotationPitch * 0.017453292f)
                .rotateYaw(-mc.thePlayer.rotationYaw * 0.017453292f)
        val vec33 = vec31.crossProduct(vec32)

        val subtitleWidth = 120
        val subtitleHeight = (if (this.isEditing()) 3 else subtitles.size) * 16

        val removeList = ArrayList<Subtitle>()

        for (subtitle in subtitles) {
            if (subtitle.startTime + 3000L <= Minecraft.getSystemTime()) {
                subtitle.isRemove = true
            }

            if (subtitle.isRemove && subtitle.isDone) {
                removeList.add(subtitle)
            }
        }

        subtitles.removeAll(removeList)

        backgroundAnimation.setAnimation(subtitleHeight.toFloat(), 20)

        if (backgroundAnimation.getValue() > 1) {
            val fakeSubtitle = ArrayList<Subtitle>()

            this.drawBackground(subtitleWidth.toFloat(), backgroundAnimation.getValue())

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
                val subtitleLocation = subtitle.location ?: continue
                val vec34 = subtitleLocation.subtract(vec3).normalize()
                val d0 = -vec33.dotProduct(vec34)
                val d1 = -vec31.dotProduct(vec34)
                val flag = d1 > 0.5

                subtitle.animation.setAnimation((if (subtitle.isRemove) 0 else 1).toFloat(), 17)

                if (subtitle.animation.getValue() < 0.1 && subtitle.isRemove) {
                    subtitle.isDone = true
                }

                val opacity = if (this.isEditing()) 255 else (subtitle.animation.getValue() * 255).toInt()
                var animationOffsetY =
                    (((index - 2) * 16) + (if (this.isEditing()) 1f else subtitle.animation.getValue()) * 16)

                if (index == 1) {
                    animationOffsetY = 0f
                }

                this.drawCenteredText(
                    subtitle.string ?: "",
                    subtitleWidth / 2f,
                    animationOffsetY + 4,
                    9f,
                    getHudFont(1),
                    this.getFontColor(opacity),
                )

                if (!flag) {
                    if (d0 > 0.0) {
                        this.drawText(
                            ">",
                            subtitleWidth - this.getTextWidth("<", 9f, getHudFont(1))!! - 4.5f,
                            animationOffsetY + 4.5f,
                            9f,
                            getHudFont(1),
                            this.getFontColor(opacity),
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

        var s = getSoundName(soundIn.soundLocation)

        if (s == null) {
            s = soundIn.soundLocation.getResourcePath()
        }

        if (s.isNullOrEmpty()) {
            return
        }

        if (!this.subtitles.isEmpty()) {
            for (subtitle in subtitles) {
                if (subtitle.string == s) {
                    subtitle.refresh(
                        Vec3(
                            soundIn.xPosF.toDouble(),
                            soundIn.yPosF.toDouble(),
                            soundIn.zPosF.toDouble(),
                        ),
                    )
                    return
                }
            }
        }

        this.subtitles.add(
            Subtitle(
                s,
                Vec3(soundIn.xPosF.toDouble(), soundIn.yPosF.toDouble(), soundIn.zPosF.toDouble()),
            ),
        )
    }

    private fun getSoundName(location: ResourceLocation): String? = soundMap[location.getResourcePath()]

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
