package me.miki.shindo.api.roles

import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class RoleManager {
    private val listeners = mutableListOf<Consumer<UUID>>()

    fun setRoles(
        uuid: UUID?,
        newRoles: Set<Role>?,
    ) {
        if (uuid == null) return
        val copy = EnumSet.noneOf(Role::class.java)
        if (newRoles != null) copy.addAll(newRoles)
        if (copy.isEmpty()) copy.add(Role.MEMBER)
        roles[uuid] = copy
        notifyChange(uuid)
    }

    fun addRole(
        uuid: UUID?,
        role: Role?,
    ) {
        if (uuid == null || role == null) return
        roles.compute(uuid) { _, v ->
            val set = if (v == null || v.isEmpty()) EnumSet.noneOf(Role::class.java) else EnumSet.copyOf(v)
            set.add(role)
            set
        }
        notifyChange(uuid)
    }

    fun removeRole(
        uuid: UUID?,
        role: Role?,
    ) {
        if (uuid == null || role == null) return
        roles.computeIfPresent(uuid) { _, v ->
            val set = if (v.isEmpty()) EnumSet.noneOf(Role::class.java) else EnumSet.copyOf(v)
            set.remove(role)
            if (set.isEmpty()) set.add(Role.MEMBER)
            set
        }
        notifyChange(uuid)
    }

    fun onChange(listener: Consumer<UUID>?) {
        if (listener != null) listeners.add(listener)
    }

    private fun notifyChange(uuid: UUID) {
        for (listener in listeners) {
            try {
                listener.accept(uuid)
            } catch (ignored: Exception) {
            }
        }
    }

    companion object {
        private val roles = ConcurrentHashMap<UUID, MutableSet<Role>>()

        @JvmStatic
        fun getDirectRoles(uuid: UUID?): Set<Role> {
            if (uuid == null) return EnumSet.of(Role.MEMBER)
            val existing = roles[uuid]
            if (existing == null || existing.isEmpty()) return EnumSet.of(Role.MEMBER)
            return EnumSet.copyOf(existing)
        }

        @JvmStatic
        fun hasRole(
            uuid: UUID?,
            role: Role?,
        ): Boolean {
            if (role == null) return false
            return getDirectRoles(uuid).contains(role)
        }

        @JvmStatic
        fun hasAtLeast(
            uuid: UUID?,
            required: Role?,
        ): Boolean = RoleHierarchy.hasAtLeast(getDirectRoles(uuid), required)
    }
}
