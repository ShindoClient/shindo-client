package me.miki.shindo.management.network.utils

/**
 * Configuração de DNS
 */
data class DNSConfig(
    /**
     * Endereço IP do servidor DNS primário
     */
    val primaryDNS: String,

    /**
     * Endereço IP do servidor DNS secundário (opcional)
     */
    val secondaryDNS: String? = null,

    /**
     * Nome amigável do servidor DNS
     */
    val name: String
) {
    companion object {
        /**
         * Configuração padrão do Cloudflare DNS (1.1.1.1)
         */
        val CLOUDFLARE = DNSConfig(
            primaryDNS = "1.1.1.1",
            secondaryDNS = "1.0.0.1",
            name = "Cloudflare"
        )

        /**
         * Configuração padrão do Google DNS (8.8.8.8)
         */
        val GOOGLE = DNSConfig(
            primaryDNS = "8.8.8.8",
            secondaryDNS = "8.8.4.4",
            name = "Google"
        )

        /**
         * Configuração padrão do Quad9 DNS (9.9.9.9)
         */
        val QUAD9 = DNSConfig(
            primaryDNS = "9.9.9.9",
            secondaryDNS = "149.112.112.112",
            name = "Quad9"
        )
    }
}
