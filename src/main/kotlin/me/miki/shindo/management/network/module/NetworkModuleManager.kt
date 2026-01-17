package me.miki.shindo.management.network.module

import me.miki.shindo.logger.ShindoLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * Gerenciador central de módulos de network.
 * Registra, inicializa e coordena todos os módulos.
 */
class NetworkModuleManager {
    val modules: MutableMap<String, NetworkModule> = ConcurrentHashMap()
    private var initialized = false

    /**
     * Registra um módulo.
     */
    fun register(module: NetworkModule) {
        if (modules.containsKey(module.name)) {
            ShindoLogger.warn("Module ${module.name} is already registered, replacing...")
        }
        modules[module.name] = module
        
        if (initialized) {
            try {
                module.initialize()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to initialize module ${module.name}", e)
            }
        }
    }

    /**
     * Remove um módulo.
     */
    fun unregister(name: String) {
        modules[name]?.let { module ->
            try {
                module.cleanup()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to cleanup module $name", e)
            }
            modules.remove(name)
        }
    }

    /**
     * Obtém um módulo por nome.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : NetworkModule> getModule(name: String): T? {
        return modules[name] as? T
    }

    /**
     * Obtém todos os módulos de um tipo específico.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : NetworkModule> getModulesOfType(): List<T> {
        return modules.values.filterIsInstance<T>()
    }

    /**
     * Inicializa todos os módulos registrados.
     */
    fun initializeAll() {
        if (initialized) return
        
        modules.values.forEach { module ->
            try {
                module.initialize()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to initialize module ${module.name}", e)
            }
        }
        initialized = true
    }

    /**
     * Atualiza todos os módulos.
     */
    fun updateAll() {
        modules.values.forEach { module ->
            try {
                module.update()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to update module ${module.name}", e)
            }
        }
    }

    /**
     * Limpa todos os módulos.
     */
    fun cleanupAll() {
        modules.values.forEach { module ->
            try {
                module.cleanup()
            } catch (e: Exception) {
                ShindoLogger.error("Failed to cleanup module ${module.name}", e)
            }
        }
        modules.clear()
        initialized = false
    }

    /**
     * Retorna todos os módulos registrados.
     */
    fun getAllModules(): Collection<NetworkModule> = modules.values

    /**
     * Verifica se um módulo está registrado.
     */
    fun hasModule(name: String): Boolean = modules.containsKey(name)
}
