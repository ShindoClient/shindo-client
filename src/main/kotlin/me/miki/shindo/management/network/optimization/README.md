# Sistema de Otimização de Rede

Este package contém o sistema completo para gerenciar otimizações de rede e processamento de pacotes.

## Estrutura

### `PacketType.kt`
Enum que define os tipos de processamento de pacotes:
- `CRITICAL`: Pacotes que DEVEM ser processados sequencialmente
- `PARALLEL_SAFE`: Pacotes que podem ser processados em paralelo
- `SEQUENTIAL`: Pacotes que requerem processamento sequencial por padrão

### `PacketClassifier.kt`
Classifica pacotes de rede para determinar o tipo de processamento adequado:
- **Lista Negra**: Padrões de nomes que indicam pacotes críticos
- **Whitelist**: Classes de pacotes seguros para processamento paralelo
- **Configuração Dinâmica**: Suporta adicionar/remover padrões e classes em tempo de execução
- **Cache**: Cache de classificação para melhor performance
- Métodos: `classify()`, `canProcessInParallel()`, `isCritical()`, `updateConfig()`

### `PacketProcessor.kt`
Processa pacotes de forma otimizada:
- Gerencia processamento paralelo e sequencial
- Usa `ThreadPoolType.NETWORK` para processamento paralelo
- Fallback automático para processamento sequencial em caso de erro
- Coleta métricas de performance (tempo, erros, etc.)
- Suporta logging opcional

### `NetworkOptimizationManager.kt`
Gerenciador central do sistema:
- Coordena classificação e processamento de pacotes
- Verifica se a otimização está habilitada
- Fornece API pública para processamento de pacotes
- Gerencia métricas e cache
- Suporta configuração dinâmica
- Métodos utilitários para classificação e verificação

### `PacketMetrics.kt`
Sistema de métricas para análise de performance:
- Rastreia total de pacotes processados
- Separação entre paralelo e sequencial
- Contagem de erros
- Tempo de processamento
- Taxas de paralelismo e erro

### `PacketCache.kt`
Cache de classificação de pacotes:
- Melhora performance evitando reclassificação
- Thread-safe usando `ConcurrentHashMap`
- Estatísticas de cache disponíveis

### `PacketConfig.kt`
Configuração do sistema:
- Habilita/desabilita otimização
- Padrões e classes adicionais para lista negra/whitelist
- Controle de cache e métricas
- Logging opcional

### `PacketAnalyzer.kt`
Utilitário para análise e debug de pacotes:
- Análise detalhada de pacotes
- Relatórios de classificação
- Informações sobre whitelist/blacklist
- Integração com métricas do sistema

## Uso

### No Mixin (Java)
```java
NetworkOptimizationManager.INSTANCE.processPacket(packet, handler);
```

### No Código Kotlin
```kotlin
NetworkOptimizationManager.processPacket(packet, handler)

// Classificar um pacote
val type = NetworkOptimizationManager.classifyPacket(packet)

// Verificar se pode processar em paralelo
val canParallel = NetworkOptimizationManager.canProcessInParallel(packet)
```

## Pacotes Suportados

### Processamento Paralelo (Whitelist)
- `S02PacketChat` - Mensagens de chat
- `S29PacketSoundEffect` - Efeitos sonoros
- `S2APacketParticles` - Partículas visuais
- `S28PacketEffect` - Efeitos de mundo
- `S03PacketTimeUpdate` - Atualização de tempo
- `S05PacketSpawnPosition` - Posição de spawn
- `S41PacketServerDifficulty` - Dificuldade do servidor
- `S30PacketWindowItems` - Itens de janela (inventário)
- `S2FPacketSetSlot` - Atualização de slot

### Processamento Sequencial (Lista Negra)
- Pacotes contendo: `Chunk`, `JoinGame`, `Respawn`, `Spawn`, `Destroy`
- Pacotes contendo: `Explosion`, `BlockChange`, `MultiBlockChange`, `MapChunk`
- Pacotes contendo: `KeepAlive`, `PlayerPosLook`, `EntityVelocity`, `EntityHeadLook`, `EntityEquipment`

## Configuração Dinâmica

O sistema suporta configuração dinâmica em tempo de execução:

```kotlin
// Atualizar configuração completa
NetworkOptimizationManager.updateConfig(
    PacketConfig(
        optimizationEnabled = true,
        additionalCriticalPatterns = setOf("CustomCritical"),
        additionalParallelSafeClasses = setOf("S99PacketCustom"),
        useCache = true,
        collectMetrics = true,
        enableLogging = false
    )
)

// Adicionar padrão crítico dinamicamente
NetworkOptimizationManager.addCriticalPattern("CustomPattern")

// Adicionar classe segura dinamicamente
NetworkOptimizationManager.addParallelSafeClass("S99PacketCustom")

// Obter métricas
val metrics = NetworkOptimizationManager.getMetrics()
println("Parallel rate: ${metrics.parallelRate}")
println("Average parallel time: ${metrics.averageParallelTimeNs}ns")

// Obter estatísticas de cache
val cacheStats = NetworkOptimizationManager.getCacheStats()
println("Cache size: ${cacheStats.classificationSize}")

// Analisar um pacote
val info = PacketAnalyzer.analyze(packet)
println("Packet type: ${info.packetType}")
println("Can parallel: ${info.canProcessInParallel}")

// Gerar relatório completo
val report = PacketAnalyzer.generateReport(packet)
println(report)
```

## Expansão Futura

Para adicionar novos pacotes à whitelist:
1. Use `addParallelSafeClass()` dinamicamente, OU
2. Adicione o nome da classe em `DEFAULT_PARALLEL_SAFE_CLASSES` em `PacketClassifier.kt`
3. Teste extensivamente para garantir que não causa problemas de sincronização
4. Considere adicionar à lista negra se o pacote afetar estado crítico

## Thread Safety

- O sistema é thread-safe
- Processamento paralelo usa `ThreadPoolType.NETWORK`
- Fallback automático garante que pacotes sempre sejam processados
- Sincronização adequada para evitar race conditions
