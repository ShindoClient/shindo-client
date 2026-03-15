package me.miki.shindo.utils.concurrent

enum class TaskPriority(val value: Int) {
    CRITICAL(100),
    HIGH(75),
    NORMAL(50),
    LOW(25),
    IDLE(0);

    companion object {
        @JvmStatic
        fun isHigherThan(a: TaskPriority, b: TaskPriority): Boolean = a.value > b.value
    }
}
