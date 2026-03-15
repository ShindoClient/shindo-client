package me.miki.shindo.management.network.proxy

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.network.interfaces.IDNSProxy
import me.miki.shindo.management.network.utils.CloudflareDNSResolver
import me.miki.shindo.management.network.utils.DNSConfig
import me.miki.shindo.management.network.utils.DNSResolver
import java.net.InetAddress
import java.util.*

/**
 * Proxy DNS customizado criado pelo usuário
 */
class CustomProxy(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val primaryDNS: String,
    val secondaryDNS: String? = null
) : IDNSProxy {

    private var active = false
    private val config = DNSConfig(
        primaryDNS = primaryDNS,
        secondaryDNS = secondaryDNS,
        name = name
    )
    private val customResolver = CloudflareDNSResolver(config)
    private val defaultResolver = DNSResolver()

    override fun isActive(): Boolean {
        return active
    }

    override fun enable() {
        if (active) {
            ShindoLogger.warn("[CustomProxy] Proxy '$name' is already active")
            return
        }

        try {
            ShindoLogger.info("[CustomProxy] Enabling custom DNS proxy '$name' (${config.primaryDNS})")
            active = true
            ShindoLogger.info("[CustomProxy] Custom DNS proxy '$name' enabled successfully")
        } catch (e: Exception) {
            ShindoLogger.error("[CustomProxy] Failed to enable proxy '$name'", e)
            active = false
        }
    }

    override fun disable() {
        if (!active) {
            return
        }

        try {
            ShindoLogger.info("[CustomProxy] Disabling custom DNS proxy '$name'")
            active = false
            ShindoLogger.info("[CustomProxy] Custom DNS proxy '$name' disabled successfully")
        } catch (e: Exception) {
            ShindoLogger.error("[CustomProxy] Failed to disable proxy '$name'", e)
        }
    }

    override fun resolve(hostname: String): InetAddress? {
        return if (active) {
            customResolver.resolve(hostname) ?: defaultResolver.resolve(hostname)
        } else {
            defaultResolver.resolve(hostname)
        }
    }

    override fun getDNSName(): String {
        return name
    }

    override fun getDNSAddress(): String {
        return primaryDNS
    }

    /**
     * Valida se o proxy é válido
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && primaryDNS.isNotBlank() && isValidIP(primaryDNS) &&
                (secondaryDNS == null || secondaryDNS.isBlank() || isValidIP(secondaryDNS))
    }

    /**
     * Verifica se uma string é um IP válido
     */
    private fun isValidIP(ip: String): Boolean {
        return try {
            val parts = ip.split(".")
            if (parts.size != 4) return false
            parts.all { part ->
                val num = part.toInt()
                num in 0..255
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Cria uma cópia do proxy
     */
    fun copy(
        id: String = this.id,
        name: String = this.name,
        primaryDNS: String = this.primaryDNS,
        secondaryDNS: String? = this.secondaryDNS
    ): CustomProxy {
        return CustomProxy(id, name, primaryDNS, secondaryDNS)
    }
}
