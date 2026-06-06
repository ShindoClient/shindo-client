package com.shindoclient.shindo.management.network.interfaces

import java.net.InetAddress

/**
 * Interface para resolução de DNS
 */
interface IDNSResolver {
    /**
     * Resolve um hostname para um endereço IP
     * @param hostname O hostname a ser resolvido
     * @return O endereço IP resolvido, ou null se não conseguir resolver
     */
    fun resolve(hostname: String): InetAddress?

    /**
     * Resolve um hostname para todos os endereços IP disponíveis
     * @param hostname O hostname a ser resolvido
     * @return Lista de endereços IP resolvidos
     */
    fun resolveAll(hostname: String): Array<InetAddress>
}
