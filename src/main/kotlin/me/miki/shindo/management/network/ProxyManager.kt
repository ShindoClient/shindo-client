package me.miki.shindo.management.network

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.network.proxy.CustomProxy
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Gerenciador de proxies DNS customizados
 */
class ProxyManager {

    private val customProxies = CopyOnWriteArrayList<CustomProxy>()
    private var activeProxyId: String? = null

    /**
     * Obtém todos os proxies customizados
     */
    fun getCustomProxies(): List<CustomProxy> {
        return customProxies.toList()
    }

    /**
     * Adiciona um novo proxy customizado
     */
    fun addProxy(proxy: CustomProxy): Boolean {
        if (!proxy.isValid()) {
            ShindoLogger.warn("[ProxyManager] Attempted to add invalid proxy: ${proxy.name}")
            return false
        }

        // Verifica se já existe um proxy com o mesmo nome
        if (customProxies.any { it.name.equals(proxy.name, ignoreCase = true) && it.id != proxy.id }) {
            ShindoLogger.warn("[ProxyManager] Proxy with name '${proxy.name}' already exists")
            return false
        }

        customProxies.add(proxy)
        ShindoLogger.info("[ProxyManager] Added custom proxy: ${proxy.name} (${proxy.primaryDNS})")
        return true
    }

    /**
     * Remove um proxy customizado
     */
    fun removeProxy(proxyId: String): Boolean {
        val proxy = customProxies.find { it.id == proxyId }
        if (proxy == null) {
            ShindoLogger.warn("[ProxyManager] Proxy with ID '$proxyId' not found")
            return false
        }

        // Se o proxy está ativo, desativa antes de remover
        if (activeProxyId == proxyId) {
            proxy.disable()
            activeProxyId = null
        }

        customProxies.remove(proxy)
        ShindoLogger.info("[ProxyManager] Removed custom proxy: ${proxy.name}")
        return true
    }

    /**
     * Atualiza um proxy existente
     */
    fun updateProxy(proxyId: String, name: String, primaryDNS: String, secondaryDNS: String?): Boolean {
        val proxy = customProxies.find { it.id == proxyId }
        if (proxy == null) {
            ShindoLogger.warn("[ProxyManager] Proxy with ID '$proxyId' not found")
            return false
        }

        val wasActive = activeProxyId == proxyId
        if (wasActive) {
            proxy.disable()
        }

        // Verifica se o novo nome já existe em outro proxy
        if (customProxies.any { it.name.equals(name, ignoreCase = true) && it.id != proxyId }) {
            ShindoLogger.warn("[ProxyManager] Proxy with name '$name' already exists")
            if (wasActive) {
                proxy.enable()
            }
            return false
        }

        val updatedProxy = proxy.copy(name = name, primaryDNS = primaryDNS, secondaryDNS = secondaryDNS)
        if (!updatedProxy.isValid()) {
            ShindoLogger.warn("[ProxyManager] Updated proxy is invalid")
            if (wasActive) {
                proxy.enable()
            }
            return false
        }

        val index = customProxies.indexOf(proxy)
        customProxies[index] = updatedProxy

        if (wasActive) {
            updatedProxy.enable()
        }

        ShindoLogger.info("[ProxyManager] Updated custom proxy: ${updatedProxy.name}")
        return true
    }

    /**
     * Obtém um proxy por ID
     */
    fun getProxyById(proxyId: String): CustomProxy? {
        return customProxies.find { it.id == proxyId }
    }

    /**
     * Obtém o proxy ativo
     */
    fun getActiveProxy(): CustomProxy? {
        return activeProxyId?.let { id -> customProxies.find { it.id == id } }
    }

    /**
     * Define o proxy ativo
     */
    fun setActiveProxy(proxyId: String?): Boolean {
        // Desativa o proxy atual se houver
        activeProxyId?.let { currentId ->
            customProxies.find { it.id == currentId }?.disable()
        }

        // Ativa o novo proxy se fornecido
        activeProxyId = proxyId?.let { newId ->
            val proxy = customProxies.find { it.id == newId }
            if (proxy != null) {
                proxy.enable()
                newId
            } else {
                ShindoLogger.warn("[ProxyManager] Proxy with ID '$newId' not found")
                null
            }
        }

        return true
    }

    /**
     * Verifica se há um proxy ativo
     */
    fun hasActiveProxy(): Boolean {
        return activeProxyId != null && getActiveProxy()?.isActive() == true
    }

    /**
     * Limpa todos os proxies customizados
     */
    fun clear() {
        activeProxyId?.let { currentId ->
            customProxies.find { it.id == currentId }?.disable()
        }
        activeProxyId = null
        customProxies.clear()
        ShindoLogger.info("[ProxyManager] Cleared all custom proxies")
    }
}
