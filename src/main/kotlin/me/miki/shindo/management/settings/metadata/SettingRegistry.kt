package me.miki.shindo.management.settings.metadata

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.SettingCategoryProvider
import me.miki.shindo.management.settings.impl.*
import me.miki.shindo.management.settings.impl.combo.Option
import java.awt.Color
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToLong

object SettingRegistry {

    private val PROPERTY_CACHE: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val PROPERTY_BINDINGS = ConcurrentHashMap<OwnerFieldKey, Setting>()
    private val CATEGORY_BINDINGS = ConcurrentHashMap<ConfigOwner, LinkedHashMap<String, CategorySetting>>()
    private val OWNER_BINDINGS = ConcurrentHashMap<ConfigOwner, CopyOnWriteArrayList<Setting>>()

    private fun registerOwnerSetting(owner: ConfigOwner?, setting: Setting?) {
        if (owner == null || setting == null) {
            return
        }
        val settings = OWNER_BINDINGS.computeIfAbsent(owner) { CopyOnWriteArrayList() }
        if (!settings.contains(setting)) {
            settings.add(setting)
        }
    }

    @JvmStatic
    fun applyMetadata(owner: ConfigOwner) {
        val processed = HashSet<Setting>()
        processPropertyFields(owner, processed)
        processSettingFields(owner, processed)
    }

    private fun processSettingFields(owner: ConfigOwner, processed: MutableSet<Setting>) {
        var type: Class<*>? = owner.javaClass

        while (type != null && type != Any::class.java) {
            for (field in type.declaredFields) {
                if (!Setting::class.java.isAssignableFrom(field.type)) {
                    continue
                }
                field.isAccessible = true
                try {
                    val setting = field.get(owner) as? Setting ?: continue
                    if (processed.contains(setting)) {
                        continue
                    }

                    val property = field.getAnnotation(Property::class.java)
                    if (property != null) {
                        ensureCategory(owner, property.category, processed)
                    }

                    val metadata = SettingMetadata(field.name)
                    if (property != null) {
                        if (property.key.isNotEmpty()) {
                            metadata.keyOverride = property.key
                        }
                        metadata.category = property.category
                        metadata.description = property.description
                        metadata.hidden = property.hidden
                        if (!property.min.isNaN()) {
                            metadata.min = property.min
                        }
                        if (!property.max.isNaN()) {
                            metadata.max = property.max
                        }
                        if (!property.step.isNaN()) {
                            metadata.step = property.step
                        }
                    }

                    setting.applyMetadata(metadata)
                    processed.add(setting)
                    registerOwnerSetting(owner, setting)
                } catch (ignored: IllegalAccessException) {
                }
            }
            type = type.superclass
        }
    }

    private fun processPropertyFields(owner: ConfigOwner, processed: MutableSet<Setting>) {
        var type: Class<*>? = owner.javaClass

        while (type != null && type != Any::class.java) {
            for (field in type.declaredFields) {
                val property = field.getAnnotation(Property::class.java) ?: continue

                val cacheKey = owner.javaClass.name + '#' + field.name
                if (!PROPERTY_CACHE.add(cacheKey)) {
                    continue
                }

                val setting = createSettingFromProperty(owner, field, property, processed)
                if (setting != null) {
                    PROPERTY_BINDINGS[OwnerFieldKey(owner, field.name)] = setting
                    processed.add(setting)
                    registerOwnerSetting(owner, setting)
                }
            }
            type = type.superclass
        }
    }

    @JvmStatic
    fun <T : Setting> getSetting(owner: ConfigOwner, fieldName: String, type: Class<T>): T? {
        val setting = PROPERTY_BINDINGS[OwnerFieldKey(owner, fieldName)] ?: return null
        if (!type.isInstance(setting)) {
            throw IllegalArgumentException("Property '" + fieldName + "' on " + owner.javaClass.simpleName + " is not of type " + type.simpleName)
        }
        @Suppress("UNCHECKED_CAST")
        return setting as T
    }

    @JvmStatic
    fun getBooleanSetting(owner: ConfigOwner, fieldName: String): BooleanSetting? {
        return getSetting(owner, fieldName, BooleanSetting::class.java)
    }

    @JvmStatic
    fun getNumberSetting(owner: ConfigOwner, fieldName: String): NumberSetting? {
        return getSetting(owner, fieldName, NumberSetting::class.java)
    }

    @JvmStatic
    fun getTextSetting(owner: ConfigOwner, fieldName: String): TextSetting? {
        return getSetting(owner, fieldName, TextSetting::class.java)
    }

    @JvmStatic
    fun getColorSetting(owner: ConfigOwner, fieldName: String): ColorSetting? {
        return getSetting(owner, fieldName, ColorSetting::class.java)
    }

    @JvmStatic
    fun getKeybindSetting(owner: ConfigOwner, fieldName: String): KeybindSetting? {
        return getSetting(owner, fieldName, KeybindSetting::class.java)
    }

    @JvmStatic
    fun getComboSetting(owner: ConfigOwner, fieldName: String): ComboSetting? {
        return getSetting(owner, fieldName, ComboSetting::class.java)
    }

    @JvmStatic
    fun getSettings(owner: ConfigOwner): List<Setting> {
        val settings = OWNER_BINDINGS[owner]
        if (settings == null || settings.isEmpty()) {
            return Collections.emptyList()
        }
        return ArrayList(settings)
    }

    private fun ensureCategory(
        owner: ConfigOwner,
        rawCategory: String?,
        processed: MutableSet<Setting>
    ): CategorySetting? {
        if (rawCategory == null) {
            return null
        }
        val category = rawCategory.trim()
        if (category.isEmpty()) {
            return null
        }
        val categories = CATEGORY_BINDINGS.computeIfAbsent(owner) { LinkedHashMap() }
        var categorySetting = categories[category]
        if (categorySetting == null) {
            categorySetting = createCategorySetting(owner, category)
            categories[category] = categorySetting
            processed.add(categorySetting)
            registerOwnerSetting(owner, categorySetting)
        }
        return categorySetting
    }

    private fun createCategorySetting(owner: ConfigOwner, category: String): CategorySetting {
        if (owner is SettingCategoryProvider) {
            val provider = owner
            val key = category.lowercase(Locale.ROOT)
            val label = provider.resolveCategoryLabel(key)
            val setting = if (label != null && label != TranslateText.NONE) {
                CategorySetting(label, owner)
            } else {
                CategorySetting(category, owner)
            }
            setting.setCollapsed(provider.isCategoryInitiallyCollapsed(key))
            return setting
        }
        val setting = CategorySetting(category, owner)
        setting.setCollapsed(false)
        return setting
    }

    private fun createSettingFromProperty(
        owner: ConfigOwner,
        field: Field,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting? {
        return try {
            val isStatic = Modifier.isStatic(field.modifiers)
            val target = if (isStatic) null else owner
            field.isAccessible = true

            when (property.type) {
                me.miki.shindo.management.settings.config.PropertyType.BOOLEAN -> createBooleanSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.NUMBER -> createNumberSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.TEXT -> createTextSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.COLOR -> createColorSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.KEYBIND -> createKeybindSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.IMAGE -> createImageSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.SOUND -> createSoundSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.COMBO -> createComboSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                me.miki.shindo.management.settings.config.PropertyType.CELL_GRID -> createCellGridSetting(
                    owner,
                    field,
                    target,
                    property,
                    processed
                )

                else -> {
                    ShindoLogger.warn("Property type " + property.type + " is not yet supported on field " + field.name)
                    null
                }
            }
        } catch (e: IllegalAccessException) {
            ShindoLogger.error("Failed to bind property field " + field.name, e)
            null
        }
    }

    private fun createBooleanSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val defaultValue = if (!property.current.isNaN()) {
            property.current != 0.0
        } else if (field.type == java.lang.Boolean.TYPE) {
            field.getBoolean(target)
        } else {
            val value = field.get(target)
            value is Boolean && value
        }

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundBooleanSetting(property.translate, owner, defaultValue, field, target)
        } else {
            BoundBooleanSetting(resolveName(property, field), owner, defaultValue, field, target)
        }

        applyMetadata(setting, property, field)
        setting.setToggled(defaultValue)
        return setting
    }

    private fun createNumberSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val integer = isIntegerType(field.type)
        var defaultValue = if (!property.current.isNaN()) property.current else readNumericField(field, target)
        if (defaultValue.isNaN()) {
            defaultValue = 0.0
        }
        var min = if (!property.min.isNaN()) property.min else kotlin.math.min(defaultValue, 0.0)
        var max = if (!property.max.isNaN()) property.max else kotlin.math.max(defaultValue, min + 1.0)
        if (max < min) {
            val tmp = max
            max = min
            min = tmp
        }
        defaultValue = clamp(defaultValue, min, max)

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundNumberSetting(property.translate, owner, defaultValue, min, max, integer, field, target)
        } else {
            BoundNumberSetting(resolveName(property, field), owner, defaultValue, min, max, integer, field, target)
        }

        val metadata = applyMetadata(setting, property, field)
        if (!property.step.isNaN()) {
            metadata.step = property.step
        }
        setting.setValue(defaultValue)
        return setting
    }

    private fun createTextSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        var defaultValue = if (property.text.isNotEmpty()) property.text else field.get(target) as? String
        if (defaultValue == null) {
            defaultValue = ""
        }

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundTextSetting(property.translate, owner, defaultValue, field, target)
        } else {
            BoundTextSetting(resolveName(property, field), owner, defaultValue, field, target)
        }

        applyMetadata(setting, property, field)
        setting.setText(defaultValue)
        return setting
    }

    private fun createColorSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val defaultColor = if (property.color != Int.MIN_VALUE) {
            Color(property.color, property.showAlpha)
        } else {
            val value = field.get(target) as? Color
            value ?: Color.WHITE
        }

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundColorSetting(property.translate, owner, defaultColor, property.showAlpha, field, target)
        } else {
            BoundColorSetting(resolveName(property, field), owner, defaultColor, property.showAlpha, field, target)
        }

        applyMetadata(setting, property, field)
        setting.setColor(defaultColor)
        return setting
    }

    private fun createKeybindSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val defaultKey = if (property.keyCode != Int.MIN_VALUE) property.keyCode else field.getInt(target)

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundKeybindSetting(property.translate, owner, defaultKey, field, target)
        } else {
            BoundKeybindSetting(resolveName(property, field), owner, defaultKey, field, target)
        }

        applyMetadata(setting, property, field)
        setting.setKeyCode(defaultKey)
        return setting
    }

    private fun createImageSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val defaultFile = field.get(target) as? File

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundImageSetting(property.translate, owner, defaultFile, field, target)
        } else {
            BoundImageSetting(resolveName(property, field), owner, defaultFile, field, target)
        }

        applyMetadata(setting, property, field)
        setting.setImage(defaultFile)
        return setting
    }

    private fun createSoundSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val defaultFile = field.get(target) as? File

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundSoundSetting(property.translate, owner, defaultFile, field, target)
        } else {
            BoundSoundSetting(resolveName(property, field), owner, defaultFile, field, target)
        }

        applyMetadata(setting, property, field)
        setting.setSound(defaultFile)
        return setting
    }

    private fun createComboSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting? {
        val rawType = field.type
        if (!rawType.isEnum) {
            ShindoLogger.warn("Property field " + field.name + " is marked as ENUM but does not use an enum type")
            return null
        }

        val constants = rawType.enumConstants
        if (constants == null || constants.isEmpty()) {
            ShindoLogger.warn("Enum property " + field.name + " on " + owner.javaClass.simpleName + " defines no constants")
            return null
        }

        ensureCategory(owner, property.category, processed)

        val options = ArrayList<Option>(constants.size)
        val mapping = LinkedHashMap<String, Enum<*>>()

        for (constantObj in constants) {
            val constant = constantObj as Enum<*>
            val option = buildEnumOption(constant)
            options.add(option)
            mapping[option.nameKey] = constant
        }

        var defaultValue = field.get(target) as? Enum<*>
        if (defaultValue == null) {
            defaultValue = constants[0] as Enum<*>
        }

        val defaultKey = ensureEnumOption(mapping, defaultValue)
        val setting = if (translate(property)) {
            BoundEnumSetting(property.translate, owner, defaultKey, options, mapping, field, target)
        } else {
            BoundEnumSetting(resolveName(property, field), owner, defaultKey, options, mapping, field, target)
        }

        applyMetadata(setting, property, field)
        setting.initialize()
        return setting
    }

    private fun createCellGridSetting(
        owner: ConfigOwner,
        field: Field,
        target: Any?,
        property: Property,
        processed: MutableSet<Setting>
    ): Setting {
        val defaultGrid = copyGrid(field.get(target) as? Array<BooleanArray>)

        ensureCategory(owner, property.category, processed)
        val setting = if (translate(property)) {
            BoundCellGridSetting(property.translate, owner, defaultGrid, field, target)
        } else {
            BoundCellGridSetting(resolveName(property, field), owner, defaultGrid, field, target)
        }

        applyMetadata(setting, property, field)
        setting.initialize()
        return setting
    }

    private fun applyMetadata(setting: Setting, property: Property, field: Field): SettingMetadata {
        val metadata = SettingMetadata(field.name)
        metadata.category = property.category
        metadata.description = property.description
        metadata.hidden = property.hidden
        if (property.key.isNotEmpty()) {
            metadata.keyOverride = property.key
        }
        if (!property.min.isNaN()) {
            metadata.min = property.min
        }
        if (!property.max.isNaN()) {
            metadata.max = property.max
        }
        if (!property.step.isNaN()) {
            metadata.step = property.step
        }
        setting.applyMetadata(metadata)
        return metadata
    }

    private fun buildEnumOption(constant: Enum<*>): Option {
        if (constant is PropertyEnum) {
            val translate = constant.getTranslate()
            if (translate != TranslateText.NONE) {
                return Option(translate)
            }
            return Option(constant.getDisplayName())
        }

        var display = constant.name.lowercase(Locale.ROOT).replace('_', ' ')
        if (display.isNotEmpty()) {
            display = display.substring(0, 1).uppercase(Locale.ROOT) + display.substring(1)
        }
        return Option(display.ifEmpty { constant.name })
    }

    private fun ensureEnumOption(mapping: LinkedHashMap<String, Enum<*>>, value: Enum<*>): String {
        for (entry in mapping.entries) {
            if (entry.value == value) {
                return entry.key
            }
        }
        return if (mapping.isEmpty()) "" else mapping.keys.iterator().next()
    }

    private fun copyGrid(source: Array<BooleanArray>?): Array<BooleanArray>? {
        if (source == null) {
            return null
        }
        val copy = Array(source.size) { BooleanArray(0) }
        for (i in source.indices) {
            val row = source[i]
            copy[i] = row.clone()
        }
        return copy
    }

    private fun translate(property: Property): Boolean {
        return property.translate != TranslateText.NONE
    }

    private fun resolveName(property: Property, field: Field): String {
        return property.name.ifEmpty {
            field.name
        }
    }

    private fun isIntegerType(type: Class<*>): Boolean {
        return type == Integer.TYPE || type == Integer::class.java
                || type == java.lang.Long.TYPE || type == java.lang.Long::class.java
                || type == java.lang.Short.TYPE || type == java.lang.Short::class.java
                || type == java.lang.Byte.TYPE || type == java.lang.Byte::class.java
    }

    private fun readNumericField(field: Field, target: Any?): Double {
        val type = field.type
        return when (type) {
            java.lang.Double.TYPE -> field.getDouble(target)
            java.lang.Double::class.java -> (field.get(target) as? Double) ?: Double.NaN
            java.lang.Float.TYPE -> field.getFloat(target).toDouble()
            java.lang.Float::class.java -> (field.get(target) as? Float)?.toDouble() ?: Double.NaN
            java.lang.Long.TYPE -> field.getLong(target).toDouble()
            java.lang.Long::class.java -> (field.get(target) as? Long)?.toDouble() ?: Double.NaN
            Integer.TYPE -> field.getInt(target).toDouble()
            Integer::class.java -> (field.get(target) as? Int)?.toDouble() ?: Double.NaN
            java.lang.Short.TYPE -> field.getShort(target).toDouble()
            java.lang.Short::class.java -> (field.get(target) as? Short)?.toDouble() ?: Double.NaN
            java.lang.Byte.TYPE -> field.getByte(target).toDouble()
            java.lang.Byte::class.java -> (field.get(target) as? Byte)?.toDouble() ?: Double.NaN
            else -> Double.NaN
        }
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return kotlin.math.max(min, kotlin.math.min(max, value))
    }

    private open class SettingBinding(val field: Field, val target: Any?) {
        fun handleException(e: Exception) {
            ShindoLogger.error("Failed to update property field " + field.name, e)
        }
    }

    private class BoundBooleanSetting : BooleanSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultValue: Boolean,
            field: Field,
            target: Any?
        ) : super(text, parent, defaultValue) {
            binding = SettingBinding(field, target)
        }

        constructor(name: String, parent: ConfigOwner, defaultValue: Boolean, field: Field, target: Any?) : super(
            name,
            parent,
            defaultValue
        ) {
            binding = SettingBinding(field, target)
        }

        override fun setToggled(toggle: Boolean) {
            super.setToggled(toggle)
            apply(toggle)
        }

        override fun reset() {
            super.reset()
            apply(isToggled())
        }

        private fun apply(value: Boolean) {
            try {
                val type = binding.field.type
                if (type == java.lang.Boolean.TYPE) {
                    binding.field.setBoolean(binding.target, value)
                } else {
                    binding.field.set(binding.target, value)
                }
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundNumberSetting : NumberSetting {
        private val binding: SettingBinding
        private val integer: Boolean

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultValue: Double,
            min: Double,
            max: Double,
            integer: Boolean,
            field: Field,
            target: Any?
        ) : super(text, parent, defaultValue, min, max, integer) {
            binding = SettingBinding(field, target)
            this.integer = integer
        }

        constructor(
            name: String,
            parent: ConfigOwner,
            defaultValue: Double,
            min: Double,
            max: Double,
            integer: Boolean,
            field: Field,
            target: Any?
        ) : super(name, parent, defaultValue, min, max, integer) {
            binding = SettingBinding(field, target)
            this.integer = integer
        }

        override fun setValue(value: Double) {
            super.setValue(value)
            apply(getValue())
        }

        override fun reset() {
            super.reset()
            apply(getValue())
        }

        private fun apply(value: Double) {
            try {
                if (integer) {
                    val rounded = value.roundToLong()
                    val type = binding.field.type
                    when (type) {
                        Integer.TYPE -> binding.field.setInt(binding.target, rounded.toInt())
                        Integer::class.java -> binding.field.set(binding.target, rounded.toInt())
                        java.lang.Long.TYPE -> binding.field.setLong(binding.target, rounded)
                        java.lang.Long::class.java -> binding.field.set(binding.target, rounded)
                        java.lang.Short.TYPE -> binding.field.setShort(binding.target, rounded.toShort())
                        java.lang.Short::class.java -> binding.field.set(binding.target, rounded.toShort())
                        java.lang.Byte.TYPE -> binding.field.setByte(binding.target, rounded.toByte())
                        java.lang.Byte::class.java -> binding.field.set(binding.target, rounded.toByte())
                    }
                } else {
                    val type = binding.field.type
                    when (type) {
                        java.lang.Double.TYPE -> binding.field.setDouble(binding.target, value)
                        java.lang.Double::class.java -> binding.field.set(binding.target, value)
                        java.lang.Float.TYPE -> binding.field.setFloat(binding.target, value.toFloat())
                        java.lang.Float::class.java -> binding.field.set(binding.target, value.toFloat())
                    }
                }
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundTextSetting : TextSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultText: String,
            field: Field,
            target: Any?
        ) : super(text, parent, defaultText) {
            binding = SettingBinding(field, target)
        }

        constructor(
            name: String,
            parent: ConfigOwner,
            defaultText: String,
            field: Field,
            target: Any?
        ) : super(name, parent, defaultText) {
            binding = SettingBinding(field, target)
        }

        override fun setText(text: String) {
            super.setText(text)
            apply(text)
        }

        override fun reset() {
            super.reset()
            apply(getText())
        }

        private fun apply(text: String) {
            try {
                binding.field.set(binding.target, text)
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundColorSetting : ColorSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            color: Color,
            showAlpha: Boolean,
            field: Field,
            target: Any?
        ) : super(text, parent, color, showAlpha) {
            binding = SettingBinding(field, target)
        }

        constructor(
            name: String,
            parent: ConfigOwner,
            color: Color,
            showAlpha: Boolean,
            field: Field,
            target: Any?
        ) : super(name, parent, color, showAlpha) {
            binding = SettingBinding(field, target)
        }

        override fun setColor(color: Color) {
            super.setColor(color)
            apply()
        }

        override fun setHue(hue: Float) {
            super.setHue(hue)
            apply()
        }

        override fun setSaturation(saturation: Float) {
            super.setSaturation(saturation)
            apply()
        }

        override fun setBrightness(brightness: Float) {
            super.setBrightness(brightness)
            apply()
        }

        override fun setAlpha(alpha: Int) {
            super.setAlpha(alpha)
            apply()
        }

        override fun reset() {
            super.reset()
            apply()
        }

        private fun apply() {
            try {
                binding.field.set(binding.target, getColor())
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundKeybindSetting : KeybindSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            keyCode: Int,
            field: Field,
            target: Any?
        ) : super(text, parent, keyCode) {
            binding = SettingBinding(field, target)
        }

        constructor(name: String, parent: ConfigOwner, keyCode: Int, field: Field, target: Any?) : super(
            name,
            parent,
            keyCode
        ) {
            binding = SettingBinding(field, target)
        }

        override fun setKeyCode(keyCode: Int) {
            super.setKeyCode(keyCode)
            try {
                val type = binding.field.type
                if (type == Integer.TYPE) {
                    binding.field.setInt(binding.target, keyCode)
                } else {
                    binding.field.set(binding.target, keyCode)
                }
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundImageSetting : ImageSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultFile: File?,
            field: Field,
            target: Any?
        ) : super(text, parent) {
            binding = SettingBinding(field, target)
            super.setImage(defaultFile)
        }

        constructor(name: String, parent: ConfigOwner, defaultFile: File?, field: Field, target: Any?) : super(
            name,
            parent
        ) {
            binding = SettingBinding(field, target)
            super.setImage(defaultFile)
        }

        override fun setImage(image: File?) {
            super.setImage(image)
            apply(image)
        }

        override fun reset() {
            super.reset()
            apply(getImage())
        }

        private fun apply(image: File?) {
            try {
                binding.field.set(binding.target, image)
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundSoundSetting : SoundSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultFile: File?,
            field: Field,
            target: Any?
        ) : super(text, parent) {
            binding = SettingBinding(field, target)
            super.setSound(defaultFile)
        }

        constructor(name: String, parent: ConfigOwner, defaultFile: File?, field: Field, target: Any?) : super(
            name,
            parent
        ) {
            binding = SettingBinding(field, target)
            super.setSound(defaultFile)
        }

        override fun setSound(sound: File?) {
            super.setSound(sound)
            apply(sound)
        }

        override fun reset() {
            super.reset()
            apply(getSound())
        }

        private fun apply(sound: File?) {
            try {
                binding.field.set(binding.target, sound)
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundEnumSetting : ComboSetting {
        private val binding: SettingBinding
        private val mapping: Map<String, Enum<*>>

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultKey: String,
            options: List<Option>,
            mapping: Map<String, Enum<*>>,
            field: Field,
            target: Any?
        ) : super(text, parent, defaultKey, options) {
            binding = SettingBinding(field, target)
            this.mapping = mapping
        }

        constructor(
            name: String,
            parent: ConfigOwner,
            defaultKey: String,
            options: List<Option>,
            mapping: Map<String, Enum<*>>,
            field: Field,
            target: Any?
        ) : super(name, parent, defaultKey, options) {
            binding = SettingBinding(field, target)
            this.mapping = mapping
        }

        fun initialize() {
            apply(getOption())
        }

        override fun setOption(option: Option?) {
            super.setOption(option)
            apply(option)
        }

        override fun reset() {
            super.reset()
            apply(getOption())
        }

        private fun apply(option: Option?) {
            var value = if (option != null) mapping[option.nameKey] else null
            if (value == null && mapping.isNotEmpty()) {
                value = mapping.values.iterator().next()
            }
            if (value == null) {
                return
            }
            try {
                binding.field.set(binding.target, value)
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }
    }

    private class BoundCellGridSetting : CellGridSetting {
        private val binding: SettingBinding

        constructor(
            text: TranslateText,
            parent: ConfigOwner,
            defaultGrid: Array<BooleanArray>?,
            field: Field,
            target: Any?
        ) : super(text, parent, copyGrid(defaultGrid)) {
            binding = SettingBinding(field, target)
        }

        constructor(
            name: String,
            parent: ConfigOwner,
            defaultGrid: Array<BooleanArray>?,
            field: Field,
            target: Any?
        ) : super(name, parent, copyGrid(defaultGrid)) {
            binding = SettingBinding(field, target)
        }

        fun initialize() {
            apply(getCells())
            notifyConsumer()
        }

        override fun setCells(cells: Array<BooleanArray>?) {
            super.setCells(copyGrid(cells))
            apply(getCells())
            notifyConsumer()
        }

        override fun reset() {
            super.reset()
            apply(getCells())
            notifyConsumer()
        }

        private fun apply(cells: Array<BooleanArray>?) {
            try {
                binding.field.set(binding.target, copyGrid(cells))
            } catch (e: IllegalAccessException) {
                binding.handleException(e)
            }
        }

        private fun notifyConsumer() {
            val target = binding.target
            if (target is CellGridSettingConsumer) {
                target.onCellGridAvailable(this)
            }
        }
    }

    private class OwnerFieldKey(private val owner: ConfigOwner, private val fieldName: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || other.javaClass != javaClass) {
                return false
            }
            val otherKey = other as OwnerFieldKey
            return owner === otherKey.owner && fieldName == otherKey.fieldName
        }

        override fun hashCode(): Int {
            var result = System.identityHashCode(owner)
            result = 31 * result + fieldName.hashCode()
            return result
        }
    }
}

