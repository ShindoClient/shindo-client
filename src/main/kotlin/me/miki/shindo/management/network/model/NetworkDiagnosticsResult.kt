package me.miki.shindo.management.network.model

/**
 * Resultado de um teste de latência.
 */
data class LatencyResult(
    val host: String,
    val pingMs: Int,
    val error: String?
) {
    val isSuccess: Boolean get() = pingMs > 0 && error == null
}

/**
 * Resultado de um teste de velocidade.
 */
data class SpeedResult(
    val downloadMbps: Double,
    val bytesRead: Int,
    val error: String?
) {
    val isSuccess: Boolean get() = downloadMbps > 0 && error == null
}

/**
 * Callback para resultados de latência.
 */
typealias LatencyCallback = (List<LatencyResult>) -> Unit

/**
 * Callback para resultados de velocidade.
 */
typealias SpeedCallback = (SpeedResult) -> Unit
