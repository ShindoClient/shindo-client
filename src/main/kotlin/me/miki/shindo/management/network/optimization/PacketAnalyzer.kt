package me.miki.shindo.management.network.optimization

import net.minecraft.network.Packet

/**
 * Utilitário para análise e debug de pacotes.
 * Fornece informações detalhadas sobre pacotes para diagnóstico.
 */
object PacketAnalyzer {
    
    /**
     * Informações detalhadas sobre um pacote.
     */
    data class PacketInfo(
        val className: String,
        val simpleName: String,
        val packetType: PacketType,
        val canProcessInParallel: Boolean,
        val isCritical: Boolean,
        val packageName: String
    )
    
    /**
     * Analisa um pacote e retorna informações detalhadas.
     */
    fun analyze(packet: Packet<*>): PacketInfo {
        val clazz = packet.javaClass
        val packetType = PacketClassifier.classify(packet)
        
        return PacketInfo(
            className = clazz.name,
            simpleName = clazz.simpleName,
            packetType = packetType,
            canProcessInParallel = PacketClassifier.canProcessInParallel(packet),
            isCritical = PacketClassifier.isCritical(packet),
            packageName = clazz.`package`?.name ?: "unknown"
        )
    }
    
    /**
     * Gera um relatório de análise de um pacote.
     */
    fun generateReport(packet: Packet<*>): String {
        val info = analyze(packet)
        val metrics = NetworkOptimizationManager.getMetrics()
        
        return buildString {
            appendLine("=== Packet Analysis Report ===")
            appendLine("Class: ${info.className}")
            appendLine("Simple Name: ${info.simpleName}")
            appendLine("Package: ${info.packageName}")
            appendLine("Type: ${info.packetType}")
            appendLine("Can Process in Parallel: ${info.canProcessInParallel}")
            appendLine("Is Critical: ${info.isCritical}")
            appendLine()
            appendLine("=== System Metrics ===")
            appendLine("Total Processed: ${metrics.totalProcessed}")
            appendLine("Parallel Processed: ${metrics.parallelProcessed}")
            appendLine("Sequential Processed: ${metrics.sequentialProcessed}")
            appendLine("Critical Processed: ${metrics.criticalProcessed}")
            appendLine("Parallel Rate: ${String.format("%.2f%%", metrics.parallelRate * 100)}")
            appendLine("Parallel Error Rate: ${String.format("%.2f%%", metrics.parallelErrorRate * 100)}")
            appendLine("Average Parallel Time: ${metrics.averageParallelTimeNs}ns")
            appendLine("Average Sequential Time: ${metrics.averageSequentialTimeNs}ns")
        }
    }
    
    /**
     * Verifica se um pacote está na whitelist ou blacklist.
     */
    fun getClassificationDetails(packet: Packet<*>): String {
        val info = analyze(packet)
        val config = PacketClassifier.getConfig()
        
        return buildString {
            appendLine("Classification Details for ${info.simpleName}:")
            appendLine("  Type: ${info.packetType}")
            
            when (info.packetType) {
                PacketType.CRITICAL -> {
                    appendLine("  Reason: Matches critical pattern (blacklist)")
                }
                PacketType.PARALLEL_SAFE -> {
                    appendLine("  Reason: In parallel-safe whitelist")
                }
                PacketType.SEQUENTIAL -> {
                    appendLine("  Reason: Default sequential processing (safety)")
                }
            }
            
            if (config.additionalCriticalPatterns.isNotEmpty()) {
                appendLine("  Additional Critical Patterns: ${config.additionalCriticalPatterns}")
            }
            if (config.additionalParallelSafeClasses.isNotEmpty()) {
                appendLine("  Additional Parallel-Safe Classes: ${config.additionalParallelSafeClasses}")
            }
        }
    }
}
