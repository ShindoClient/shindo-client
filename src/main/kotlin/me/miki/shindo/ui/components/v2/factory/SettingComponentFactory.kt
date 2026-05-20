package me.miki.shindo.ui.components.v2.factory

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
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.ui.components.v2.buttons.CompToggleButton
import me.miki.shindo.ui.components.v2.inputs.CompCellGrid
import me.miki.shindo.ui.components.v2.inputs.CompColorPicker
import me.miki.shindo.ui.components.v2.inputs.CompComboBox
import me.miki.shindo.ui.components.v2.inputs.CompImageSelect
import me.miki.shindo.ui.components.v2.inputs.CompKeybind
import me.miki.shindo.ui.components.v2.inputs.CompModTextBox
import me.miki.shindo.ui.components.v2.inputs.CompSlider
import me.miki.shindo.ui.components.v2.inputs.CompSoundSelect
import java.util.concurrent.ConcurrentHashMap

object SettingComponentFactory {
    private val registry: MutableMap<Class<out Setting>, (Setting) -> Component> = LinkedHashMap()
    private val componentCache: MutableMap<Setting, Component> = ConcurrentHashMap()

    init {
        registerDefaultFactories()
    }

    private fun registerDefaultFactories() {
        register(BooleanSetting::class.java) { setting ->
            CompToggleButton(setting)
        }
        register(NumberSetting::class.java) { setting ->
            CompSlider(setting)
        }
        register(ComboSetting::class.java) { setting ->
            CompComboBox(140f, setting)
        }
        register(KeybindSetting::class.java) { setting ->
            CompKeybind(120f, setting)
        }
        register(TextSetting::class.java) { setting ->
            CompModTextBox(setting)
        }
        register(ColorSetting::class.java) { setting ->
            CompColorPicker(setting)
        }
        register(ImageSetting::class.java) { setting ->
            CompImageSelect(setting)
        }
        register(SoundSetting::class.java) { setting ->
            CompSoundSelect(setting)
        }
        register(CellGridSetting::class.java) { setting ->
            CompCellGrid(270f, 270f, setting)
        }
    }

    @JvmStatic
    fun <T : Setting> register(
        type: Class<T>,
        factory: (T) -> Component,
    ) {
        @Suppress("UNCHECKED_CAST")
        registry[type] = factory as (Setting) -> Component
    }

    @JvmStatic
    fun create(setting: Setting): Component? {
        componentCache[setting]?.let { return it }

        val settingClass = setting.javaClass
        val factory =
            findFactory(settingClass)
                ?: return null

        val component = factory(setting)
        componentCache[setting] = component
        return component
    }

    private fun findFactory(settingClass: Class<out Setting>): ((Setting) -> Component)? {
        registry[settingClass]?.let { return it }

        var current: Class<*>? = settingClass
        while (current != null && Setting::class.java.isAssignableFrom(current)) {
            @Suppress("UNCHECKED_CAST")
            registry[current as Class<out Setting>]?.let { return it }
            current = current.superclass
        }

        return null
    }
}
