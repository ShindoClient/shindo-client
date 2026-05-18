package me.miki.shindo.utils

class TimerUtils {
    var lastMs: Long = 0L

    val elapsedTime: Long
        get() = System.currentTimeMillis() - lastMs

    fun reset() {
        lastMs = System.currentTimeMillis()
    }

    fun delay(nextDelay: Long): Boolean = System.currentTimeMillis() - lastMs >= nextDelay

    @JvmOverloads
    fun delay(
        nextDelay: Float,
        reset: Boolean = false,
    ): Boolean {
        if (System.currentTimeMillis() - lastMs >= nextDelay) {
            if (reset) {
                this.reset()
            }
            return true
        }
        return false
    }

    fun isDelayComplete(valueState: Double): Boolean = System.currentTimeMillis() - lastMs >= valueState
}
