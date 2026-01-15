package me.miki.shindo.management.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.netty.channel.Channel
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.network.model.*
import me.miki.shindo.management.network.module.NetworkModuleManager
import me.miki.shindo.management.network.module.impl.ChannelModule
import me.miki.shindo.management.network.module.impl.FlushModule
import me.miki.shindo.management.network.module.impl.MetricsModule
import me.miki.shindo.management.network.proxy.WarpProxyManager
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.config.SettingCategoryProvider
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.utils.JsonUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import java.io.File
import java.util.Locale

/**
 * Gerenciador central do sistema de network otimizado.
 * Coordena módulos e gerencia configurações de rede.
 */
class NetworkManager : ConfigOwner, SettingCategoryProvider {

    private val mc = Minecraft.getMinecraft()
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: File
    private val moduleManager = NetworkModuleManager()

    // Módulos
    private val channelModule = ChannelModule()
    private val flushModule = FlushModule()
    private val metricsModule = MetricsModule()

    // Configuração atual
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_OPTIMIZER_TOGGLE, category = "overview")
    private var optimizerEnabled: Boolean = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.NETWORK_MEDIUM, category = "profile")
    private var networkMedium: LinkMedium = LinkMedium.WIRED

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_LINK_CAPACITY, category = "profile", min = 10, max = 1000, step = 10, current = 200)
    private var linkCapacityMbps: Int = 200

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_AGGRESSIVE, category = "profile")
    private var aggressiveProfile: Boolean = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_ADAPTIVE_BUFFERING, category = "profile")
    private var adaptiveBuffering: Boolean = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_TCP_NODELAY, category = "transport")
    private var tcpNoDelayEnabled: Boolean = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_AUTO_FLUSH, category = "transport")
    private var autoFlushEnabled: Boolean = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_NATIVE_TRANSPORT, category = "transport")
    private var preferNativeTransport: Boolean = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_WRITE_BUFFER, category = "transport", min = 128, max = 4096, step = 32, current = 512)
    private var writeBufferKb: Int = 512

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_BURST_SMOOTHING, category = "flow")
    private var burstFlushSmoothing: Boolean = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_FLUSH_INTERVAL, category = "flow", min = 10, max = 120, step = 5, current = 45)
    private var flushIntervalMs: Int = 45

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_FLUSH_THRESHOLD, category = "flow", min = 1, max = 12, step = 1, current = 4)
    private var flushPacketThreshold: Int = 4

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_DYNAMIC_FLUSH, category = "flow")
    private var dynamicFlushEnabled: Boolean = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_JITTER_SENSITIVITY, category = "flow", min = 1, max = 20, step = 1, current = 6)
    private var jitterSensitivity: Int = 6

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_PROXY_WARP, category = "routing")
    private var warpProxyEnabled: Boolean = false

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_RESPONSIVENESS, category = "profile", min = 1, max = 10, step = 1, current = 6)
    private var responsivenessLevel: Int = 6

    // Estado interno
    private var configDirty: Boolean = false
    private var lastConfigSave: Long = 0L
    private var savedConfig: NetworkConfig? = null

    init {
        val instance = Shindo.getInstance()
        configFile = File(instance.fileManager.shindoDir, "connection-tweaker.json")
        
        // Registra módulos
        moduleManager.register(channelModule)
        moduleManager.register(flushModule)
        moduleManager.register(metricsModule)
        moduleManager.initializeAll()

        // Carrega configuração
        loadConfig()
        SettingRegistry.applyMetadata(this)
        
        // Aplica configurações iniciais
        applyNativeTransportSetting(preferNativeTransport)
        instance.warpProxyManager?.setEnabled(warpProxyEnabled)
        
        // Aplica configuração aos módulos
        applyConfigToModules()
        
        // Registra eventos
        instance.eventManager.register(this)
    }

    // Getters públicos (compatibilidade)
    fun isOptimizerEnabled(): Boolean = optimizerEnabled
    fun getNetworkMedium(): LinkMedium = networkMedium
    fun getLinkCapacityMbps(): Int = linkCapacityMbps
    fun isAggressiveProfileEnabled(): Boolean = aggressiveProfile
    fun isAdaptiveBufferingEnabled(): Boolean = adaptiveBuffering
    fun isTcpNoDelayEnabled(): Boolean = tcpNoDelayEnabled
    fun isAutoFlushEnabled(): Boolean = autoFlushEnabled
    fun isPreferNativeTransport(): Boolean = preferNativeTransport
    fun getWriteBufferKb(): Int = writeBufferKb
    fun isBurstFlushSmoothingEnabled(): Boolean = burstFlushSmoothing
    fun getFlushIntervalMs(): Int = flushIntervalMs
    fun getFlushPacketThreshold(): Int = flushPacketThreshold
    fun isDynamicFlushEnabled(): Boolean = dynamicFlushEnabled
    fun getJitterSensitivity(): Int = jitterSensitivity
    fun isWarpProxyEnabled(): Boolean = warpProxyEnabled
    fun getResponsivenessLevel(): Int = responsivenessLevel

    /**
     * Obtém a configuração atual como data class.
     */
    fun getConfig(): NetworkConfig {
        return NetworkConfig(
            optimizerEnabled = optimizerEnabled,
            networkMedium = networkMedium,
            linkCapacityMbps = linkCapacityMbps,
            aggressiveProfile = aggressiveProfile,
            adaptiveBuffering = adaptiveBuffering,
            tcpNoDelayEnabled = tcpNoDelayEnabled,
            autoFlushEnabled = autoFlushEnabled,
            preferNativeTransport = preferNativeTransport,
            writeBufferKb = writeBufferKb,
            burstFlushSmoothing = burstFlushSmoothing,
            flushIntervalMs = flushIntervalMs,
            flushPacketThreshold = flushPacketThreshold,
            warpProxyEnabled = warpProxyEnabled,
            dynamicFlushEnabled = dynamicFlushEnabled,
            jitterSensitivity = jitterSensitivity,
            responsivenessLevel = responsivenessLevel
        )
    }

    /**
     * Cria um snapshot completo do estado atual.
     */
    fun getProfileSnapshot(): NetworkSnapshot {
        val config = getConfig()
        val metrics = metricsModule.getMetrics()
        
        // Calcula valores dinâmicos usando a mesma lógica do FlushModule
        val dynamicInterval = if (config.dynamicFlushEnabled && metrics.pingCount > 0) {
            val base = config.flushIntervalMs.coerceAtLeast(10)
            val average = metrics.averagePing()
            val jitter = metrics.jitterPing()
            val jitterImpact = ((jitter * config.jitterSensitivity) / 20).coerceAtMost(12)
            val latencyImpact = if (average > 180) ((average - 180) / 25).coerceAtMost(10) else 0
            (base - jitterImpact - latencyImpact).coerceAtLeast(10)
        } else {
            config.flushIntervalMs
        }
        
        val dynamicThreshold = if (config.dynamicFlushEnabled && metrics.pingCount > 0) {
            val base = config.flushPacketThreshold.coerceAtLeast(1)
            val jitter = metrics.jitterPing()
            when {
                jitter > 40 -> (base - 2).coerceAtLeast(1)
                jitter > 20 -> (base - 1).coerceAtLeast(1)
                else -> base
            }
        } else {
            config.flushPacketThreshold
        }

        val warpDiagnostics = Shindo.getInstance().warpProxyManager?.getDiagnostics()
        return NetworkSnapshot.create(
            config = config,
            metrics = metrics,
            currentBufferKb = writeBufferKb,
            dynamicInterval = dynamicInterval,
            dynamicThreshold = dynamicThreshold,
            warpDiagnostics = warpDiagnostics
        )
    }

    /**
     * Aplica um perfil JSON.
     */
    fun applyProfile(json: JsonObject) {
        if (json == null) return
        
        val newConfig = NetworkConfig.fromJson(json, getConfig())
        applyConfig(newConfig)
    }

    /**
     * Aplica uma configuração completa.
     */
    private fun applyConfig(config: NetworkConfig) {
        optimizerEnabled = config.optimizerEnabled
        networkMedium = config.networkMedium
        linkCapacityMbps = config.linkCapacityMbps
        aggressiveProfile = config.aggressiveProfile
        adaptiveBuffering = config.adaptiveBuffering
        tcpNoDelayEnabled = config.tcpNoDelayEnabled
        autoFlushEnabled = config.autoFlushEnabled
        preferNativeTransport = config.preferNativeTransport
        writeBufferKb = config.writeBufferKb
        burstFlushSmoothing = config.burstFlushSmoothing
        flushIntervalMs = config.flushIntervalMs
        flushPacketThreshold = config.flushPacketThreshold
        warpProxyEnabled = config.warpProxyEnabled
        dynamicFlushEnabled = config.dynamicFlushEnabled
        jitterSensitivity = config.jitterSensitivity
        responsivenessLevel = config.responsivenessLevel

        applyConfigToModules()
        markDirty()
    }

    /**
     * Aplica a configuração atual aos módulos.
     */
    private fun applyConfigToModules() {
        val config = getConfig()
        
        // Aplica configuração aos módulos
        channelModule.applyConfig(config)
        flushModule.applyConfig(config)
        
        // Atualiza métricas no flush module
        flushModule.updateMetrics(metricsModule.getMetrics())
        
        // Aplica configurações especiais
        if (!config.optimizerEnabled && savedConfig == null) {
            savedConfig = config
            applyConfig(config.copyWithOptimizerDisabled())
        } else if (config.optimizerEnabled && savedConfig != null) {
            applyConfig(savedConfig!!)
            savedConfig = null
        }
        
        // Aplica perfil adaptativo se necessário
        if (config.optimizerEnabled && config.adaptiveBuffering) {
            applyAdaptiveProfile(config)
        }
        
        // Aplica proxy WARP
        Shindo.getInstance().warpProxyManager?.setEnabled(config.warpProxyEnabled)
    }

    /**
     * Aplica perfil adaptativo baseado na configuração.
     */
    private fun applyAdaptiveProfile(config: NetworkConfig) {
        val profile = NetworkProfile.calculate(config, writeBufferKb)
        writeBufferKb = profile.recommendedBufferKb
        
        if (config.aggressiveProfile) {
            // Ajusta configurações para perfil agressivo
            // (já aplicado via NetworkProfile)
        }
    }

    // Implementação de ConfigOwner
    override fun getConfigId(): String = "connectionTweaker"
    override fun getDisplayName(): String = TranslateText.NETWORK_OPTIMIZER_TOGGLE.text

    // Implementação de SettingCategoryProvider
    override fun resolveCategoryLabel(categoryKey: String?): TranslateText {
        if (categoryKey == null) return TranslateText.NONE
        
        return when (categoryKey.lowercase(Locale.ROOT)) {
            "overview" -> TranslateText.NETWORK_CATEGORY_OVERVIEW
            "profile" -> TranslateText.NETWORK_CATEGORY_PROFILE
            "transport" -> TranslateText.NETWORK_CATEGORY_TRANSPORT
            "flow" -> TranslateText.NETWORK_CATEGORY_FLOW
            "routing" -> TranslateText.NETWORK_CATEGORY_ROUTING
            else -> TranslateText.NONE
        }
    }

    /**
     * Aplica configurações ao canal ativo.
     */
    fun applyChannel(channel: Channel?) {
        channelModule.applyChannel(channel)
    }

    /**
     * Processa o envio de um pacote.
     */
    fun onSendPacket(channel: Channel?, packet: Packet<*>?) {
        flushModule.onSendPacket(channel, packet)
    }

    @EventTarget
    fun onUpdate(event: EventUpdate) {
        moduleManager.updateAll()
        flushModule.updateMetrics(metricsModule.getMetrics())
        
        if (configDirty && System.currentTimeMillis() - lastConfigSave > 750L) {
            saveConfig()
        }
    }

    // Setters públicos
    fun setTcpNoDelayEnabled(enabled: Boolean) {
        tcpNoDelayEnabled = enabled
        markDirty()
        applyConfigToModules()
    }

    fun setAutoFlushEnabled(enabled: Boolean) {
        autoFlushEnabled = enabled
        markDirty()
        applyConfigToModules()
    }

    fun setWarpProxyEnabled(enabled: Boolean) {
        warpProxyEnabled = enabled
        Shindo.getInstance().warpProxyManager?.setEnabled(enabled)
        markDirty()
    }

    fun setOptimizerEnabled(enabled: Boolean) {
        optimizerEnabled = enabled
        markDirty()
        applyConfigToModules()
    }

    fun setPreferNativeTransport(enabled: Boolean) {
        preferNativeTransport = enabled
        applyNativeTransportSetting(enabled)
        markDirty()
    }

    fun setWriteBufferKb(value: Int) {
        writeBufferKb = NetworkConfig.normalizeWriteBuffer(value)
        markDirty()
        applyConfigToModules()
    }

    fun setLinkCapacityMbps(value: Int) {
        linkCapacityMbps = NetworkConfig.normalizeLinkCapacity(value)
        markDirty()
        applyConfigToModules()
    }

    fun setNetworkMedium(medium: LinkMedium) {
        if (medium == null) return
        networkMedium = medium
        markDirty()
        applyConfigToModules()
    }

    fun setAggressiveProfile(aggressive: Boolean) {
        aggressiveProfile = aggressive
        markDirty()
        applyConfigToModules()
    }

    fun setAdaptiveBuffering(enabled: Boolean) {
        adaptiveBuffering = enabled
        markDirty()
        applyConfigToModules()
    }

    fun setBurstFlushSmoothing(enabled: Boolean) {
        burstFlushSmoothing = enabled
        markDirty()
        applyConfigToModules()
    }

    fun setFlushIntervalMs(value: Int) {
        flushIntervalMs = NetworkConfig.normalizeFlushInterval(value)
        markDirty()
        applyConfigToModules()
    }

    fun setFlushPacketThreshold(value: Int) {
        flushPacketThreshold = NetworkConfig.normalizeFlushThreshold(value)
        markDirty()
        applyConfigToModules()
    }

    fun setResponsivenessLevel(level: Int) {
        responsivenessLevel = NetworkConfig.normalizeResponsiveness(level)
        markDirty()
        applyConfigToModules()
    }

    fun setDynamicFlushEnabled(enabled: Boolean) {
        dynamicFlushEnabled = enabled
        markDirty()
        applyConfigToModules()
    }

    fun setJitterSensitivity(value: Int) {
        jitterSensitivity = NetworkConfig.normalizeJitterSensitivity(value)
        markDirty()
        applyConfigToModules()
    }

    private fun applyNativeTransportSetting(enabled: Boolean) {
        try {
            mc.gameSettings.useNativeTransport = enabled
            mc.gameSettings.saveOptions()
        } catch (e: Exception) {
            // Ignorar erros
        }
    }

    private fun markDirty() {
        configDirty = true
    }

    private fun loadConfig() {
        if (!configFile.exists()) return

        try {
            val reader = configFile.reader()
            val element: JsonElement? = gson.fromJson(reader, JsonElement::class.java)
            reader.close()
            
            if (element == null || !element.isJsonObject) return
            
            val json = element.asJsonObject
            val loadedConfig = NetworkConfig.fromJson(json, getConfig())
            applyConfig(loadedConfig)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load connection tweaker configuration", e)
        }
    }

    private fun saveConfig() {
        try {
            configFile.parentFile?.mkdirs()
            val config = getConfig()
            val json = config.toJson()
            
            val writer = configFile.writer()
            gson.toJson(json, writer)
            writer.close()
            
            configDirty = false
            lastConfigSave = System.currentTimeMillis()
        } catch (e: Exception) {
            ShindoLogger.error("Failed to save connection tweaker configuration", e)
        }
    }

    fun toProfileJson(): JsonObject = getConfig().toJson()

    /**
     * Classe de compatibilidade para ProfileSnapshot (usado pela UI).
     * @deprecated Use NetworkSnapshot diretamente
     */
    @Deprecated("Use getProfileSnapshot() which returns NetworkSnapshot", ReplaceWith("getProfileSnapshot()"))
    class ProfileSnapshot {
        var optimizerEnabled: Boolean = false
        var networkMedium: LinkMedium = LinkMedium.WIRED
        var linkCapacityMbps: Int = 0
        var aggressiveProfile: Boolean = false
        var adaptiveBuffering: Boolean = false
        var tcpNoDelay: Boolean = false
        var autoFlush: Boolean = false
        var preferNative: Boolean = false
        var writeBufferKb: Int = 0
        var burstSmoothing: Boolean = false
        var flushIntervalMs: Int = 0
        var flushThreshold: Int = 0
        var dynamicFlush: Boolean = false
        var dynamicIntervalMs: Int = 0
        var dynamicThreshold: Int = 0
        var averagePingMs: Int = 0
        var jitterMs: Int = 0
        var responsivenessLevel: Int = 0
        var latencyFocus: Float = 0f
        var stabilityFocus: Float = 0f
        var throughputFocus: Float = 0f
        var recommendedBufferKb: Int = 0
        var warpProxyEnabled: Boolean = false
        var warpStatus: WarpProxyManager.WarpStatus = WarpProxyManager.WarpStatus.DISABLED
        var warpResolver: String? = null
        var warpLookupMs: Long = 0L
        var warpLastUpdatedAt: Long = 0L
        var warpCacheHit: Boolean = false
        var warpError: String? = null

        fun isOptimizerEnabled(): Boolean = optimizerEnabled
        fun getLatencyFocus(): Float = latencyFocus
        fun getStabilityFocus(): Float = stabilityFocus
        fun getThroughputFocus(): Float = throughputFocus
    }

    /**
     * Obtém ProfileSnapshot (compatibilidade).
     * @deprecated Use getProfileSnapshot() que retorna NetworkSnapshot
     */
    @Deprecated("Use getProfileSnapshot() which returns NetworkSnapshot", ReplaceWith("getProfileSnapshot()"))
    val profileSnapshot: ProfileSnapshot
        get() {
            val snapshot = getProfileSnapshot()
            val ps = ProfileSnapshot()
            ps.optimizerEnabled = snapshot.config.optimizerEnabled
            ps.networkMedium = snapshot.config.networkMedium
            ps.linkCapacityMbps = snapshot.config.linkCapacityMbps
            ps.aggressiveProfile = snapshot.config.aggressiveProfile
            ps.adaptiveBuffering = snapshot.config.adaptiveBuffering
            ps.tcpNoDelay = snapshot.config.tcpNoDelayEnabled
            ps.autoFlush = snapshot.config.autoFlushEnabled
            ps.preferNative = snapshot.config.preferNativeTransport
            ps.writeBufferKb = snapshot.config.writeBufferKb
            ps.burstSmoothing = snapshot.config.burstFlushSmoothing
            ps.flushIntervalMs = snapshot.config.flushIntervalMs
            ps.flushThreshold = snapshot.config.flushPacketThreshold
            ps.dynamicFlush = snapshot.config.dynamicFlushEnabled
            ps.dynamicIntervalMs = snapshot.dynamicIntervalMs
            ps.dynamicThreshold = snapshot.dynamicThreshold
            ps.averagePingMs = snapshot.metrics.averagePing()
            ps.jitterMs = snapshot.metrics.jitterPing()
            ps.responsivenessLevel = snapshot.config.responsivenessLevel
            ps.latencyFocus = snapshot.profile.latencyFocus
            ps.stabilityFocus = snapshot.profile.stabilityFocus
            ps.throughputFocus = snapshot.profile.throughputFocus
            ps.recommendedBufferKb = snapshot.profile.recommendedBufferKb
            ps.warpProxyEnabled = snapshot.warpInfo.enabled
            ps.warpStatus = snapshot.warpInfo.status
            ps.warpResolver = snapshot.warpInfo.resolver
            ps.warpLookupMs = snapshot.warpInfo.lookupMs
            ps.warpLastUpdatedAt = snapshot.warpInfo.lastUpdatedAt
            ps.warpCacheHit = snapshot.warpInfo.cacheHit
            ps.warpError = snapshot.warpInfo.error
            return ps
        }
}
