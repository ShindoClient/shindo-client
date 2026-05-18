package me.miki.shindo.management.nanovg.font

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.IOUtils
import org.lwjgl.nanovg.NanoVG

class FontManager {
    fun init(nvg: Long) {
        loadFont(nvg, Fonts.UNIFONT)
        loadFont(nvg, Fonts.FALLBACK)
        loadFont(nvg, Fonts.REGULAR)
        loadFont(nvg, Fonts.MEDIUM)
        loadFont(nvg, Fonts.SEMIBOLD)
        loadFont(nvg, Fonts.SHINCONIC)
        loadFont(nvg, Fonts.LEGACYICON)
        loadFont(nvg, Fonts.MOJANGLES)
    }

    private fun loadFont(
        nvg: Long,
        font: Font,
    ) {
        if (font.isLoaded) {
            return
        }

        var loaded = -1

        try {
            val buffer = IOUtils.resourceToByteBuffer(font.resourceLocation)
            loaded = NanoVG.nvgCreateFontMem(nvg, font.name, buffer, false)
            font.buffer = buffer
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load font", e)
        }

        if (loaded == -1) {
            throw RuntimeException("Failed to init font ${font.name}")
        } else {
            font.isLoaded = true
            if (font === Fonts.MOJANGLES && Fonts.UNIFONT.isLoaded) {
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.UNIFONT.name)
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.REGULAR.name)
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.FALLBACK.name)
            } else if (font === Fonts.LEGACYICON && Fonts.SHINCONIC.isLoaded) {
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.SHINCONIC.name)
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.UNIFONT.name)
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.FALLBACK.name)
            } else if (Fonts.FALLBACK.isLoaded && font !== Fonts.FALLBACK) {
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.FALLBACK.name)
                NanoVG.nvgAddFallbackFont(nvg, font.name, Fonts.UNIFONT.name)
            }
        }
    }
}
