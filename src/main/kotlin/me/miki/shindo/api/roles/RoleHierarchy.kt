package me.miki.shindo.api.roles

object RoleHierarchy {

    @JvmStatic
    fun rank(role: Role?): Int = role?.priority ?: 0

    @JvmStatic
    fun atLeast(have: Role?, required: Role?): Boolean = Role.atLeast(have, required)

    @JvmStatic
    fun highest(roles: Collection<Role>?): Role {
        var best = Role.MEMBER
        if (roles.isNullOrEmpty()) return best
        for (r in roles) {
            best = Role.max(best, r)
        }
        return best
    }

    @JvmStatic
    fun hasAtLeast(roles: Collection<Role>?, required: Role?): Boolean =
        atLeast(highest(roles), required)
}
