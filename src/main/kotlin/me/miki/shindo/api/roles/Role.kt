package me.miki.shindo.api.roles

enum class Role(val priority: Int) {
    MEMBER(1),
    GOLD(2),
    DIAMOND(3),
    EMERALD(4),
    NETHERITE(5),
    STAFF(6);

    companion object {
        @JvmStatic
        fun max(a: Role?, b: Role?): Role {
            if (a == null) return b ?: MEMBER
            if (b == null) return a
            return if (a.priority >= b.priority) a else b
        }

        @JvmStatic
        fun atLeast(have: Role?, required: Role?): Boolean {
            if (required == null) return true
            if (have == null) return false
            return have.priority >= required.priority
        }
    }
}
