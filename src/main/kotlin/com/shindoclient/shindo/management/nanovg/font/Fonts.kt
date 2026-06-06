package com.shindoclient.shindo.management.nanovg.font

import net.minecraft.util.ResourceLocation

object Fonts {
    private const val PATH = "shindo/fonts/"

    @JvmField
    val FALLBACK = Font("fallback", ResourceLocation(PATH + "fallback.ttf"))

    @JvmField
    val REGULAR = Font("regular", ResourceLocation(PATH + "inter/Inter-Regular.ttf"))

    @JvmField
    val MEDIUM = Font("medium", ResourceLocation(PATH + "inter/Inter-Medium.ttf"))

    @JvmField
    val SEMIBOLD = Font("semi-bold", ResourceLocation(PATH + "inter/Inter-SemiBold.ttf"))

    @JvmField
    val SHINCONIC = Font("shinconic", ResourceLocation(PATH + "Shinconic.ttf"))

    @JvmField
    val MOJANGLES = Font("mojangles", ResourceLocation(PATH + "mojangles.ttf"))

    @JvmField
    val UNIFONT = Font("unifont", ResourceLocation(PATH + "unifont/unifont.otf"))

    @JvmField
    val BANGERS = Font("bangers", ResourceLocation(PATH + "Bangers.ttf"))

    @JvmField
    val LUCIDE = Font("bangers", ResourceLocation(PATH + "Lucide.ttf"))

    @JvmStatic
    fun getFonts(): ArrayList<Font> = ArrayList(listOf(MEDIUM, SEMIBOLD, REGULAR, LUCIDE, SHINCONIC, MOJANGLES))
}
