package com.shindoclient.shindo.management.network

import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.network.proxy.CloudflareProxy
import com.shindoclient.shindo.management.network.utils.DNSConfig
import java.net.InetAddress

/**
 * Gerenciador do sistema de rede
 *
 * Gerencia proxies DNS e configurações de rede do cliente
 */
class NetworkManager {
    val proxyManager = ProxyManager()

    private var cloudflareProxy: CloudflareProxy? = null
    private var enabled = false
    private var activeProxyType: ProxyType = ProxyType.SYSTEM_DEFAULT

    enum class ProxyType {
        SYSTEM_DEFAULT,
        CLOUDFLARE,
        CUSTOM,
    }

    /**
     * Inicializa o NetworkManager
     */
    fun init() {
        ShindoLogger.info("[NetworkManager] Initializing network manager...")
        cloudflareProxy = CloudflareProxy(DNSConfig.CLOUDFLARE)
        ShindoLogger.info("[NetworkManager] Network manager initialized")
    }

    /**
     * Habilita o proxy DNS do Cloudflare
     */
    fun enableCloudflareProxy() {
        // Desativa qualquer proxy customizado ativo
        proxyManager.setActiveProxy(null)

        if (enabled && activeProxyType == ProxyType.CLOUDFLARE) {
            ShindoLogger.warn("[NetworkManager] Cloudflare proxy is already enabled")
            return
        }

        cloudflareProxy?.enable()
        enabled = true
        activeProxyType = ProxyType.CLOUDFLARE
        ShindoLogger.info("[NetworkManager] Cloudflare DNS proxy enabled")
    }

    /**
     * Desabilita o proxy DNS do Cloudflare
     */
    fun disableCloudflareProxy() {
        if (!enabled || activeProxyType != ProxyType.CLOUDFLARE) {
            return
        }

        cloudflareProxy?.disable()
        enabled = false
        activeProxyType = ProxyType.SYSTEM_DEFAULT
        ShindoLogger.info("[NetworkManager] Cloudflare DNS proxy disabled")
    }

    /**
     * Habilita um proxy customizado
     */
    fun enableCustomProxy(proxyId: String): Boolean {
        // Desativa Cloudflare se estiver ativo
        if (enabled && activeProxyType == ProxyType.CLOUDFLARE) {
            cloudflareProxy?.disable()
            enabled = false
        }

        val success = proxyManager.setActiveProxy(proxyId)
        if (success) {
            activeProxyType = ProxyType.CUSTOM
            enabled = true
            ShindoLogger.info("[NetworkManager] Custom proxy enabled: $proxyId")
        }
        return success
    }

    /**
     * Desabilita qualquer proxy ativo (volta para sistema padrão)
     */
    fun disableAllProxies() {
        if (enabled) {
            when (activeProxyType) {
                ProxyType.CLOUDFLARE -> {
                    cloudflareProxy?.disable()
                }

                ProxyType.CUSTOM -> {
                    proxyManager.setActiveProxy(null)
                }

                ProxyType.SYSTEM_DEFAULT -> {}
            }
            enabled = false
            activeProxyType = ProxyType.SYSTEM_DEFAULT
            ShindoLogger.info("[NetworkManager] All proxies disabled")
        }
    }

    /**
     * Verifica se algum proxy está ativo
     */
    fun isProxyEnabled(): Boolean =
        when (activeProxyType) {
            ProxyType.CLOUDFLARE -> enabled && (cloudflareProxy?.isActive() == true)
            ProxyType.CUSTOM -> enabled && proxyManager.hasActiveProxy()
            ProxyType.SYSTEM_DEFAULT -> false
        }

    /**
     * Verifica se o proxy Cloudflare está ativo
     */
    fun isCloudflareProxyEnabled(): Boolean = enabled && activeProxyType == ProxyType.CLOUDFLARE

    /**
     * Obtém o tipo de proxy ativo
     */
    fun getActiveProxyType(): ProxyType = activeProxyType

    /**
     * Obtém o ID do proxy customizado ativo (se houver)
     */
    fun getActiveCustomProxyId(): String? =
        if (activeProxyType == ProxyType.CUSTOM) {
            proxyManager.getActiveProxy()?.id
        } else {
            null
        }

    /**
     * Resolve um hostname usando o proxy DNS configurado
     */
    fun resolveHostname(hostname: String): InetAddress? =
        when (activeProxyType) {
            ProxyType.CLOUDFLARE -> cloudflareProxy?.resolve(hostname)
            ProxyType.CUSTOM -> proxyManager.getActiveProxy()?.resolve(hostname)
            ProxyType.SYSTEM_DEFAULT -> null
        }

    /**
     * Obtém informações sobre o DNS atual
     */
    fun getCurrentDNSInfo(): String =
        when (activeProxyType) {
            ProxyType.CLOUDFLARE -> {
                "Cloudflare (${cloudflareProxy?.getDNSAddress()})"
            }

            ProxyType.CUSTOM -> {
                val proxy = proxyManager.getActiveProxy()
                "${proxy?.name} (${proxy?.getDNSAddress()})"
            }

            ProxyType.SYSTEM_DEFAULT -> {
                "System Default"
            }
        }

    /**
     * Limpa recursos do NetworkManager
     */
    fun cleanup() {
        disableAllProxies()
        proxyManager.clear()
        cloudflareProxy = null
        ShindoLogger.info("[NetworkManager] Network manager cleaned up")
    }
}
