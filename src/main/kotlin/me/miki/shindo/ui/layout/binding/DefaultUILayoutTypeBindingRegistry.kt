package me.miki.shindo.ui.layout.binding

import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.ui.layout.interfaces.UILayoutTypeBinding
import me.miki.shindo.ui.layout.interfaces.UILayoutTypeBindingRegistry
import java.util.EnumMap

class DefaultUILayoutTypeBindingRegistry(
    bindings: List<UILayoutTypeBinding>
) : UILayoutTypeBindingRegistry {

    private val bindingMap = EnumMap<UILayoutType, UILayoutTypeBinding>(UILayoutType::class.java)

    init {
        for (binding in bindings) {
            bindingMap[binding.type] = binding
        }
    }

    override fun getTypes(area: UILayoutArea): List<UILayoutType> {
        val values = UILayoutType.values()
        val result = ArrayList<UILayoutType>(values.size)
        for (type in values) {
            if (type.area == area) {
                result.add(type)
            }
        }
        return result
    }

    override fun getSelectedType(area: UILayoutArea): UILayoutType? {
        val areaTypes = getTypes(area)
        for (type in areaTypes) {
            if (isSelected(type)) {
                return type
            }
        }
        return if (areaTypes.isEmpty()) null else areaTypes[0]
    }

    override fun selectType(type: UILayoutType?) {
        if (type == null) {
            return
        }
        bindingMap[type]?.applySelection()
    }

    override fun isSelected(type: UILayoutType): Boolean {
        return bindingMap[type]?.isSelected() == true
    }
}

