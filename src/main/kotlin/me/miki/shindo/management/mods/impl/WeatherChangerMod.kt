package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class WeatherChangerMod : Mod(
    TranslateText.WEATHER_CHANGER,
    TranslateText.WEATHER_CHANGER_DESCRIPTION,
    ModCategory.WORLD,
    LegacyIcon.MOD_WEATHER_CHANGER
) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.WEATHER)
    @JvmField
    val weather: Weather = Weather.CLEAR

    @Property(type = PropertyType.NUMBER, translate = TranslateText.RAIN_STRENGTH, min = 0.0, max = 1.0, current = 1.0)
    @JvmField
    var rainStrength = 1.0

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.THUNDER_STRENGTH,
        min = 0.0,
        max = 1.0,
        current = 1.0
    )
    @JvmField
    var thunderStrength = 1.0

    init {
        instance = this
    }

    enum class Weather(private val translate: TranslateText) : PropertyEnum {
        CLEAR(TranslateText.CLEAR),
        RAIN(TranslateText.RAIN),
        STORM(TranslateText.STORM),
        SNOW(TranslateText.SNOW);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }

    companion object {
        @JvmField
        var instance: WeatherChangerMod? = null
    }

    fun getRainStrength(): NumberSetting? = getNumberSetting(this, "rainStrength")

    fun getThunderStrength(): NumberSetting? = getNumberSetting(this, "thunderStrength")
}




