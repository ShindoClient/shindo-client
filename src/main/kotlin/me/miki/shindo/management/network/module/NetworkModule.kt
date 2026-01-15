package me.miki.shindo.management.network.module

/**
 * Interface base para módulos do sistema de network.
 * Cada módulo é responsável por uma funcionalidade específica.
 */
interface NetworkModule {
    /**
     * Nome do módulo para identificação.
     */
    val name: String

    /**
     * Inicializa o módulo.
     */
    fun initialize()

    /**
     * Atualiza o módulo (chamado periodicamente).
     */
    fun update()

    /**
     * Limpa recursos do módulo.
     */
    fun cleanup()
}

/**
 * Módulo que requer configuração.
 */
interface ConfigurableNetworkModule<T> : NetworkModule {
    /**
     * Aplica uma nova configuração ao módulo.
     */
    fun applyConfig(config: T)
}

/**
 * Módulo que pode ser habilitado/desabilitado.
 */
interface ToggleableNetworkModule : NetworkModule {
    var enabled: Boolean
}
