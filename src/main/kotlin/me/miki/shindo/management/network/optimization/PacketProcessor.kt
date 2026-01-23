package me.miki.shindo.management.network.optimization

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.TaskPriority
import me.miki.shindo.utils.concurrent.ThreadPoolType
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import java.util.concurrent.CompletableFuture

/**
 * Processa pacotes de rede de forma otimizada.
 * Gerencia processamento paralelo e sequencial baseado na classificação dos pacotes.
 * Coleta métricas e suporta logging.
 */
class PacketProcessor(
    private val metricsBuilder: PacketMetricsBuilder? = null,
    private val enableLogging: Boolean = false
) {
    
    /**
     * Processa um pacote de forma otimizada.
     * 
     * @param packet O pacote a ser processado
     * @param handler O handler de rede
     * @param optimizationEnabled Se a otimização está habilitada
     * @return Future que completa quando o pacote é processado
     */
    fun processPacket(
        packet: Packet<INetHandler>,
        handler: INetHandler,
        optimizationEnabled: Boolean
    ): CompletableFuture<Void> {
        val startTime = System.nanoTime()
        metricsBuilder?.incrementTotal()
        
        // Se a otimização não está habilitada, processa sequencialmente
        if (!optimizationEnabled) {
            return processSequentially(packet, handler, startTime)
        }
        
        // Classifica o pacote
        val packetType = PacketClassifier.classify(packet)
        
        if (packetType == PacketType.CRITICAL) {
            metricsBuilder?.incrementCritical()
        }
        
        if (enableLogging) {
            me.miki.shindo.logger.ShindoLogger.info("Processing packet: ${packet.javaClass.simpleName} as $packetType")
        }
        
        return when (packetType) {
            PacketType.CRITICAL -> {
                // Pacotes críticos sempre processados sequencialmente
                processSequentially(packet, handler, startTime)
            }
            
            PacketType.PARALLEL_SAFE -> {
                // Pacotes seguros podem ser processados em paralelo
                processInParallel(packet, handler, startTime)
            }
            
            PacketType.SEQUENTIAL -> {
                // Por padrão, processa sequencialmente
                processSequentially(packet, handler, startTime)
            }
        }
    }
    
    /**
     * Processa um pacote sequencialmente no thread atual.
     */
    private fun processSequentially(
        packet: Packet<INetHandler>,
        handler: INetHandler,
        startTime: Long
    ): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        try {
            packet.processPacket(handler)
            val duration = System.nanoTime() - startTime
            metricsBuilder?.incrementSequential()
            metricsBuilder?.addSequentialTime(duration)
            future.complete(null)
        } catch (e: Exception) {
            metricsBuilder?.incrementSequentialError()
            if (enableLogging) {
                ShindoLogger.error("Error processing packet sequentially: ${packet.javaClass.simpleName}", e)
            }
            future.completeExceptionally(e)
        }
        return future
    }
    
    /**
     * Processa um pacote em paralelo no pool de rede.
     */
    private fun processInParallel(
        packet: Packet<INetHandler>,
        handler: INetHandler,
        startTime: Long
    ): CompletableFuture<Void> {
        // Processa em paralelo no pool de rede
        val future = CompletableFuture<Void>()
        TaskExecutor.runAsync(
            ThreadPoolType.NETWORK,
            TaskPriority.NORMAL
        ) {
            try {
                packet.processPacket(handler)
                val duration = System.nanoTime() - startTime
                metricsBuilder?.incrementParallel()
                metricsBuilder?.addParallelTime(duration)
                future.complete(null)
            } catch (e: Exception) {
                metricsBuilder?.incrementParallelError()
                if (enableLogging) {
                    me.miki.shindo.logger.ShindoLogger.error("Error processing packet in parallel: ${packet.javaClass.simpleName}", e)
                }
                future.completeExceptionally(e)
            }
        }
        
        // Retorna o future para que o caller possa aguardar se necessário
        return future
    }
    
    /**
     * Processa um pacote e aguarda sua conclusão.
     * Útil quando é necessário garantir que o pacote foi processado antes de continuar.
     */
    fun processAndWait(
        packet: Packet<INetHandler>,
        handler: INetHandler,
        optimizationEnabled: Boolean
    ) {
        try {
            val future = processPacket(packet, handler, optimizationEnabled)
            
            // Aguarda o processamento de forma não-bloqueante quando possível
            if (!future.isDone()) {
                future.get()
            }
        } catch (e: Exception) {
            // Fallback para processamento sequencial em caso de erro
            try {
                packet.processPacket(handler)
            } catch (fallbackError: Exception) {
                // Se até o fallback falhar, deixa o erro propagar
                // O Minecraft lidará com o erro normalmente
                if (enableLogging) {
                    ShindoLogger.error("Fallback processing also failed for: ${packet.javaClass.simpleName}", fallbackError)
                }
            }
        }
    }
}
