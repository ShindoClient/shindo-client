package me.miki.dsl.impl.module.data

data class Setting(
    val key: String,
    val default: Any?,
    val min: Double? = null,
    val max: Double? = null,
)
