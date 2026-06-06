package com.shindoclient.shindo.management.network.proxy

import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.network.interfaces.IDNSProxy
import com.shindoclient.shindo.management.network.utils.CloudflareDNSResolver
import com.shindoclient.shindo.management.network.utils.DNSConfig
import com.shindoclient.shindo.management.network.utils.DNSResolver
import java.net.InetAddress
import java.net.ProxySelector

/**
 * Proxy DNS usando Cloudflare (1.1.1.1)
 *
 * Este proxy configura o sistema para usar Cloudflare DNS para resolução de nomes.
 * Nota: Em Java/Minecraft, a configuração de DNS do sistema é limitada.
 * Este proxy funciona principalmente como um wrapper que usa Cloudflare DNS quando possível.
 */
class CloudflareProxy(
    private val config: DNSConfig = DNSConfig.CLOUDFLARE,
) : IDNSProxy {
    private var active = false
    private val cloudflareResolver = CloudflareDNSResolver(config)
    private val defaultResolver = DNSResolver()
    private val originalProxySelector: ProxySelector? = ProxySelector.getDefault()

    override fun isActive(): Boolean = active

    override fun enable() {
        if (active) {
            ShindoLogger.warn("[CloudflareProxy] Proxy is already active")
            return
        }

        try {
            ShindoLogger.info("[CloudflareProxy] Enabling Cloudflare DNS proxy (${config.primaryDNS})")

            // Em Java, não podemos alterar diretamente o DNS do sistema sem permissões especiais.
            // O que podemos fazer é configurar um ProxySelector customizado ou usar o resolvedor customizado.
            // Por enquanto, apenas marcamos como ativo e usamos o resolvedor Cloudflare quando necessário.

            active = true
            ShindoLogger.info("[CloudflareProxy] Cloudflare DNS proxy enabled successfully")
        } catch (e: Exception) {
            ShindoLogger.error("[CloudflareProxy] Failed to enable proxy", e)
            active = false
        }
    }

    override fun disable() {
        if (!active) {
            return
        }

        try {
            ShindoLogger.info("[CloudflareProxy] Disabling Cloudflare DNS proxy")

            // Restaura o proxy selector original se tivermos alterado
            if (originalProxySelector != null) {
                ProxySelector.setDefault(originalProxySelector)
            }

            active = false
            ShindoLogger.info("[CloudflareProxy] Cloudflare DNS proxy disabled successfully")
        } catch (e: Exception) {
            ShindoLogger.error("[CloudflareProxy] Failed to disable proxy", e)
        }
    }

    override fun resolve(hostname: String): InetAddress? =
        if (active) {
            // Usa o resolvedor Cloudflare quando ativo
            cloudflareResolver.resolve(hostname) ?: defaultResolver.resolve(hostname)
        } else {
            // Usa o resolvedor padrão quando inativo
            defaultResolver.resolve(hostname)
        }

    override fun getDNSName(): String = config.name

    override fun getDNSAddress(): String = config.primaryDNS
}
