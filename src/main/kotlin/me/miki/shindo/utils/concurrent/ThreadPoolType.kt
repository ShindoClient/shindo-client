package me.miki.shindo.utils.concurrent

enum class ThreadPoolType {
    IO,
    CPU,
    NETWORK,
    SCHEDULED,
    GENERAL,
}
