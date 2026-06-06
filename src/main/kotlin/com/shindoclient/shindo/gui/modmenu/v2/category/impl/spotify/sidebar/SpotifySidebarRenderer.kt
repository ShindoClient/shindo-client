package com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.sidebar
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyNavigator
import com.shindoclient.shindo.gui.modmenu.v2.category.impl.spotify.nav.SpotifyScreen

import com.shindoclient.spotify.data.PlaylistSimplified
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.color.AccentColor
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.music.MusicManager
import com.shindoclient.shindo.management.music.cache.AlbumArtCache
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.mouse.Scroll
import net.minecraft.util.ResourceLocation
import java.io.File
import java.awt.Color
import kotlin.math.max

private const val SIDEBAR_WIDTH = 140f
private const val SIDEBAR_ROW_H = 34f
private const val SIDEBAR_ROW_SPACING = 38f
private const val CONTROL_BAR_H = 46f
private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")

class SpotifySidebarRenderer(
    private val getX: () -> Float,
    private val getY: () -> Float,
    private val getWidth: () -> Float,
    private val getHeight: () -> Float,
    private val navigator: SpotifyNavigator,
) {
    @Volatile
    var userPlaylists: List<PlaylistSimplified>? = null
    val sidebarScroll = Scroll()

    fun fetchUserPlaylists(mm: MusicManager) {
        mm.getUserPlaylists()
            .thenAccept { playlists ->
                userPlaylists = playlists?.reversed().orEmpty()
            }
            .exceptionally { ex ->
                ShindoLogger.error("Failed to fetch user playlists: ${ex.message}")
                userPlaylists = emptyList()
                null
            }
    }

    fun drawSidebar(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
    ) {
        nvg.drawRoundedRectVarying(
            getX().toFloat(),
            getY().toFloat(),
            SIDEBAR_WIDTH,
            getHeight().toFloat(),
            12f,
            0f,
            0f,
            12f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200),
        )
        nvg.drawRect(
            getX() + SIDEBAR_WIDTH - 1f,
            getY().toFloat(),
            1f,
            getHeight().toFloat(),
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 120),
        )

        sidebarScroll.onScroll()
        sidebarScroll.onAnimation()

        val playlists = userPlaylists ?: return
        val sx = getX() + 10f
        val sw = SIDEBAR_WIDTH - 20f

        nvg.drawText(Lucide.LIBRARY, sx, getY() + 16f, palette.getFontColor(ColorType.NORMAL), 14f, Fonts.LUCIDE)
        nvg.drawText("Your Library", sx + 20f, getY() + 20f, palette.getFontColor(ColorType.DARK), 9f, Fonts.MEDIUM)
        nvg.drawRect(sx, getY() + 34f, sw, 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 150))

        val clipTop = getY() + 38f
        val clipH = getHeight() - 38f - CONTROL_BAR_H

        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = getX().toFloat(),
            y = clipTop,
            width = SIDEBAR_WIDTH,
            height = clipH,
        ) {
            var rowY = clipTop + sidebarScroll.getValue() + 4f
            playlists.forEach { playlist ->
                val inView = rowY + SIDEBAR_ROW_H > clipTop && rowY < clipTop + clipH
                if (inView) {
                    val hovered = MouseUtils.isInside(mouseX, mouseY, sx, rowY, sw, SIDEBAR_ROW_H)
                    val isActive =
                        (navigator.current as? SpotifyScreen.PlaylistDetail)?.playlist?.id == playlist.id
                    if (hovered || isActive) {
                        nvg.drawRoundedRect(
                            sx - 2f,
                            rowY,
                            sw + 4f,
                            SIDEBAR_ROW_H,
                            6f,
                            if (isActive) {
                                ColorUtils.applyAlpha(accentColor.getInterpolateColor(), 30)
                            } else {
                                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 100)
                            },
                        )
                    }
                    drawSmallArt(
                        nvg,
                        Shindo.getInstance().getMusicManager().getPlaylistImageUrl(playlist),
                        sx,
                        rowY + 5f,
                        24f,
                        4f,
                    )
                    val nameColor =
                        if (isActive) accentColor.getInterpolateColor() else palette.getFontColor(ColorType.DARK)
                    nvg.drawText(
                        nvg.getLimitText(playlist.name ?: "Untitled", 9f, Fonts.MEDIUM, sw - 34f),
                        sx + 30f,
                        rowY + 10f,
                        nameColor,
                        9f,
                        Fonts.MEDIUM,
                    )
                    nvg.drawText(
                        nvg.getLimitText(playlist.owner?.displayName ?: "", 8f, Fonts.REGULAR, sw - 34f),
                        sx + 30f,
                        rowY + 22f,
                        palette.getFontColor(ColorType.NORMAL),
                        8f,
                        Fonts.REGULAR,
                    )
                }
                rowY += SIDEBAR_ROW_SPACING
            }
        }
        sidebarScroll.maxScroll = max(0f, playlists.size * SIDEBAR_ROW_SPACING - clipH + 8f)
    }

    private fun drawSmallArt(
        nvg: NanoVGManager,
        imageUrl: String?,
        x: Float,
        y: Float,
        size: Float,
        radius: Float,
    ) {
        val isValid =
            !imageUrl.isNullOrBlank() && imageUrl != AlbumArtCache.PLACEHOLDER_PATH && File(imageUrl).exists()
        if (isValid) {
            try {
                nvg.drawRoundedImage(File(imageUrl), x, y, size, size, radius)
                return
            } catch (e: Exception) {
                ShindoLogger.error("[MUSIC] Failed to draw small art: $imageUrl", e)
            }
        }
        nvg.drawRoundedRect(x, y, size, size, radius, Color(50, 50, 50))
        try {
            nvg.drawRoundedImage(PLACEHOLDER_IMAGE, x, y, size, size, radius)
        } catch (e: Exception) {
            ShindoLogger.error("[MUSIC] Failed to draw small art.", e)
        }
    }
}
