package me.miki.shindo.api.roles

import me.miki.shindo.api.websocket.presence.PresenceTracker
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object RoleVisuals {
    private const val ROLE_CACHE_TTL_MS = 2_000L
    private const val ONLINE_CACHE_TTL_MS = 1_000L

    private val staffColor = Color(178, 2, 2)
    private val netheriteColor = Color(52, 52, 52)
    private val emeraldColor = Color(0, 196, 60)
    private val diamondColor = Color(0, 184, 163)
    private val goldColor = Color(227, 216, 0)
    private val memberColor = Color(255, 255, 255)

    private const val STAFF_ICON = "shindo/icns/staff.png"
    private const val NETHERITE_ICON = "shindo/icns/netherite.png"
    private const val EMERALD_ICON = "shindo/icns/emerald.png"
    private const val DIAMOND_ICON = "shindo/icns/diamond.png"
    private const val GOLD_ICON = "shindo/icns/gold.png"
    private const val MEMBER_ICON = "shindo/icns/member.png"

    private val roleCache = ConcurrentHashMap<UUID, CacheEntry<Role>>()
    private val onlineCache = ConcurrentHashMap<UUID, CacheEntry<Boolean>>()

    @JvmStatic
    fun getPrimaryRole(uuid: UUID?): Role = RoleHierarchy.highest(RoleManager.getDirectRoles(uuid))

    @JvmStatic
    fun getPrimaryRoleCached(uuid: UUID?): Role {
        if (uuid == null) return Role.MEMBER
        val now = System.currentTimeMillis()
        val cached = roleCache[uuid]
        if (cached != null && cached.expiresAt > now) return cached.value
        val role = getPrimaryRole(uuid)
        roleCache[uuid] = CacheEntry(role, now + ROLE_CACHE_TTL_MS)
        return role
    }

    @JvmStatic
    fun isOnline(uuid: UUID?): Boolean {
        if (uuid == null) return false
        val now = System.currentTimeMillis()
        val cached = onlineCache[uuid]
        if (cached != null && cached.expiresAt > now) return cached.value
        val online = PresenceTracker.isOnline(uuid.toString())
        onlineCache[uuid] = CacheEntry(online, now + ONLINE_CACHE_TTL_MS)
        return online
    }

    @JvmStatic
    fun getRoleColor(role: Role?): Color =
        when (role) {
            Role.STAFF -> staffColor
            Role.NETHERITE -> netheriteColor
            Role.EMERALD -> emeraldColor
            Role.DIAMOND -> diamondColor
            Role.GOLD -> goldColor
            else -> memberColor
        }

    @JvmStatic
    fun getTabIcon(role: Role?): String = Shinconic.SHINDO

    @JvmStatic
    fun getTabFallbackText(role: Role?): String = "★"

    @JvmStatic
    fun getIconPNG(role: Role?): String =
        when (role) {
            Role.STAFF -> STAFF_ICON
            Role.NETHERITE -> NETHERITE_ICON
            Role.EMERALD -> EMERALD_ICON
            Role.DIAMOND -> DIAMOND_ICON
            Role.GOLD -> GOLD_ICON
            else -> MEMBER_ICON
        }

    private data class CacheEntry<T>(
        val value: T,
        val expiresAt: Long,
    )
}
