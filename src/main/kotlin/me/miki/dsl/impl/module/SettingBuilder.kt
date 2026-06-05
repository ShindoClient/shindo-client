package me.miki.dsl.impl.module

import me.miki.dsl.ShindoDsl
import me.miki.dsl.impl.module.data.Setting

@ShindoDsl
class SettingBuilder(
    val key: String,
) {
    var default: Any? = null
    var min: Double? = null
    var max: Double? = null

    fun build(): Setting = Setting(key, default, min, max)
}
