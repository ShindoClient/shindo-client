package me.miki.shindo.management.network

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.network.model.*
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Utilitários para diagnóstico de rede.
 * Fornece testes de latência e velocidade de download.
 */
object NetworkDiagnostics {

    /**
     * Executa um teste de latência para múltiplos hosts em background.
     */
    fun runLatencyTest(hosts: List<String>, timeoutMs: Int, callback: LatencyCallback?) {
        Thread({
            val results = hosts
                .filter { !it.isNullOrBlank() }
                .map { pingHost(it.trim(), timeoutMs) }
            
            callback?.invoke(results)
        }, "shindo-latency").start()
    }

    /**
     * Executa um teste de velocidade de download em background.
     */
    fun runSpeedTest(url: String, bytesLimit: Int, callback: SpeedCallback?) {
        Thread({
            val result = download(url, bytesLimit)
            callback?.invoke(result)
        }, "shindo-speedtest").start()
    }

    private fun pingHost(host: String, timeoutMs: Int): LatencyResult {
        val start = System.currentTimeMillis()
        return try {
            val address = InetAddress.getByName(host)
            val reachable = address.isReachable(timeoutMs)
            val took = (System.currentTimeMillis() - start).toInt()
            LatencyResult(
                host = host,
                pingMs = if (reachable) took else -1,
                error = null
            )
        } catch (e: Exception) {
            LatencyResult(
                host = host,
                pingMs = -1,
                error = e.message
            )
        }
    }

    private fun download(targetUrl: String, limitBytes: Int): SpeedResult {
        val start = System.currentTimeMillis()
        var readBytes = 0
        
        return try {
            val connection = URL(targetUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("User-Agent", "Shindo-SpeedTest")
            connection.connect()
            
            val status = connection.responseCode
            if (status >= 400) {
                return SpeedResult(0.0, 0, "HTTP $status")
            }
            
            BufferedInputStream(connection.inputStream).use { input ->
                val buffer = ByteArray(8192)
                while (readBytes < limitBytes) {
                    val read = input.read(
                        buffer, 
                        0, 
                        buffer.size.coerceAtMost(limitBytes - readBytes)
                    )
                    if (read == -1) break
                    readBytes += read
                }
            }
            
            val durationMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            val mbps = (readBytes * 8.0) / (durationMs / 1000.0) / (1024.0 * 1024.0)
            
            SpeedResult(mbps, readBytes, null)
        } catch (e: Exception) {
            ShindoLogger.warn("Speed test failed", e)
            SpeedResult(0.0, readBytes, e.message)
        }
    }
}
