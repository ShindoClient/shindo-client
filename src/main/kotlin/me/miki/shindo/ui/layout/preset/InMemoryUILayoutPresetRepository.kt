package me.miki.shindo.ui.layout.preset

import me.miki.shindo.ui.layout.interfaces.UILayoutPresetRepository
import me.miki.shindo.ui.layout.model.UILayoutPreset
import java.util.LinkedHashMap

/**
 * In-memory repository with built-in presets and custom extension support.
 */
class InMemoryUILayoutPresetRepository(
    builtInPresets: List<UILayoutPreset>
) : UILayoutPresetRepository {

    private val builtIn = LinkedHashMap<String, UILayoutPreset>()
    private val custom = LinkedHashMap<String, UILayoutPreset>()

    init {
        for (preset in builtInPresets) {
            builtIn[preset.id] = preset.copy(userDefined = false)
        }
    }

    override fun getPresets(): List<UILayoutPreset> {
        val result = ArrayList<UILayoutPreset>(builtIn.size + custom.size)
        result.addAll(builtIn.values)
        result.addAll(custom.values)
        return result
    }

    override fun getPresetById(id: String): UILayoutPreset? {
        return builtIn[id] ?: custom[id]
    }

    override fun saveCustomPreset(preset: UILayoutPreset) {
        custom[preset.id] = preset.copy(userDefined = true)
    }

    override fun removeCustomPreset(id: String): Boolean {
        return custom.remove(id) != null
    }
}

