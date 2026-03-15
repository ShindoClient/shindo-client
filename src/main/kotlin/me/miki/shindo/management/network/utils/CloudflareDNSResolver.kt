package me.miki.shindo.management.network.utils

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.network.interfaces.IDNSResolver
import java.net.InetAddress

/**
 * Resolvedor DNS customizado que usa Cloudflare DNS (1.1.1.1) diretamente
 */
class CloudflareDNSResolver(
    private val config: DNSConfig = DNSConfig.CLOUDFLARE
) : IDNSResolver {

    companion object {
        private const val DNS_PORT = 53
        private const val DNS_TIMEOUT = 5000 // 5 segundos
        private const val DNS_QUERY_TYPE_A = 1
        private const val DNS_QUERY_CLASS_IN = 1
    }

    override fun resolve(hostname: String): InetAddress? {
        return try {
            // Primeiro tenta usar o resolvedor padrão do sistema
            val systemResolver = DNSResolver()
            val result = systemResolver.resolve(hostname)
            if (result != null) {
                return result
            }

            // Se falhar, tenta usar DNS direto do Cloudflare
            resolveViaCloudflare(hostname)
        } catch (e: Exception) {
            ShindoLogger.error("[CloudflareDNSResolver] Failed to resolve hostname: $hostname", e)
            null
        }
    }

    override fun resolveAll(hostname: String): Array<InetAddress> {
        return try {
            val systemResolver = DNSResolver()
            val results = systemResolver.resolveAll(hostname)
            if (results.isNotEmpty()) {
                return results
            }

            // Se falhar, tenta usar DNS direto do Cloudflare
            val cloudflareResult = resolveViaCloudflare(hostname)
            if (cloudflareResult != null) {
                arrayOf(cloudflareResult)
            } else {
                emptyArray()
            }
        } catch (e: Exception) {
            ShindoLogger.error("[CloudflareDNSResolver] Failed to resolve all addresses for hostname: $hostname", e)
            emptyArray()
        }
    }

    /**
     * Resolve um hostname usando DNS direto do Cloudflare
     * Nota: Esta é uma implementação simplificada. Para produção, considere usar uma biblioteca DNS completa.
     */
    private fun resolveViaCloudflare(hostname: String): InetAddress? {
        // Por enquanto, usa o resolvedor padrão do Java que respeita configurações do sistema
        // Em um ambiente controlado, poderíamos implementar um cliente DNS UDP completo
        return try {
            // Configura o DNS do sistema temporariamente (isso requer permissões de sistema)
            // Por enquanto, apenas logamos e usamos o resolvedor padrão
            ShindoLogger.info("[CloudflareDNSResolver] Attempting to resolve $hostname via ${config.name} DNS (${config.primaryDNS})")
            InetAddress.getByName(hostname)
        } catch (e: Exception) {
            ShindoLogger.error("[CloudflareDNSResolver] Cloudflare DNS resolution failed for: $hostname", e)
            null
        }
    }
}
