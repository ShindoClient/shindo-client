package me.miki.shindo.management.color

import me.miki.shindo.management.color.palette.ColorPalette
import java.awt.Color

class ColorManager {

    private val colors = ArrayList<AccentColor>()
    private val palette = ColorPalette()
    private var currentColor: AccentColor
    private var theme: Theme = Theme.LIGHT

    init {
        add("Default", Color(170, 255, 169), Color(17, 255, 189))
        add("Evening Sunshine", Color(185, 43, 39), Color(21, 101, 192))
        add("Light Orange", Color(255, 183, 94), Color(237, 143, 3))
        add("Reef", Color(0, 210, 255), Color(58, 123, 213))
        add("Amin", Color(142, 45, 226), Color(74, 0, 224))
        add("Magic", Color(89, 193, 115), Color(93, 38, 193))
        add("Mango Pulp", Color(240, 152, 25), Color(237, 222, 93))
        add("Moon Purple", Color(78, 84, 200), Color(143, 148, 251))
        add("Aqualicious", Color(80, 201, 195), Color(150, 222, 218))
        add("Stripe", Color(31, 162, 255), Color(166, 255, 203))
        add("Shifter", Color(188, 78, 156), Color(248, 7, 89))
        add("Quepal", Color(17, 153, 142), Color(56, 239, 125))
        add("Orca", Color(68, 160, 141), Color(9, 54, 55))
        add("Sublime Vivid", Color(252, 70, 107), Color(63, 94, 251))
        add("Moon Asteroid", Color(15, 32, 39), Color(44, 83, 100))
        add("Summer Dog", Color(168, 255, 120), Color(120, 255, 214))
        add("Pink Flavour", Color(128, 0, 128), Color(255, 192, 203))
        add("Sin City Red", Color(237, 33, 58), Color(147, 41, 30))
        add("Timber", Color(252, 0, 255), Color(0, 219, 222))
        add("Pinot Noir", Color(75, 108, 183), Color(24, 40, 72))
        add("Dirty Fog", Color(185, 147, 214), Color(140, 166, 219))
        add("Piglet", Color(238, 156, 167), Color(255, 221, 225))
        add("Little Leaf", Color(118, 184, 82), Color(141, 194, 111))
        add("Nelson", Color(242, 112, 156), Color(255, 148, 114))
        add("Turquoise flow", Color(19, 106, 138), Color(38, 120, 113))
        add("Purplin", Color(106, 48, 147), Color(160, 68, 255))
        add("Martini", Color(253, 252, 71), Color(36, 254, 65))
        add("SoundCloud", Color(254, 140, 0), Color(248, 54, 0))
        add("Inbox", Color(69, 127, 202), Color(86, 145, 200))
        add("Amethyst", Color(157, 80, 187), Color(110, 72, 170))
        add("Blush", Color(178, 69, 146), Color(241, 95, 121))
        add("Mocha Rose", Color(245, 194, 231), Color(243, 139, 168))
        add("Muted Ocean", Color(131, 165, 152), Color(69, 133, 136))
        add("Algae", Color(142, 192, 124), Color(104, 157, 106))
        add("Greys", Color(140, 140, 140), Color(189, 189, 189))
        add("Pandas", Color(182, 182, 182), Color(54, 54, 54))
        add("Flame", Color(224, 7, 7), Color(224, 175, 15))

        currentColor = getColorByName("Default")
    }

    private fun add(name: String, color1: Color, color2: Color) {
        colors.add(AccentColor(name, color1, color2))
    }

    fun getColors(): ArrayList<AccentColor> = colors
    fun getCurrentColor(): AccentColor = currentColor
    fun setCurrentColor(c: AccentColor) {
        currentColor = c
    }

    fun getColorByName(name: String): AccentColor =
        colors.firstOrNull { it.getName() == name } ?: getColorByName("Default")

    fun getTheme(): Theme = theme
    fun setTheme(t: Theme) {
        theme = t
    }
    fun getPalette(): ColorPalette = palette
}
