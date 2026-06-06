package com.shindoclient.shindo.utils.mouse

/**
 * Global mouse-wheel lock used to avoid scroll state changes during animated transitions.
 *
 * Nested locks are supported so different systems can coordinate safely.
 */
object ScrollInputGuard {
    private var lockDepth = 0

    fun lock() {
        lockDepth++
    }

    fun unlock() {
        if (lockDepth > 0) {
            lockDepth--
        }
    }

    fun isLocked(): Boolean = lockDepth > 0

    inline fun <T> withLock(block: () -> T): T {
        lock()
        return try {
            block()
        } finally {
            unlock()
        }
    }
}
