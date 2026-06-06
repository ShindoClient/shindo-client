package com.shindoclient.shindo.management.network.utils

import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.network.interfaces.IDNSResolver
import java.net.InetAddress

/**
 * Implementação padrão de resolução DNS usando o sistema Java
 */
class DNSResolver : IDNSResolver {
    override fun resolve(hostname: String): InetAddress? =
        try {
            InetAddress.getByName(hostname)
        } catch (e: Exception) {
            ShindoLogger.error("[DNSResolver] Failed to resolve hostname: $hostname", e)
            null
        }

    override fun resolveAll(hostname: String): Array<InetAddress> =
        try {
            InetAddress.getAllByName(hostname)
        } catch (e: Exception) {
            ShindoLogger.error("[DNSResolver] Failed to resolve all addresses for hostname: $hostname", e)
            emptyArray()
        }
}
