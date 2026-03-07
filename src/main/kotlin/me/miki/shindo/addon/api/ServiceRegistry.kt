package me.miki.shindo.addon.api

import kotlin.reflect.KClass

/**
 * Registro de serviços expostos pelo Shindo Client para addons.
 *
 * Implementações concretas existem apenas dentro do client; addons consomem
 * serviços através deste contrato, sem conhecer detalhes de implementação.
 */
interface ServiceRegistry {

    /**
     * Obtém uma instância de serviço registrada para o tipo informado.
     *
     * @return instância do serviço ou null caso não esteja registrada.
     */
    fun <T : Any> get(serviceClass: KClass<T>): T?

    /**
     * Obtém uma instância de serviço registrada ou lança exceção se ausente.
     *
     * Conveniente para serviços obrigatórios do core.
     */
    fun <T : Any> getOrThrow(serviceClass: KClass<T>): T =
        get(serviceClass)
            ?: throw IllegalStateException("Service not registered for: ${serviceClass.qualifiedName}")
}

