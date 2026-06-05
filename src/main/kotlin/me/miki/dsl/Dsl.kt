package me.miki.dsl

import me.miki.dsl.impl.module.ModuleBuilder
import me.miki.dsl.impl.module.data.Module

fun module(
    key: String,
    block: ModuleBuilder.() -> Unit,
): Module = ModuleBuilder(key).apply(block).build()

fun registerModules(vararg modules: Module) {
    modules.forEach { println("[DSL] registered module: ${it.name}") }
}
