package me.miki.shindo.ui.comp.factory

import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.CellGridSetting
import me.miki.shindo.management.settings.impl.ColorSetting
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.ImageSetting
import me.miki.shindo.management.settings.impl.KeybindSetting
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.impl.SoundSetting
import me.miki.shindo.management.settings.impl.TextSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.impl.CompCellGrid
import me.miki.shindo.ui.comp.impl.CompColorPicker
import me.miki.shindo.ui.comp.impl.CompComboBox
import me.miki.shindo.ui.comp.impl.CompImageSelect
import me.miki.shindo.ui.comp.impl.CompKeybind
import me.miki.shindo.ui.comp.impl.CompModTextBox
import me.miki.shindo.ui.comp.impl.CompSlider
import me.miki.shindo.ui.comp.impl.CompSoundSelect
import me.miki.shindo.ui.comp.impl.CompToggleButton
import java.util.LinkedHashMap

/**
 * Factory that converts [Setting] instances into UI components.
 */
object SettingComponentFactory {
    private val registry: MutableMap<Class<out Setting>, (Setting) -> Comp> = LinkedHashMap()

    init {
        register(BooleanSetting::class.java  )   { setting -> CompToggleButton(setting as BooleanSetting                           ) }
        register(NumberSetting::class.java   )   { setting -> CompSlider(setting as NumberSetting                                  ) }
        register(ComboSetting::class.java    )   { setting -> CompComboBox(140f, setting as ComboSetting                   ) }
        register(KeybindSetting::class.java  )   { setting -> CompKeybind(120f, setting as KeybindSetting                  ) }
        register(TextSetting::class.java     )   { setting -> CompModTextBox(setting as TextSetting                                ) }
        register(ColorSetting::class.java    )   { setting -> CompColorPicker(setting as ColorSetting                              ) }
        register(ImageSetting::class.java    )   { setting -> CompImageSelect(setting as ImageSetting                              ) }
        register(SoundSetting::class.java    )   { setting -> CompSoundSelect(setting as SoundSetting                              ) }
        register(CellGridSetting::class.java )   { setting -> CompCellGrid(270f, 160f, setting as CellGridSetting ) }
    }

    @JvmStatic
    fun register(type: Class<out Setting>, factory: (Setting) -> Comp) {
        registry[type] = factory
    }

    @JvmStatic
    fun create(setting: Setting): Comp? {
        for ((type, factory) in registry) {
            if (type.isInstance(setting)) {
                return factory(setting)
            }
        }
        return null
    }
}