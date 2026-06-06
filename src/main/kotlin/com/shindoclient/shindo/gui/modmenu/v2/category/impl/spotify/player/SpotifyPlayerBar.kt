package com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.player
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyNavigator

import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.music.MusicManager
import com.shindoclient.shindo.management.music.cache.AlbumArtCache
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.ui.components.v2.inputs.CompSlider
import com.shindoclient.shindo.utils.mouse.MouseUtils
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File

private const val CONTROL_BAR_H = 46f
private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")

class SpotifyPlayerBar(
    private val getX: () -> Float,
    private val getY: () -> Float,
    private val getWidth: () -> Float,
    private val getHeight: () -> Float,
    private val volumeSlider: CompSlider,
    private val getTrackPosition: () -> Long,
    private val getTrackDuration: () -> Long,
    private val navigator: SpotifyNavigator,
) {
    private val noColor = Color(0, 0, 0, 0)

    fun drawBottomControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        showLyricsButton: Boolean,
    ) {
        drawControlBar(nvg, palette, mm)
        drawPlaybackControls(nvg, palette, mm)
        drawVolumeSlider(nvg, palette, mouseX, mouseY, partialTicks)
        if (showLyricsButton) drawLyricsButton(nvg, palette, mouseX, mouseY)
        drawProgressBar(nvg, accentColor, palette)
    }

    fun drawSharedBottomControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mm: MusicManager,
        mouseX: Int,
        mouseY: Int,
    ) {
        drawControlBar(nvg, palette, mm)
        drawPlaybackControls(nvg, palette, mm)
        drawVolumeSlider(nvg, palette, mouseX, mouseY, 0f)
        drawProgressBar(nvg, accentColor, palette)
    }

    fun drawControlBar(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mm: MusicManager,
    ) {
        nvg.drawRoundedRectVarying(
            getX().toFloat(),
            getY() + getHeight() - CONTROL_BAR_H,
            getWidth().toFloat(),
            CONTROL_BAR_H,
            0f,
            0f,
            0f,
            12f,
            palette.getBackgroundColor(ColorType.DARK),
        )
        val track = mm.getCurrentTrack()
        val artY = getY() + getHeight() - 43f
        if (track != null) {
            val url = mm.getAlbumArtUrl(track)
            val valid = !url.isNullOrBlank() && url != AlbumArtCache.PLACEHOLDER_PATH && File(url!!).exists()
            if (valid) {
                try {
                    nvg.drawRoundedImage(File(url!!), getX() + 4f, artY, 36f, 36f, 6f)
                } catch (_: Exception) {
                    drawBarPlaceholder(nvg, artY)
                }
            } else {
                drawBarPlaceholder(nvg, artY)
            }
            nvg.drawText(
                nvg.getLimitText(track.name ?: "Unknown", 9f, Fonts.MEDIUM, 100f),
                getX() + 45f,
                getY() + getHeight() - 39f,
                palette.getFontColor(ColorType.DARK),
                9f,
                Fonts.MEDIUM,
            )
            nvg.drawText(
                nvg.getLimitText(track.artists?.firstOrNull()?.name ?: "", 9f, Fonts.REGULAR, 100f),
                getX() + 45f,
                getY() + getHeight() - 27f,
                palette.getFontColor(ColorType.NORMAL),
                9f,
                Fonts.REGULAR,
            )
        } else {
            drawBarPlaceholder(nvg, artY)
            nvg.drawText(
                TranslateText.NOTHING_IS_PLAYING.getText(),
                getX() + 45f,
                getY() + getHeight() - 33f,
                palette.getFontColor(ColorType.DARK),
                9f,
                Fonts.MEDIUM,
            )
        }
    }

    private fun drawBarPlaceholder(
        nvg: NanoVGManager,
        artY: Float,
    ) {
        nvg.drawRoundedRect(getX() + 4f, artY, 36f, 36f, 6f, Color(50, 50, 50))
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, getX() + 4f, artY, 36f, 36f, 6f)
        } catch (_: Exception) {
        }
    }

    fun drawPlaybackControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mm: MusicManager,
    ) {
        val cx = getX() + getWidth() / 2f
        val cy = getY() + getHeight() - 32f
        val col = palette.getFontColor(ColorType.NORMAL)
        nvg.drawText(Lucide.REWIND, cx - 32f, cy, col, 16f, Fonts.LUCIDE)
        nvg.drawText(if (mm.isPlaying()) Lucide.PAUSE else Lucide.PLAY, cx - 8f, cy, col, 16f, Fonts.LUCIDE)
        nvg.drawText(Lucide.FAST_FORWARD, cx + 16f, cy, col, 16f, Fonts.LUCIDE)
    }

    fun drawVolumeSlider(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        volumeSlider.setX(getX() + getWidth() - 72f)
        volumeSlider.setY(getY() + getHeight() - 20f)
        volumeSlider.setWidth(62f)
        volumeSlider.setHeight(4.5f)
        volumeSlider.draw(mouseX, mouseY, partialTicks)
        val vol = (volumeSlider.getSetting().getValueFloat() * 100).toInt()
        nvg.drawText(
            when {
                vol == 0 -> Lucide.VOLUME_X
                vol > 80 -> Lucide.VOLUME_2
                vol > 40 -> Lucide.VOLUME_1
                else -> Lucide.VOLUME
            },
            getX() + getWidth() - 94f,
            getY() + getHeight() - 26f,
            palette.getFontColor(ColorType.NORMAL),
            16f,
            Fonts.LUCIDE,
        )
    }

    fun drawProgressBar(
        nvg: NanoVGManager,
        accentColor: AccentColor,
        palette: ColorPalette,
    ) {
        val trackDuration = getTrackDuration()
        if (trackDuration <= 0) return
        val y = (getY() + getHeight() - 5).toFloat()
        val w = (getWidth() - 40).toFloat()
        nvg.drawRoundedRect(getX() + 20f, y, w, 2f, 1f, palette.getBackgroundColor(ColorType.NORMAL))
        nvg.drawRoundedRect(
            getX() + 20f,
            y,
            w * (getTrackPosition().toFloat() / trackDuration),
            2f,
            1f,
            accentColor.getInterpolateColor(),
        )
    }

    fun drawLyricsButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
    ) {
        val bx = getX() + getWidth() - 116f
        val by = getY() + getHeight() - 26f
        nvg.drawText(
            Lucide.LIST,
            bx,
            by,
            if (MouseUtils.isInside(mouseX, mouseY, bx, by, 16f, 16f)) {
                palette.getFontColor(ColorType.DARK)
            } else {
                palette.getFontColor(ColorType.NORMAL)
            },
            16f,
            Fonts.LUCIDE,
        )
    }

    fun drawLoadingSpinner(
        nvg: NanoVGManager,
        palette: ColorPalette,
    ) {
        nvg.drawCenteredText(
            "Loading...",
            getX() + getWidth() / 2f,
            getY() + (getHeight() - CONTROL_BAR_H) / 2f,
            palette.getFontColor(ColorType.NORMAL),
            11f,
            Fonts.MEDIUM,
        )
    }

    fun drawErrorMessage(
        nvg: NanoVGManager,
        palette: ColorPalette,
        message: String,
    ) {
        val cy = getY() + (getHeight() - CONTROL_BAR_H) / 2f
        nvg.drawCenteredText("Failed to load", getX() + getWidth() / 2f, cy - 8f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        nvg.drawCenteredText(message.take(60), getX() + getWidth() / 2f, cy + 6f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.REGULAR)
    }
}
