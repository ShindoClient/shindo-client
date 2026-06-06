package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class WeatherChangerMod :
    Mod(
        TranslateText.WEATHER_CHANGER,
        TranslateText.WEATHER_CHANGER_DESCRIPTION,
        ModCategory.WORLD,
        Shinconic.MOD_WEATHER_CHANGER,
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
        current = 1.0,
    )
    @JvmField
    var thunderStrength = 1.0

    init {
        instance = this
    }

    enum class Weather(
        private val translate: TranslateText,
    ) : PropertyEnum {
        CLEAR(TranslateText.CLEAR),
        RAIN(TranslateText.RAIN),
        STORM(TranslateText.STORM),
        SNOW(TranslateText.SNOW),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    companion object {
        @JvmField
        var instance: WeatherChangerMod? = null
    }

    fun getRainStrength(): NumberSetting? = getNumberSetting(this, "rainStrength")

    fun getThunderStrength(): NumberSetting? = getNumberSetting(this, "thunderStrength")
}
