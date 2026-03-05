package me.miki.shindo.ui.comp.layout.settingspanel

import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.buttons.CompToggleButton
import me.miki.shindo.ui.comp.inputs.*
import kotlin.math.max
import kotlin.math.min

data class ComponentLayoutContext(
    val x: Float,
    val y: Float,
    val width: Float,
    val rowHeight: Float,
    val narrow: Boolean,
    val style: SettingsPanelStyle
)

data class ComponentPlacement(val controlLeft: Float = Float.NaN)

interface ComponentLayoutDelegate {
    fun canHandle(comp: Comp): Boolean
    fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float
    fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement
    fun preferFullWidth(comp: Comp): Boolean = false
}

class ComponentLayoutRegistry(private val delegates: List<ComponentLayoutDelegate>) {

    private val fallback = object : ComponentLayoutDelegate {
        override fun canHandle(comp: Comp): Boolean = true

        override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
            return if (narrow) style.minRowHeightNarrow else style.minRowHeightDefault
        }

        override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
            val right = context.x + context.width
            val controlWidth = max(100f, min(200f, context.width * if (context.narrow) 0.92f else 0.45f))
            val controlX = if (context.narrow) {
                context.x + context.style.componentPadding
            } else {
                right - controlWidth - context.style.componentPadding
            }
            val controlY = if (context.narrow) context.y + 30f else context.y + context.style.componentPadding + 2f
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

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 56f else 42f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompToggleButton
        val right = context.x + context.width
        val scale = if (context.narrow) 0.82f else 0.9f
        comp.setScale(scale)
        val x = right - 54f
        val y = if (context.narrow) context.y + 30f else context.y + context.style.componentPadding + 1f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(x)
    }
}

private class SliderDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompSlider

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 70f else 54f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompSlider
        val right = context.x + context.width
        if (context.narrow) {
            comp.setWidth(max(100f, context.width - (context.style.componentPadding * 2f)))
            comp.setX(context.x + context.style.componentPadding + 2f)
            comp.setY(context.y + 35f)
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

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 64f else 46f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompComboBox
        val right = context.x + context.width
        if (context.narrow) {
            val comboWidth = max(110f, context.width - (context.style.componentPadding * 2f) - 6f)
            comp.setWidth(comboWidth - 2f)
            comp.setX(context.x + context.style.componentPadding + 2f)
            comp.setY(context.y + 30f)
            return ComponentPlacement(Float.NaN)
        }

        val comboWidth = max(120f, min(context.width - 48f, context.width - 182f))
        val x = max(context.x + context.style.componentPadding, right - comboWidth - context.style.componentPadding)
        comp.setWidth(comboWidth)
        comp.setX(x)
        comp.setY(context.y + context.style.componentPadding + 1f)
        return ComponentPlacement(x)
    }
}

private class KeybindDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompKeybind

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 58f else 40f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompKeybind
        val right = context.x + context.width
        val x = if (context.narrow) context.x + context.style.componentPadding + 2f else right - 136f
        val y = if (context.narrow) context.y + 30f else context.y + context.style.componentPadding + 2f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.narrow) Float.NaN else x)
    }
}

private class TextBoxDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompModTextBox

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 58f else 40f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompModTextBox
        val right = context.x + context.width
        val width = if (context.narrow) {
            max(120f, context.width - (context.style.componentPadding * 2f))
        } else {
            min(max(120f, context.width - 160f), 180f)
        }
        val x =
            if (context.narrow) context.x + context.style.componentPadding + 2f else right - width - context.style.componentPadding
        val y = if (context.narrow) context.y + 30f else context.y + context.style.componentPadding + 2f
        comp.setWidth(width)
        comp.setHeight(18f)
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.narrow) Float.NaN else x)
    }
}

private class ImageDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompImageSelect

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 58f else 40f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompImageSelect
        val right = context.x + context.width
        val x = if (context.narrow) context.x + context.style.componentPadding + 2f else right - 124f
        val y = if (context.narrow) context.y + 30f else context.y + context.style.componentPadding + 2f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.narrow) Float.NaN else x)
    }
}

private class SoundDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompSoundSelect

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        return if (narrow) 58f else 40f
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompSoundSelect
        val right = context.x + context.width
        val x = if (context.narrow) context.x + context.style.componentPadding + 2f else right - 124f
        val y = if (context.narrow) context.y + 30f else context.y + context.style.componentPadding + 2f
        comp.setX(x)
        comp.setY(y)
        return ComponentPlacement(if (context.narrow) Float.NaN else x)
    }
}

private class ColorPickerDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompColorPicker

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        comp as CompColorPicker
        val scale = comp.getScale().coerceAtLeast(0.6f)
        val layoutScale = (style.componentPadding / 12f).coerceIn(0.35f, 1.2f)
        if (!comp.isOpen()) {
            return if (narrow) 58f else max(38f, (30f * scale) + style.componentPadding)
        }

        val pickerBody = if (comp.isShowAlpha()) 118f else 100f
        // Reserve enough row height for the expanded picker visual area after the panel compact scaling.
        val visualHeight = max(38f, ((26f + pickerBody) * scale) + 14f)
        val reservedHeight = visualHeight / layoutScale
        return if (narrow) reservedHeight + (12f / layoutScale) else reservedHeight
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompColorPicker
        val right = context.x + context.width
        if (context.narrow) {
            val compactScale = max(0.6f, min(0.95f, context.width / 220f))
            comp.setScale(compactScale)
            comp.setX(context.x + context.style.componentPadding + 2f)
            comp.setY(context.y + 28f)
            return ComponentPlacement(Float.NaN)
        }

        val scale = max(0.6f, min(1.0f, context.width / 180f))
        comp.setScale(scale)
        val pickerWidth = 118f * scale
        val scrollbarInset = 10f
        val x = max(
            context.x + context.style.componentPadding,
            right - pickerWidth - context.style.componentPadding - scrollbarInset
        )
        comp.setX(x)
        comp.setY(context.y + context.style.componentPadding)
        return ComponentPlacement(x)
    }
}

private class CellGridDelegate : ComponentLayoutDelegate {
    override fun canHandle(comp: Comp): Boolean = comp is CompCellGrid

    override fun targetHeight(comp: Comp, narrow: Boolean, style: SettingsPanelStyle): Float {
        comp as CompCellGrid
        return comp.estimatePreferredHeight(narrow)
    }

    override fun place(comp: Comp, context: ComponentLayoutContext): ComponentPlacement {
        comp as CompCellGrid
        val horizontalInset = max(6f, context.style.componentPadding * 0.8f)
        val topInset = if (context.narrow) 18f else 16f
        val bottomInset = 6f
        comp.setWidth(max(160f, context.width - (horizontalInset * 2f)))
        comp.setHeight(max(136f, context.rowHeight - (topInset + bottomInset)))
        comp.setX(context.x + horizontalInset)
        comp.setY(context.y + topInset)
        return ComponentPlacement(Float.NaN)
    }

    override fun preferFullWidth(comp: Comp): Boolean = true
}
