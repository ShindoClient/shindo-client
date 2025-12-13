package me.miki.shindo.management.nanovg.font

import net.minecraft.util.ResourceLocation
import java.util.ArrayList
import java.util.Arrays

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
    val LEGACYICON = Font("icon", ResourceLocation(PATH + "Icon.ttf"))
    @JvmField
    val MOJANGLES = Font("mojangles", ResourceLocation(PATH + "mojangles.ttf"))
    @JvmField
    val UNIFONT = Font("unifont", ResourceLocation(PATH + "unifont/unifont.otf"))

    @JvmStatic
    fun getFonts(): ArrayList<Font> {
        return ArrayList(Arrays.asList(MEDIUM, SEMIBOLD, REGULAR, LEGACYICON, MOJANGLES))
    }
}
