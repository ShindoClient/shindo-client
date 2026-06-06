package com.shindoclient.addon.impl.module

import com.shindoclient.addon.ShindoDsl
import com.shindoclient.addon.impl.module.data.Setting

@ShindoDsl
class SettingBuilder(
    val key: String,
) {
    var default: Any? = null
    var min: Double? = null
    var max: Double? = null

    fun build(): Setting = Setting(key, default, min, max)
}
