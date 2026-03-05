package me.miki.shindo.api.services

import me.miki.client_api.ServiceRegistry
import kotlin.reflect.KClass

/**
 * Implementação simples de [ServiceRegistry] usada dentro do Shindo Client.
 *
 * Mantém instâncias singleton de serviços compartilhados acessíveis pelos addons.
 */
class ClientServiceRegistry : ServiceRegistry {

    private val services = mutableMapOf<KClass<*>, Any>()

    @Synchronized
    fun <T : Any> register(serviceClass: KClass<T>, impl: T) {
        if (services.containsKey(serviceClass)) {
            throw IllegalStateException("Service already registered for: ${serviceClass.qualifiedName}")
        }
        services[serviceClass] = impl
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(serviceClass: KClass<T>): T? {
        return services[serviceClass] as? T
    }
}

