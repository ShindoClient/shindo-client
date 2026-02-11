package me.miki.shindo.ui.comp.layout.settingspanel

import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.buttons.CompToggleButton
import me.miki.shindo.ui.comp.inputs.CompCellGrid
import me.miki.shindo.ui.comp.inputs.CompColorPicker
import me.miki.shindo.ui.comp.inputs.CompComboBox
import me.miki.shindo.ui.comp.inputs.CompImageSelect
import me.miki.shindo.ui.comp.inputs.CompKeybind
import me.miki.shindo.ui.comp.inputs.CompModTextBox
import me.miki.shindo.ui.comp.inputs.CompSlider
import me.miki.shindo.ui.comp.inputs.CompSoundSelect
import kotlin.math.max
import kotlin.math.min

data class ComponentLayoutContext(
    val x: Float,
    val y: Float,
    val width: Float,
    val rowHeight: Float,
    val compact: Boolean,
    val style: SettingsPanelStyle
)

data class ComponentPlacement(val controlLeft: Float = Float.NaN)

interface ComponentLayoutDelegate {
    fun canHandle(comp: Comp): Boolean
    fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float
    fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement
}

class ComponentLayoutRegistry(private val delegates: List<ComponentLayoutDelegate>) {

    private val fallback = object : ComponentLayoutDelegate {
        override fun canHandle(comp: Comp): Boolean = true

        override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
            return if (compact) style.minRowHeightCompact else style.minRowHeightComfortable
        }

        override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
            val right = context.x + context.width
            val controlWidth = max(80f, min(180f, context.width * if (context.compact) 0.88f else 0.45f))
            val controlX = if (context.compact) {
                context.x + context.style.componentPadding
            } else {
                right - controlWidth - context.style.componentPadding
            }
            val controlY = if (context.compact) context.y + 26f else context.y + context.style.componentPadding + 2f
            comp.setWidth(controlWidth)
            comp.setX(controlX)
            comp.setY(controlY)
            return ComponentPlacement(controlX)
        }
    }

    fun resolve(comp: Comp): ComponentLayoutDelegate {
        for (delegate in delegates) {
            if (delegate.canHandle(comp)) {
                return delegate
            }
        }
        return fallback
    }

    companion object {
        fun createDefault(): ComponentLayoutRegistry {
            return ComponentLayoutRegistry(
                listOf(
                    ToggleDelegate(),
                    SliderDelegate(),
                    ComboDelegate(),
                    KeybindDelegate(),
                    TextBoxDelegate(),
                    ImageDelegate(),
                    SoundDelegate(),
                    ColorPickerDelegate(),
                    CellGridDelegate()
                )
            )
        }
    }
}

private class ToggleDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompToggleButton

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 52f else 38f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompToggleButton
        val right = context.x + context.width
        val scale = if (context.compact) 0.8f else 0.85f
        comp.setScale(scale)
        val x = right - 54f
        val y = if (context.compact) context.y + 27f else context.y + context.style.componentPadding - 1f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(x)
    }
}

private class SliderDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompSlider

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 66f else 50f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompSlider
        val right = context.x + context.width
        if (context.compact) {
            comp.setWidth(max(100f, context.width - (context.style.componentPadding * 2f)))
            comp.setX(context.x + context.style.componentPadding)
            comp.setY(context.y + 33f)
            return ComponentPlacement(Float.NaN)
        }

        comp.setWidth(max(110f, context.width - 200f))
        val x = right - comp.getWidth() - context.style.componentPadding
        comp.setX(x)
        comp.setY(context.y + context.style.componentPadding + 2f)
        return ComponentPlacement(x)
    }
}

private class ComboDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompComboBox

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 64f else 50f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompComboBox
        val right = context.x + context.width
        if (context.compact) {
            val comboWidth = max(110f, context.width - (context.style.componentPadding * 2f) - 6f)
            comp.setWidth(comboWidth)
            comp.setX(context.x + context.style.componentPadding)
            comp.setY(context.y + 30f)
            return ComponentPlacement(Float.NaN)
        }

        val comboWidth = max(110f, min(context.width - 48f, context.width - 180f))
        val x = max(context.x + context.style.componentPadding, right - comboWidth - context.style.componentPadding)
        comp.setWidth(comboWidth)
        comp.setX(x)
        comp.setY(context.y + context.style.componentPadding)
        return ComponentPlacement(x)
    }
}

private class KeybindDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompKeybind

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 56f else 38f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompKeybind
        val right = context.x + context.width
        val x = if (context.compact) context.x + context.style.componentPadding else right - 130f
        val y = if (context.compact) context.y + 28f else context.y + context.style.componentPadding + 2f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.compact) Float.NaN else x)
    }
}

private class TextBoxDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompModTextBox

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 56f else 38f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompModTextBox
        val right = context.x + context.width
        val width = if (context.compact) {
            max(120f, context.width - (context.style.componentPadding * 2f))
        } else {
            min(max(120f, context.width - 160f), 180f)
        }
        val x = if (context.compact) context.x + context.style.componentPadding else right - width - context.style.componentPadding
        val y = if (context.compact) context.y + 28f else context.y + context.style.componentPadding + 2f
        comp.setWidth(width)
        comp.setHeight(18f)
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.compact) Float.NaN else x)
    }
}

private class ImageDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompImageSelect

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 56f else 38f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompImageSelect
        val right = context.x + context.width
        val x = if (context.compact) context.x + context.style.componentPadding else right - 120f
        val y = if (context.compact) context.y + 28f else context.y + context.style.componentPadding + 2f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.compact) Float.NaN else x)
    }
}

private class SoundDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompSoundSelect

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 56f else 38f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompSoundSelect
        val right = context.x + context.width
        val x = if (context.compact) context.x + context.style.componentPadding else right - 120f
        val y = if (context.compact) context.y + 28f else context.y + context.style.componentPadding + 2f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.compact) Float.NaN else x)
    }
}

private class ColorPickerDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompColorPicker

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        comp as CompColorPicker
        val scale = comp.getScale().coerceAtLeast(0.6f)
        if (!comp.isOpen()) {
            return if (compact) 58f else max(38f, (30f * scale) + style.componentPadding)
        }

        val pickerBody = if (comp.isShowAlpha()) 118f else 100f
        val base = max(38f, style.componentPadding + (26f + pickerBody) * scale + 14f)
        return if (compact) base + 16f else base
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompColorPicker
        val right = context.x + context.width
        if (context.compact) {
            val compactScale = max(0.6f, min(0.95f, context.width / 220f))
            comp.setScale(compactScale)
            comp.setX(context.x + context.style.componentPadding)
            comp.setY(context.y + 28f)
            return ComponentPlacement(Float.NaN)
        }

        val scale = max(0.6f, min(1.0f, context.width / 180f))
        comp.setScale(scale)
        val pickerWidth = 118f * scale
        val x = max(context.x + context.style.componentPadding, right - pickerWidth - context.style.componentPadding)
        comp.setX(x)
        comp.setY(context.y + context.style.componentPadding)
        return ComponentPlacement(x)
    }
}

private class CellGridDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompCellGrid

    override fun targetHeight(comp: Comp, compact: Boolean, style: SettingsPanelStyle): Float {
        return if (compact) 360f else 340f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompCellGrid
        comp.setWidth(max(120f, context.width - 24f))
        comp.setHeight(max(180f, context.rowHeight - if (context.compact) 50f else 40f))
        comp.setX(context.x + 12f)
        comp.setY(context.y + if (context.compact) 16f else context.style.componentPadding + 6f)
        return ComponentPlacement(Float.NaN)
    }
}
