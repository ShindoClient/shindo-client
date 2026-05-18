package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner

open class NumberSetting : Setting {
    private val defaultValue: Double
    private val minValue: Double
    private val maxValue: Double
    private val integer: Boolean
    private var value: Double

    constructor(
        text: TranslateText,
        parent: ConfigOwner,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        integer: Boolean,
    ) : super(text, parent) {
        this.value = defaultValue
        this.defaultValue = defaultValue
        this.minValue = minValue
        this.maxValue = maxValue
        this.integer = integer
    }

    constructor(
        name: String,
        parent: ConfigOwner,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        integer: Boolean,
    ) : super(name, parent) {
        this.value = defaultValue
        this.defaultValue = defaultValue
        this.minValue = minValue
        this.maxValue = maxValue
        this.integer = integer
    }

    override fun reset() {
        value = defaultValue
    }

    fun getValue(): Double {
        if (integer) {
            value = value.toInt().toDouble()
        }
        return value
    }

    open fun setValue(value: Double) {
        var nextValue = value
        if (nextValue < getMinValue()) {
            nextValue = getMinValue()
        }
        if (nextValue > getMaxValue()) {
            nextValue = getMaxValue()
        }
        if (integer) {
            nextValue = Math.round(nextValue).toDouble()
        }
        this.value = nextValue
    }

    fun getValueInt(): Int {
        if (integer) {
            value = value.toInt().toDouble()
        }
        return value.toInt()
    }

    fun getValueFloat(): Float {
        if (integer) {
            value = value.toInt().toDouble()
        }
        return value.toFloat()
    }

    fun getValueLong(): Long {
        if (integer) {
            value = value.toInt().toDouble()
        }
        return value.toLong()
    }

    fun getMinValue(): Double {
        val meta = getMetadata()
        if (meta != null && !meta.min.isNaN()) {
            return meta.min
        }
        return minValue
    }

    fun getMaxValue(): Double {
        val meta = getMetadata()
        if (meta != null && !meta.max.isNaN()) {
            return meta.max
        }
        return maxValue
    }

    fun getDefaultValue(): Double = defaultValue

    fun getStep(): Double {
        val meta = getMetadata()
        if (meta != null && !meta.step.isNaN()) {
            return meta.step
        }
        return if (integer) 1.0 else 0.0
    }

    fun isInteger(): Boolean = integer
}
