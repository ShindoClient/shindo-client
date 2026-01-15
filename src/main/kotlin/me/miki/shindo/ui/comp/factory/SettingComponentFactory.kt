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
import me.miki.shindo.ui.comp.inputs.CompCellGrid
import me.miki.shindo.ui.comp.inputs.CompColorPicker
import me.miki.shindo.ui.comp.inputs.CompComboBox
import me.miki.shindo.ui.comp.inputs.CompImageSelect
import me.miki.shindo.ui.comp.inputs.CompKeybind
import me.miki.shindo.ui.comp.inputs.CompModTextBox
import me.miki.shindo.ui.comp.inputs.CompSlider
import me.miki.shindo.ui.comp.inputs.CompSoundSelect
import me.miki.shindo.ui.comp.buttons.CompToggleButton
import java.util.concurrent.ConcurrentHashMap

/**
 * Factory otimizado que converte instâncias de [Setting] em componentes UI.
 * 
 * Melhorias de performance:
 * - Cache de componentes criados
 * - Type-safe factory functions
 * - Registro otimizado com lookup direto
 */
object SettingComponentFactory {
    private val registry: MutableMap<Class<out Setting>, (Setting) -> Comp> = ConcurrentHashMap()
    private val componentCache: MutableMap<Setting, Comp> = ConcurrentHashMap()

    init {
        registerDefaultFactories()
    }

    private fun registerDefaultFactories() {
        register(BooleanSetting::class.java) { setting ->
            CompToggleButton(setting as BooleanSetting)
        }
        register(NumberSetting::class.java) { setting ->
            CompSlider(setting as NumberSetting)
        }
        register(ComboSetting::class.java) { setting ->
            CompComboBox(140f, setting as ComboSetting)
        }
        register(KeybindSetting::class.java) { setting ->
            CompKeybind(120f, setting as KeybindSetting)
        }
        register(TextSetting::class.java) { setting ->
            CompModTextBox(setting as TextSetting)
        }
        register(ColorSetting::class.java) { setting ->
            CompColorPicker(setting as ColorSetting)
        }
        register(ImageSetting::class.java) { setting ->
            CompImageSelect(setting as ImageSetting)
        }
        register(SoundSetting::class.java) { setting ->
            CompSoundSelect(setting as SoundSetting)
        }
        register(CellGridSetting::class.java) { setting ->
            CompCellGrid(270f, 160f, setting as CellGridSetting)
        }
    }

    /**
     * Registra uma factory para um tipo específico de Setting.
     * @param type Classe do tipo de Setting
     * @param factory Função que cria o componente a partir do Setting
     */
    @JvmStatic
    fun <T : Setting> register(type: Class<T>, factory: (T) -> Comp) {
        @Suppress("UNCHECKED_CAST")
        registry[type] = factory as (Setting) -> Comp
    }

    /**
     * Cria um componente para o Setting fornecido.
     * Usa cache para evitar recriação desnecessária.
     * 
     * @param setting O Setting para criar o componente
     * @return O componente criado ou null se não houver factory registrada
     */
    @JvmStatic
    fun create(setting: Setting): Comp? {
        // Verifica cache primeiro
        componentCache[setting]?.let { return it }

        // Procura factory mais específica (subclasses primeiro)
        val settingClass = setting.javaClass
        val factory = findFactory(settingClass)
            ?: return null

        // Cria componente e armazena no cache
        val component = factory(setting)
        componentCache[setting] = component
        return component
    }

    /**
     * Encontra a factory mais específica para a classe fornecida.
     * Procura na hierarquia de classes para encontrar o melhor match.
     */
    private fun findFactory(settingClass: Class<out Setting>): ((Setting) -> Comp)? {
        // Tenta lookup direto primeiro (mais rápido)
        registry[settingClass]?.let { return it }

        // Procura na hierarquia de classes
        var current: Class<*>? = settingClass
        while (current != null && Setting::class.java.isAssignableFrom(current)) {
            @Suppress("UNCHECKED_CAST")
            registry[current as Class<out Setting>]?.let { return it }
            current = current.superclass
        }

        return null
    }

    /**
     * Limpa o cache de componentes.
     * Útil quando settings são modificados e componentes precisam ser recriados.
     */
    @JvmStatic
    fun clearCache() {
        componentCache.clear()
    }

    /**
     * Remove um componente específico do cache.
     */
    @JvmStatic
    fun invalidateCache(setting: Setting) {
        componentCache.remove(setting)
    }

    /**
     * Retorna o número de factories registradas.
     */
    @JvmStatic
    fun getRegisteredFactoryCount(): Int = registry.size
}