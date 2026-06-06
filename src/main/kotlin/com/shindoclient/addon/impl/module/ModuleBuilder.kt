package com.shindoclient.addon.impl.module

import com.shindoclient.addon.ShindoDsl
import com.shindoclient.addon.impl.module.data.Module

@ShindoDsl
class ModuleBuilder(
    val key: String,
) {
    var name: String = key
    var description: String = ""
    private val settings = mutableListOf<SettingBuilder>()
    private val subModules = mutableListOf<ModuleBuilder>()

    fun setting(
        name: String,
        block: SettingBuilder.() -> Unit,
    ) {
        SettingBuilder(name).apply(block).let { settings.add(it) }
    }

    fun subModule(
        key: String,
        block: ModuleBuilder.() -> Unit,
    ) {
        ModuleBuilder(key).apply(block).let { subModules.add(it) }
    }

    fun build(): Module = Module(key, name, description, settings.map { it.build() }, subModules.map { it.build() })
}
