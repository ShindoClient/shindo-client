package me.miki.shindo.management.security.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.security.SecurityFeature
import net.minecraft.network.play.server.S48PacketResourcePackSend
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class ResourcePackSecurity : SecurityFeature() {
    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val pkt = event.getPacket()
        if (pkt is S48PacketResourcePackSend) {
            val url = pkt.url
            val hash = pkt.hash
            if (url.lowercase().startsWith("level://") && check(url, hash)) {
                event.setCancelled(true)
            }
        }
    }

    private fun check(
        url: String,
        hash: String,
    ): Boolean =
        try {
            val uri = URI(url)
            val scheme = uri.scheme
            val isLevelProtocol = "level" == scheme
            if ("http" != scheme && "https" != scheme && !isLevelProtocol) {
                throw java.net.URISyntaxException(
                    url,
                    "Wrong protocol",
                )
            }
            val decoded = URLDecoder.decode(url.substring("level://".length), StandardCharsets.UTF_8.name())
            if (isLevelProtocol && (decoded.contains("..") || !decoded.endsWith("/resources.zip"))) {
                throw java.net.URISyntaxException(decoded, "Invalid levelstorage resource pack path")
            }
            false
        } catch (_: Exception) {
            true
        }
}
