package com.shindoclient.shindo.management.color

import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import com.shindoclient.shindo.utils.ColorUtils
import java.awt.Color

enum class Theme(
    private val id: Int,
    private val names: String,
    private val darkBackgroundColor: Color,
    private val normalBackgroundColor: Color,
    private val darkFontColor: Color,
    private val normalFontColor: Color,
) {
    DARK(0, "Dark", Color(19, 19, 20), Color(34, 35, 39), Color(255, 255, 255), Color(235, 235, 235)),
    LIGHT(1, "Light", Color(254, 254, 254), Color(238, 238, 238), Color(54, 54, 54), Color(107, 117, 129)),
    DARK_BLUE(2, "Dark Blue", Color(22, 28, 41), Color(27, 36, 52), Color(157, 175, 211), Color(116, 131, 164)),
    MIDNIGHT(3, "Midnight", Color(47, 54, 61), Color(36, 41, 46), Color(255, 255, 255), Color(235, 235, 235)),
    DARK_PURPLE(4, "Dark Purple", Color(44, 14, 72), Color(53, 24, 90), Color(234, 226, 252), Color(194, 186, 212)),
    SEA(5, "Sea", Color(203, 224, 255), Color(190, 216, 238), Color(32, 32, 32), Color(106, 106, 106)),
    SAKURA(6, "Sakura", Color(255, 191, 178), Color(255, 223, 226), Color(35, 35, 35), Color(80, 80, 80)),
    CATPPUCCIN_MOCHA(
        7,
        "Catppuccin Mocha",
        Color(49, 50, 68),
        Color(30, 30, 46),
        Color(205, 214, 244),
        Color(245, 194, 231),
    ),
    CATPPUCCIN_LATTE(
        8,
        "Catppuccin Latte",
        Color(230, 233, 239),
        Color(239, 241, 245),
        Color(76, 79, 105),
        Color(140, 143, 161),
    ),
    BIRD(9, "Twoot twoot", Color(25, 40, 52), Color(20, 32, 43), Color(255, 255, 255), Color(136, 153, 171)),
    CALIFORNIA(10, "California", Color(22, 22, 25), Color(0, 0, 0), Color(230, 230, 230), Color(130, 130, 130)),
    LAVENDER(11, "Lavender", Color(228, 229, 241), Color(250, 250, 250), Color(72, 75, 105), Color(147, 148, 165)),
    CAMELLIA(12, "Camellia", Color(30, 31, 36), Color(23, 24, 28), Color(228, 229, 231), Color(250, 56, 103)),
    TERMINAL(13, "Terminal", Color(7, 7, 7), Color(12, 12, 12), Color(33, 96, 7), Color(54, 73, 0)),
    NORD(14, "Nord", Color(59, 66, 82), Color(46, 52, 64), Color(236, 239, 244), Color(216, 222, 233)),
    GRUVBOX(15, "Gruvbox Dark Med", Color(0x3C3836), Color(0x282828), Color(0xEBDBB2), Color(0xA89984)),
    ;

    private val animation = SimpleAnimation()

    private val adjDark = ColorUtils.darken(darkBackgroundColor, 0.12f)
    private val adjNormal = ColorUtils.lighten(normalBackgroundColor, 0.10f)
    private val midBackgroundColor = ColorUtils.interpolateColor(adjDark, adjNormal, 0.5)
    private val midFontColor = ColorUtils.interpolateColor(darkFontColor, normalFontColor, 0.5)

    fun getName(): String = names

    fun getId(): Int = id

    fun getDarkBackgroundColor(): Color = adjDark

    fun getMidBackgroundColor(): Color = midBackgroundColor

    fun getNormalBackgroundColor(): Color = adjNormal

    fun getDarkFontColor(): Color = darkFontColor

    fun getMidFontColor(): Color = midFontColor

    fun getNormalFontColor(): Color = normalFontColor

    fun getDarkBackgroundColor(alpha: Int): Color = ColorUtils.applyAlpha(adjDark, alpha)

    fun getMidBackgroundColor(alpha: Int): Color = ColorUtils.applyAlpha(midBackgroundColor, alpha)

    fun getNormalBackgroundColor(alpha: Int): Color = ColorUtils.applyAlpha(adjNormal, alpha)

    fun getDarkFontColor(alpha: Int): Color = ColorUtils.applyAlpha(darkFontColor, alpha)

    fun getMidFontColor(alpha: Int): Color = ColorUtils.applyAlpha(midFontColor, alpha)

    fun getNormalFontColor(alpha: Int): Color = ColorUtils.applyAlpha(normalFontColor, alpha)

    fun getAnimation(): SimpleAnimation = animation

    companion object {
        fun getThemeById(id: Int): Theme = values().find { it.id == id } ?: LIGHT
    }
}
