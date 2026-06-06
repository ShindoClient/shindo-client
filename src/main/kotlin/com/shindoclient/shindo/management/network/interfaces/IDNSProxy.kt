package com.shindoclient.shindo.management.network.interfaces

import java.net.InetAddress

/**
 * Interface para sistemas de proxy DNS
 */
interface IDNSProxy {
    /**
     * Verifica se o proxy está ativo
     */
    fun isActive(): Boolean

    /**
     * Ativa o proxy DNS
     */
    fun enable()

    /**
     * Desativa o proxy DNS
     */
    fun disable()

    /**
     * Resolve um hostname para um endereço IP usando o proxy DNS
     * @param hostname O hostname a ser resolvido
     * @return O endereço IP resolvido, ou null se não conseguir resolver
     */
    fun resolve(hostname: String): InetAddress?

    /**
     * Obtém o nome do servidor DNS usado pelo proxy
     */
    fun getDNSName(): String

    /**
     * Obtém o endereço IP do servidor DNS usado pelo proxy
     */
    fun getDNSAddress(): String
}
