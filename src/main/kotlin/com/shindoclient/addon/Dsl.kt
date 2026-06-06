package com.shindoclient.addon

import com.shindoclient.addon.impl.module.ModuleBuilder
import com.shindoclient.addon.impl.module.data.Module

fun module(
    key: String,
    block: ModuleBuilder.() -> Unit,
): Module = ModuleBuilder(key).apply(block).build()

fun registerModules(vararg modules: Module) {
    modules.forEach { println("[DSL] registered module: ${it.name}") }
}
