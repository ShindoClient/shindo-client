package me.miki.shindo.management.network.model

/**
 * Métricas de rede coletadas em tempo real.
 * Armazena histórico de ping e calcula estatísticas.
 */
data class NetworkMetrics(
    val pingSamples: IntArray = IntArray(PING_HISTORY),
    val pingIndex: Int = 0,
    val pingCount: Int = 0,
    val lastPingPoll: Long = 0L
) {
    companion object {
        const val PING_HISTORY = 30
        const val PING_POLL_MS = 500L
    }

    fun addPingSample(ping: Int): NetworkMetrics {
        val newSamples = pingSamples.copyOf()
        val newIndex = (pingIndex + 1) % PING_HISTORY
        newSamples[pingIndex] = ping
        return copy(
            pingSamples = newSamples,
            pingIndex = newIndex,
            pingCount = (pingCount + 1).coerceAtMost(PING_HISTORY)
        )
    }

    fun averagePing(): Int {
        if (pingCount == 0) return 0
        return pingSamples.take(pingCount).sum() / pingCount
    }

    fun jitterPing(): Int {
        if (pingCount < 2) return 0
        val samples = pingSamples.take(pingCount)
        val min = samples.minOrNull() ?: 0
        val max = samples.maxOrNull() ?: 0
        return (max - min).coerceAtLeast(0)
    }

    fun shouldPoll(now: Long): Boolean = (now - lastPingPoll) >= PING_POLL_MS

    fun withPollTime(now: Long): NetworkMetrics = copy(lastPingPoll = now)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkMetrics

        if (!pingSamples.contentEquals(other.pingSamples)) return false
        if (pingIndex != other.pingIndex) return false
        if (pingCount != other.pingCount) return false
        if (lastPingPoll != other.lastPingPoll) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pingSamples.contentHashCode()
        result = 31 * result + pingIndex
        result = 31 * result + pingCount
        result = 31 * result + lastPingPoll.hashCode()
        return result
    }
}
