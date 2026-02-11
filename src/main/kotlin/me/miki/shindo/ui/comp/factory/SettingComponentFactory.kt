package me.miki.shindo.ui.comp.factory

import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.*
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.buttons.CompToggleButton
import me.miki.shindo.ui.comp.inputs.*
import java.util.concurrent.ConcurrentHashMap
object SettingComponentFactory {
    private val registry: MutableMap<Class<out Setting>, (Setting) -> Comp> = ConcurrentHashMap()
    private val componentCache: MutableMap<Setting, Comp> = ConcurrentHashMap()

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
            CompCellGrid(270f, 160f, setting)
        }
    }
    @JvmStatic
    fun <T : Setting> register(type: Class<T>, factory: (T) -> Comp) {
        @Suppress("UNCHECKED_CAST")
        registry[type] = factory as (Setting) -> Comp
    }
    @JvmStatic
    fun create(setting: Setting): Comp? {

        componentCache[setting]?.let { return it }

        val settingClass = setting.javaClass
        val factory = findFactory(settingClass)
                ?: return null

        val component = factory(setting)
        componentCache[setting] = component
        return component
    }
    private fun findFactory(settingClass: Class<out Setting>): ((Setting) -> Comp)? {

        registry[settingClass]?.let { return it }

        var current: Class<*>? = settingClass
        while (current != null && Setting::class.java.isAssignableFrom(current)) {
            @Suppress("UNCHECKED_CAST")
            registry[current as Class<out Setting>]?.let { return it }
            current = current.superclass
        }

        return null
    }
    @JvmStatic
    fun clearCache() {
        componentCache.clear()
    }
    @JvmStatic
    fun invalidateCache(setting: Setting) {
        componentCache.remove(setting)
    }
    @JvmStatic
    fun getRegisteredFactoryCount(): Int = registry.size
}