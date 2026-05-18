package me.miki.shindo.management.settings.config

import me.miki.shindo.management.language.TranslateText

const val PROPERTY_SENTINEL_DOUBLE = -1.0

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Property(
    val type: PropertyType,
    val translate: TranslateText = TranslateText.NONE,
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val hidden: Boolean = false,
    val min: Double = PROPERTY_SENTINEL_DOUBLE,
    val max: Double = PROPERTY_SENTINEL_DOUBLE,
    val step: Double = PROPERTY_SENTINEL_DOUBLE,
    val current: Double = PROPERTY_SENTINEL_DOUBLE,
    val color: Int = Int.MIN_VALUE,
    val showAlpha: Boolean = false,
    val keyCode: Int = Int.MIN_VALUE,
    val text: String = "",
    val enumName: String = "",
    val key: String = "",
)
