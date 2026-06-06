package com.shindoclient.shindo.management.settings.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.settings.Setting
import com.shindoclient.shindo.management.settings.config.ConfigOwner
import com.shindoclient.shindo.utils.ColorUtils
import java.awt.Color

open class ColorSetting : Setting {
    private val defaultColor: Color
    private val showAlpha: Boolean
    private var hue: Float
    private var saturation: Float
    private var brightness: Float
    private var alpha: Int
    private var color: Color

    constructor(text: TranslateText, parent: ConfigOwner, color: Color, showAlpha: Boolean) : super(text, parent) {
        this.color = color
        this.defaultColor = color
        this.hue = ColorUtils.getHue(color)
        this.saturation = ColorUtils.getSaturation(color)
        this.brightness = ColorUtils.getBrightness(color)
        this.alpha = color.alpha
        this.showAlpha = showAlpha
    }

    constructor(name: String, parent: ConfigOwner, color: Color, showAlpha: Boolean) : super(name, parent) {
        this.color = color
        this.defaultColor = color
        this.hue = ColorUtils.getHue(color)
        this.saturation = ColorUtils.getSaturation(color)
        this.brightness = ColorUtils.getBrightness(color)
        this.alpha = color.alpha
        this.showAlpha = showAlpha
    }

    override fun reset() {
        color = defaultColor
        hue = ColorUtils.getHue(color)
        saturation = ColorUtils.getSaturation(color)
        brightness = ColorUtils.getBrightness(color)
        alpha = color.alpha
    }

    fun getColor(): Color = color

    open fun setColor(color: Color) {
        this.color = color
    }

    fun getDefaultColor(): Color = defaultColor

    fun getHue(): Float = hue

    open fun setHue(hue: Float) {
        this.hue = hue
        color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha)
    }

    fun getSaturation(): Float = saturation

    open fun setSaturation(saturation: Float) {
        this.saturation = saturation
        color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha)
    }

    fun getBrightness(): Float = brightness

    open fun setBrightness(brightness: Float) {
        this.brightness = brightness
        color = ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha)
    }

    fun getAlpha(): Int = alpha

    open fun setAlpha(alpha: Int) {
        this.alpha = alpha
        color = ColorUtils.applyAlpha(color, alpha)
    }

    fun isShowAlpha(): Boolean = showAlpha
}
