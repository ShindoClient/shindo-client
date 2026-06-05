package me.miki.dsl.impl.module.data

data class Module(
    val key: String,
    val name: String,
    val description: String,
    val settings: List<Setting>,
    val subModules: List<Module> = emptyList(),
)
