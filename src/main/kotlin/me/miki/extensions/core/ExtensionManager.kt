package me.miki.extensions.core

import me.miki.shindo.logger.ShindoLogger

class ExtensionManager {
    private val modules: MutableMap<String, ExtensionModule> = linkedMapOf()
    private val loggerTag = "ExtensionManager"

    @Synchronized
    fun register(module: ExtensionModule): ExtensionModule {
        val existing = modules[module.id]
        if (existing != null) {
            ShindoLogger.warn("[${loggerTag}] Replacing already registered extension ${module.id}")
        }
        modules[module.id] = module
        return module
    }

    fun register(action: ExtensionModuleBuilder.() -> Unit): ExtensionModule {
        val builder = ExtensionModuleBuilder().apply(action)
        return register(builder.build())
    }

    fun find(id: String): ExtensionModule? = synchronized(modules) { modules[id] }

    fun all(): List<ExtensionModule> = synchronized(modules) { modules.values.toList() }

    fun namespace(namespace: String): List<ExtensionModule> = synchronized(modules) {
        modules.values.filter { it.namespace == namespace }
    }

    fun isRegistered(id: String): Boolean = synchronized(modules) { modules.containsKey(id) }

    @Synchronized
    fun clear() {
        modules.clear()
    }
}
