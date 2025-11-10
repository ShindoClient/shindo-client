package me.miki.shindo.management.tweaker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.config.SettingCategoryProvider;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.management.tweaker.proxy.WarpProxyManager;
import me.miki.shindo.utils.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Centralises connection level tweaks (TCP flags, buffering and flush cadence) and exposes them as configurable
 * properties that can be surfaced inside the mod menu. The manager is responsible for synchronising runtime channel
 * settings with the values defined by the user as well as persisting those values between sessions.
 */
public class ConnectionTweakerManager implements ConfigOwner, SettingCategoryProvider {

    private static final boolean DEFAULT_TCP_NODELAY = false;
    private static final boolean DEFAULT_AUTO_FLUSH = false;
    private static final boolean DEFAULT_NATIVE_TRANSPORT = true;
    private static final int DEFAULT_WRITE_BUFFER_KB = 256;

    private static final int MIN_WRITE_BUFFER_KB = 128;
    private static final int MAX_WRITE_BUFFER_KB = 4096;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_OPTIMIZER_TOGGLE, category = "overview")
    private boolean optimizerEnabled = true;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.NETWORK_MEDIUM, category = "profile")
    private LinkMedium networkMedium = LinkMedium.WIRED;

    @Getter
    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_LINK_CAPACITY, category = "profile", min = 10, max = 1000, step = 10, current = 200)
    private int linkCapacityMbps = 200;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_AGGRESSIVE, category = "profile")
    private boolean aggressiveProfile = false;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_ADAPTIVE_BUFFERING, category = "profile")
    private boolean adaptiveBuffering = true;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_TCP_NODELAY, category = "transport")
    private boolean tcpNoDelayEnabled = true;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_AUTO_FLUSH, category = "transport")
    private boolean autoFlushEnabled = true;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_NATIVE_TRANSPORT, category = "transport")
    private boolean preferNativeTransport = true;

    @Getter
    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_WRITE_BUFFER, category = "transport", min = MIN_WRITE_BUFFER_KB, max = MAX_WRITE_BUFFER_KB, step = 32, current = 512)
    private int writeBufferKb = 512;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_BURST_SMOOTHING, category = "flow")
    private boolean burstFlushSmoothing = true;

    @Getter
    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_FLUSH_INTERVAL, category = "flow", min = 10, max = 120, step = 5, current = 45)
    private int flushIntervalMs = 45;

    @Getter
    @Property(type = PropertyType.NUMBER, translate = TranslateText.NETWORK_FLUSH_THRESHOLD, category = "flow", min = 1, max = 12, step = 1, current = 4)
    private int flushPacketThreshold = 4;

    @Getter
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NETWORK_PROXY_WARP, category = "routing")
    private boolean warpProxyEnabled = false;

    private Channel activeChannel;
    private Boolean cachedOptimizerEnabled;
    private LinkMedium cachedMedium;
    private Integer cachedLinkCapacity;
    private Boolean cachedAggressive;
    private Boolean cachedAdaptive;
    private Boolean cachedTcpNoDelay;
    private Boolean cachedAutoFlush;
    private Boolean cachedPreferNative;
    private Integer cachedWriteBuffer;
    private Boolean cachedBurstSmoothing;
    private Integer cachedFlushInterval;
    private Integer cachedFlushThreshold;
    private Boolean cachedWarpProxyEnabled;

    private boolean configDirty;
    private long lastFlushTimestamp;
    private int pendingPackets;
    private long lastConfigSave;

    private SavedState savedState;

    public ConnectionTweakerManager() {
        Shindo instance = Shindo.getInstance();
        this.configFile = new File(instance.getFileManager().getShindoDir(), "connection-tweaker.json");
        loadConfig();
        SettingRegistry.applyMetadata(this);
        applyNativeTransportSetting(preferNativeTransport);
        WarpProxyManager warpProxyManager = instance.getWarpProxyManager();
        if (warpProxyManager != null) {
            warpProxyManager.setEnabled(warpProxyEnabled);
        }
        invalidateCaches();
        refreshRuntime();
        instance.getEventManager().register(this);
    }

    public ProfileSnapshot getProfileSnapshot() {
        ProfileSnapshot snapshot = new ProfileSnapshot();
        snapshot.optimizerEnabled = optimizerEnabled;
        snapshot.networkMedium = networkMedium;
        snapshot.linkCapacityMbps = linkCapacityMbps;
        snapshot.aggressiveProfile = aggressiveProfile;
        snapshot.adaptiveBuffering = adaptiveBuffering;
        snapshot.tcpNoDelay = tcpNoDelayEnabled;
        snapshot.autoFlush = autoFlushEnabled;
        snapshot.preferNative = preferNativeTransport;
        snapshot.writeBufferKb = writeBufferKb;
        snapshot.burstSmoothing = burstFlushSmoothing;
        snapshot.flushIntervalMs = flushIntervalMs;
        snapshot.flushThreshold = flushPacketThreshold;
        snapshot.latencyFocus = calculateLatencyFocus();
        snapshot.stabilityFocus = calculateStabilityFocus(snapshot.latencyFocus);
        snapshot.throughputFocus = calculateThroughputFocus(snapshot.latencyFocus, snapshot.stabilityFocus);
        snapshot.recommendedBufferKb = computeRecommendedBuffer();
        WarpProxyManager warpProxyManager = Shindo.getInstance().getWarpProxyManager();
        if (warpProxyManager != null) {
            WarpProxyManager.WarpDiagnostics diagnostics = warpProxyManager.getDiagnostics();
            snapshot.warpProxyEnabled = diagnostics.isEnabled() && warpProxyEnabled;
            snapshot.warpStatus = diagnostics.getStatus();
            snapshot.warpResolver = diagnostics.getLastResolver();
            snapshot.warpLookupMs = diagnostics.getLastLookupDurationMs();
            snapshot.warpLastUpdatedAt = diagnostics.getLastUpdatedAt();
            snapshot.warpCacheHit = diagnostics.isCacheHit();
            snapshot.warpError = diagnostics.getLastError();
        } else {
            snapshot.warpProxyEnabled = warpProxyEnabled;
            snapshot.warpStatus = warpProxyEnabled ? WarpProxyManager.WarpStatus.IDLE : WarpProxyManager.WarpStatus.DISABLED;
            snapshot.warpResolver = null;
            snapshot.warpLookupMs = 0L;
            snapshot.warpLastUpdatedAt = 0L;
            snapshot.warpCacheHit = false;
            snapshot.warpError = null;
        }
        return snapshot;
    }

    public JsonObject toProfileJson() {
        JsonObject object = new JsonObject();
        object.addProperty("optimizerEnabled", optimizerEnabled);
        object.addProperty("networkMedium", networkMedium.name());
        object.addProperty("linkCapacityMbps", linkCapacityMbps);
        object.addProperty("aggressiveProfile", aggressiveProfile);
        object.addProperty("adaptiveBuffering", adaptiveBuffering);
        object.addProperty("tcpNoDelayEnabled", tcpNoDelayEnabled);
        object.addProperty("autoFlushEnabled", autoFlushEnabled);
        object.addProperty("preferNativeTransport", preferNativeTransport);
        object.addProperty("writeBufferKb", writeBufferKb);
        object.addProperty("burstFlushSmoothing", burstFlushSmoothing);
        object.addProperty("flushIntervalMs", flushIntervalMs);
        object.addProperty("flushPacketThreshold", flushPacketThreshold);
        object.addProperty("warpProxyEnabled", warpProxyEnabled);
        return object;
    }

    public void applyProfile(JsonObject object) {
        if (object == null) {
            return;
        }

        optimizerEnabled = JsonUtils.getBooleanProperty(object, "optimizerEnabled", optimizerEnabled);
        networkMedium = LinkMedium.fromKey(JsonUtils.getStringProperty(object, "networkMedium", networkMedium.name()));
        linkCapacityMbps = normalizeLinkCapacity(JsonUtils.getIntProperty(object, "linkCapacityMbps", linkCapacityMbps));
        aggressiveProfile = JsonUtils.getBooleanProperty(object, "aggressiveProfile", aggressiveProfile);
        adaptiveBuffering = JsonUtils.getBooleanProperty(object, "adaptiveBuffering", adaptiveBuffering);
        tcpNoDelayEnabled = JsonUtils.getBooleanProperty(object, "tcpNoDelayEnabled", tcpNoDelayEnabled);
        autoFlushEnabled = JsonUtils.getBooleanProperty(object, "autoFlushEnabled", autoFlushEnabled);
        preferNativeTransport = JsonUtils.getBooleanProperty(object, "preferNativeTransport", preferNativeTransport);
        writeBufferKb = normalizeWriteBuffer(JsonUtils.getIntProperty(object, "writeBufferKb", writeBufferKb));
        burstFlushSmoothing = JsonUtils.getBooleanProperty(object, "burstFlushSmoothing", burstFlushSmoothing);
        flushIntervalMs = Math.max(10, Math.min(120, JsonUtils.getIntProperty(object, "flushIntervalMs", flushIntervalMs)));
        flushPacketThreshold = Math.max(1, Math.min(12, JsonUtils.getIntProperty(object, "flushPacketThreshold", flushPacketThreshold)));
        warpProxyEnabled = JsonUtils.getBooleanProperty(object, "warpProxyEnabled", warpProxyEnabled);

        invalidateCaches();
        markDirty();
        refreshRuntime();
    }

    @Override
    public String getConfigId() {
        return "connectionTweaker";
    }

    @Override
    public String getDisplayName() {
        return TranslateText.NETWORK_OPTIMIZER_TOGGLE.getText();
    }

    @Override
    public TranslateText resolveCategoryLabel(String categoryKey) {
        if (categoryKey == null) {
            return TranslateText.NONE;
        }
        String key = categoryKey.toLowerCase(Locale.ROOT);
        switch (key) {
            case "overview":
                return TranslateText.NETWORK_CATEGORY_OVERVIEW;
            case "profile":
                return TranslateText.NETWORK_CATEGORY_PROFILE;
            case "transport":
                return TranslateText.NETWORK_CATEGORY_TRANSPORT;
            case "flow":
                return TranslateText.NETWORK_CATEGORY_FLOW;
            case "routing":
                return TranslateText.NETWORK_CATEGORY_ROUTING;
            default:
                return TranslateText.NONE;
        }
    }

    public void applyChannel(Channel channel) {
        if (channel == null) {
            return;
        }

        this.activeChannel = channel;
        this.pendingPackets = 0;
        this.lastFlushTimestamp = System.currentTimeMillis();

        try {
            channel.config().setOption(ChannelOption.TCP_NODELAY, resolveTcpNoDelay());
        } catch (Exception ignored) {
        }

        applyBufferConfiguration(channel);
    }

    public void onSendPacket(Channel channel, Packet<?> packet) {
        if (channel == null) {
            return;
        }

        if (!resolveAutoFlush()) {
            return;
        }

        if (!optimizerEnabled || !burstFlushSmoothing) {
            flushChannel(channel);
            return;
        }

        pendingPackets++;
        long now = System.currentTimeMillis();
        boolean intervalExceeded = now - lastFlushTimestamp >= Math.max(10, flushIntervalMs);
        boolean thresholdExceeded = pendingPackets >= Math.max(1, flushPacketThreshold);

        if (intervalExceeded || thresholdExceeded || isPriorityPacket(packet)) {
            flushChannel(channel);
        }
    }

    private void flushChannel(Channel channel) {
        try {
            channel.flush();
        } catch (Exception ignored) {
        } finally {
            pendingPackets = 0;
            lastFlushTimestamp = System.currentTimeMillis();
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        refreshRuntime();
        if (configDirty && System.currentTimeMillis() - lastConfigSave > 750L) {
            saveConfig();
        }
    }

    public void setTcpNoDelayEnabled(boolean enabled) {
        this.tcpNoDelayEnabled = enabled;
        cachedTcpNoDelay = null;
        markDirty();
    }

    public void setAutoFlushEnabled(boolean enabled) {
        this.autoFlushEnabled = enabled;
        cachedAutoFlush = null;
        markDirty();
    }

    public void setPreferNativeTransport(boolean enabled) {
        this.preferNativeTransport = enabled;
        cachedPreferNative = null;
        applyNativeTransportSetting(enabled);
        markDirty();
    }

    public void setWriteBufferKb(int value) {
        this.writeBufferKb = normalizeWriteBuffer(value);
        cachedWriteBuffer = null;
        markDirty();
    }

    public void setLinkCapacityMbps(int value) {
        this.linkCapacityMbps = normalizeLinkCapacity(value);
        cachedLinkCapacity = null;
        markDirty();
    }

    private void refreshRuntime() {
        boolean changed = false;

        if (cachedOptimizerEnabled == null || optimizerEnabled != cachedOptimizerEnabled) {
            cachedOptimizerEnabled = optimizerEnabled;
            changed = true;
            if (!optimizerEnabled) {
                savedState = captureState();
                applyDisabledDefaults();
            } else {
                restoreState(savedState);
                savedState = null;
            }
        }

        WarpProxyManager warpProxyManager = Shindo.getInstance().getWarpProxyManager();
        if (warpProxyManager != null) {
            if (cachedWarpProxyEnabled == null || cachedWarpProxyEnabled != warpProxyEnabled) {
                cachedWarpProxyEnabled = warpProxyEnabled;
                warpProxyManager.setEnabled(warpProxyEnabled);
                changed = true;
            }
        }

        if (!optimizerEnabled) {
            changed |= applyTcpNoDelayIfNeeded();
            changed |= applyAutoFlushIfNeeded();
            changed |= applyWriteBufferIfNeeded();
            changed |= applyPreferNativeIfNeeded();
            changed |= applyFlushSettingsIfNeeded();
            if (changed) {
                markDirty();
            }
            return;
        }

        boolean profileChanged = false;
        if (cachedMedium == null || cachedMedium != networkMedium) {
            cachedMedium = networkMedium;
            profileChanged = true;
        }
        if (cachedLinkCapacity == null || cachedLinkCapacity != linkCapacityMbps) {
            cachedLinkCapacity = linkCapacityMbps;
            profileChanged = true;
        }
        if (cachedAggressive == null || cachedAggressive != aggressiveProfile) {
            cachedAggressive = aggressiveProfile;
            profileChanged = true;
        }
        if (cachedAdaptive == null || cachedAdaptive != adaptiveBuffering) {
            cachedAdaptive = adaptiveBuffering;
            profileChanged = true;
        }

        if (profileChanged && adaptiveBuffering) {
            applyAdaptiveProfile();
            changed = true;
        }

        changed |= applyTcpNoDelayIfNeeded();
        changed |= applyAutoFlushIfNeeded();
        changed |= applyWriteBufferIfNeeded();
        changed |= applyPreferNativeIfNeeded();
        changed |= applyFlushSettingsIfNeeded();

        if (changed) {
            markDirty();
        }
    }

    private boolean applyTcpNoDelayIfNeeded() {
        if (cachedTcpNoDelay != null && cachedTcpNoDelay == resolveTcpNoDelay()) {
            return false;
        }
        boolean target = resolveTcpNoDelay();
        cachedTcpNoDelay = target;
        if (activeChannel != null) {
            try {
                activeChannel.config().setOption(ChannelOption.TCP_NODELAY, target);
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    private boolean applyAutoFlushIfNeeded() {
        boolean target = resolveAutoFlush();
        if (cachedAutoFlush != null && cachedAutoFlush == target) {
            return false;
        }
        cachedAutoFlush = target;
        return true;
    }

    private boolean applyWriteBufferIfNeeded() {
        int target = resolveWriteBuffer();
        if (cachedWriteBuffer != null && cachedWriteBuffer == target) {
            return false;
        }
        writeBufferKb = target;
        cachedWriteBuffer = target;
        if (activeChannel != null) {
            applyBufferConfiguration(activeChannel);
        }
        return true;
    }

    private boolean applyPreferNativeIfNeeded() {
        if (cachedPreferNative != null && cachedPreferNative == resolveNativeTransport()) {
            return false;
        }
        boolean target = resolveNativeTransport();
        cachedPreferNative = target;
        applyNativeTransportSetting(target);
        return true;
    }

    private boolean applyFlushSettingsIfNeeded() {
        boolean changed = false;
        if (cachedBurstSmoothing == null || cachedBurstSmoothing != burstFlushSmoothing) {
            cachedBurstSmoothing = burstFlushSmoothing;
            changed = true;
        }
        if (cachedFlushInterval == null || cachedFlushInterval != flushIntervalMs) {
            flushIntervalMs = Math.max(10, Math.min(flushIntervalMs, 120));
            cachedFlushInterval = flushIntervalMs;
            changed = true;
        }
        if (cachedFlushThreshold == null || cachedFlushThreshold != flushPacketThreshold) {
            flushPacketThreshold = Math.max(1, Math.min(flushPacketThreshold, 12));
            cachedFlushThreshold = flushPacketThreshold;
            changed = true;
        }
        return changed;
    }

    private void applyAdaptiveProfile() {
        writeBufferKb = computeRecommendedBuffer();
        cachedWriteBuffer = null;

        if (aggressiveProfile) {
            tcpNoDelayEnabled = true;
            autoFlushEnabled = true;
            burstFlushSmoothing = true;
            flushIntervalMs = Math.max(15, baseFlushInterval() - 10);
            flushPacketThreshold = Math.max(2, baseFlushThreshold() - 1);
        } else {
            flushIntervalMs = baseFlushInterval();
            flushPacketThreshold = baseFlushThreshold();
        }

        cachedTcpNoDelay = null;
        cachedAutoFlush = null;
        cachedBurstSmoothing = null;
        cachedFlushInterval = null;
        cachedFlushThreshold = null;
    }

    private void applyDisabledDefaults() {
        tcpNoDelayEnabled = DEFAULT_TCP_NODELAY;
        autoFlushEnabled = DEFAULT_AUTO_FLUSH;
        preferNativeTransport = DEFAULT_NATIVE_TRANSPORT;
        writeBufferKb = DEFAULT_WRITE_BUFFER_KB;
        burstFlushSmoothing = false;
        flushIntervalMs = 50;
        flushPacketThreshold = 6;
        cachedTcpNoDelay = null;
        cachedAutoFlush = null;
        cachedPreferNative = null;
        cachedWriteBuffer = null;
        cachedBurstSmoothing = null;
        cachedFlushInterval = null;
        cachedFlushThreshold = null;
        cachedWarpProxyEnabled = null;
    }

    private SavedState captureState() {
        SavedState state = new SavedState();
        state.networkMedium = networkMedium;
        state.linkCapacity = linkCapacityMbps;
        state.aggressiveProfile = aggressiveProfile;
        state.adaptiveBuffering = adaptiveBuffering;
        state.tcpNoDelay = tcpNoDelayEnabled;
        state.autoFlush = autoFlushEnabled;
        state.preferNative = preferNativeTransport;
        state.writeBuffer = writeBufferKb;
        state.burstSmoothing = burstFlushSmoothing;
        state.flushInterval = flushIntervalMs;
        state.flushThreshold = flushPacketThreshold;
        return state;
    }

    private void restoreState(SavedState state) {
        if (state == null) {
            cachedTcpNoDelay = null;
            cachedAutoFlush = null;
            cachedPreferNative = null;
            cachedWriteBuffer = null;
            cachedBurstSmoothing = null;
            cachedFlushInterval = null;
            cachedFlushThreshold = null;
            cachedWarpProxyEnabled = null;
            return;
        }
        networkMedium = state.networkMedium;
        linkCapacityMbps = state.linkCapacity;
        aggressiveProfile = state.aggressiveProfile;
        adaptiveBuffering = state.adaptiveBuffering;
        tcpNoDelayEnabled = state.tcpNoDelay;
        autoFlushEnabled = state.autoFlush;
        preferNativeTransport = state.preferNative;
        writeBufferKb = state.writeBuffer;
        burstFlushSmoothing = state.burstSmoothing;
        flushIntervalMs = state.flushInterval;
        flushPacketThreshold = state.flushThreshold;

        cachedMedium = null;
        cachedLinkCapacity = null;
        cachedAggressive = null;
        cachedAdaptive = null;
        cachedTcpNoDelay = null;
        cachedAutoFlush = null;
        cachedPreferNative = null;
        cachedWriteBuffer = null;
        cachedBurstSmoothing = null;
        cachedFlushInterval = null;
        cachedFlushThreshold = null;
        cachedWarpProxyEnabled = null;
    }

    private void applyBufferConfiguration(Channel channel) {
        int bufferKb = resolveWriteBuffer();
        int highWaterMark = Math.max(MIN_WRITE_BUFFER_KB, bufferKb) * 1024;
        int lowWaterMark = Math.max(32 * 1024, highWaterMark / 2);

        try {
            channel.config().setOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, highWaterMark);
            channel.config().setOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, lowWaterMark);
        } catch (Exception ignored) {
        }
    }

    private void applyNativeTransportSetting(boolean enabled) {
        try {
            mc.gameSettings.useNativeTransport = enabled;
            mc.gameSettings.saveOptions();
        } catch (Exception ignored) {
        }
    }

    private void markDirty() {
        configDirty = true;
    }

    private void invalidateCaches() {
        cachedOptimizerEnabled = null;
        cachedMedium = null;
        cachedLinkCapacity = null;
        cachedAggressive = null;
        cachedAdaptive = null;
        cachedTcpNoDelay = null;
        cachedAutoFlush = null;
        cachedPreferNative = null;
        cachedWriteBuffer = null;
        cachedBurstSmoothing = null;
        cachedFlushInterval = null;
        cachedFlushThreshold = null;
    }

    private boolean resolveTcpNoDelay() {
        return optimizerEnabled ? tcpNoDelayEnabled : DEFAULT_TCP_NODELAY;
    }

    private boolean resolveAutoFlush() {
        return optimizerEnabled ? autoFlushEnabled : DEFAULT_AUTO_FLUSH;
    }

    private boolean resolveNativeTransport() {
        return optimizerEnabled ? preferNativeTransport : DEFAULT_NATIVE_TRANSPORT;
    }

    private int resolveWriteBuffer() {
        return optimizerEnabled ? normalizeWriteBuffer(writeBufferKb) : DEFAULT_WRITE_BUFFER_KB;
    }

    private int normalizeWriteBuffer(int value) {
        return Math.max(MIN_WRITE_BUFFER_KB, Math.min(MAX_WRITE_BUFFER_KB, value));
    }

    private int normalizeLinkCapacity(int value) {
        return Math.max(10, Math.min(1000, value));
    }

    private boolean isPriorityPacket(Packet<?> packet) {
        if (packet == null) {
            return false;
        }
        String simpleName = packet.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return simpleName.contains("keepalive")
                || simpleName.contains("handshake")
                || simpleName.contains("clientstatus")
                || simpleName.contains("login");
    }

    private int computeRecommendedBuffer() {
        int base;
        switch (networkMedium) {
            case WIRED:
                base = 512;
                break;
            case WIRELESS:
                base = 384;
                break;
            case MOBILE:
                base = 256;
                break;
            default:
                base = 512;
        }

        double capacity = Math.max(10, Math.min(1000, linkCapacityMbps));
        double capacityFactor = Math.sqrt(capacity) * (aggressiveProfile ? 24D : 16D);
        int recommended = (int) Math.round(base + capacityFactor);
        if (aggressiveProfile) {
            recommended += 128;
        }
        return normalizeWriteBuffer(recommended);
    }

    private int baseFlushInterval() {
        switch (networkMedium) {
            case WIRED:
                return aggressiveProfile ? 28 : 38;
            case WIRELESS:
                return aggressiveProfile ? 34 : 46;
            case MOBILE:
                return aggressiveProfile ? 38 : 52;
            default:
                return 42;
        }
    }

    private int baseFlushThreshold() {
        switch (networkMedium) {
            case WIRED:
                return aggressiveProfile ? 3 : 4;
            case WIRELESS:
                return aggressiveProfile ? 4 : 5;
            case MOBILE:
                return aggressiveProfile ? 5 : 6;
            default:
                return 4;
        }
    }

    private float calculateLatencyFocus() {
        float mediumBias;
        switch (networkMedium) {
            case WIRED:
                mediumBias = 0.75F;
                break;
            case WIRELESS:
                mediumBias = 0.6F;
                break;
            case MOBILE:
                mediumBias = 0.5F;
                break;
            default:
                mediumBias = 0.6F;
        }
        float aggressiveBias = aggressiveProfile ? 0.15F : 0.0F;
        return clamp01(mediumBias + aggressiveBias);
    }

    private float calculateStabilityFocus(float latencyFocus) {
        float mediumBase;
        switch (networkMedium) {
            case WIRED:
                mediumBase = 0.8F;
                break;
            case WIRELESS:
                mediumBase = 0.65F;
                break;
            case MOBILE:
                mediumBase = 0.55F;
                break;
            default:
                mediumBase = 0.65F;
        }
        float adaptiveBonus = adaptiveBuffering ? 0.1F : 0.0F;
        float smoothingBonus = burstFlushSmoothing ? 0.05F : 0.0F;
        return clamp01((mediumBase + adaptiveBonus + smoothingBonus) - (latencyFocus * 0.25F));
    }

    private float calculateThroughputFocus(float latencyFocus, float stabilityFocus) {
        float bufferRatio = (float) resolveWriteBuffer() / MAX_WRITE_BUFFER_KB;
        float capacityRatio = Math.min(1F, linkCapacityMbps / 750F);
        float base = (bufferRatio * 0.4F) + (capacityRatio * 0.6F);
        return clamp01(base - (latencyFocus * 0.2F) + (stabilityFocus * 0.15F));
    }

    private float clamp01(float value) {
        if (value < 0F) {
            return 0F;
        }
        return Math.min(value, 1F);
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonElement element = gson.fromJson(reader, JsonElement.class);
            if (element == null || !element.isJsonObject()) {
                return;
            }
            JsonObject object = element.getAsJsonObject();

            optimizerEnabled = JsonUtils.getBooleanProperty(object, "optimizerEnabled", optimizerEnabled);
            networkMedium = LinkMedium.fromKey(JsonUtils.getStringProperty(object, "networkMedium", networkMedium.name()));
            linkCapacityMbps = normalizeLinkCapacity(JsonUtils.getIntProperty(object, "linkCapacityMbps", linkCapacityMbps));
            aggressiveProfile = JsonUtils.getBooleanProperty(object, "aggressiveProfile", aggressiveProfile);
            adaptiveBuffering = JsonUtils.getBooleanProperty(object, "adaptiveBuffering", adaptiveBuffering);
            tcpNoDelayEnabled = JsonUtils.getBooleanProperty(object, "tcpNoDelayEnabled", tcpNoDelayEnabled);
            autoFlushEnabled = JsonUtils.getBooleanProperty(object, "autoFlushEnabled", autoFlushEnabled);
            preferNativeTransport = JsonUtils.getBooleanProperty(object, "preferNativeTransport", preferNativeTransport);
            writeBufferKb = normalizeWriteBuffer(JsonUtils.getIntProperty(object, "writeBufferKb", writeBufferKb));
            burstFlushSmoothing = JsonUtils.getBooleanProperty(object, "burstFlushSmoothing", burstFlushSmoothing);
            flushIntervalMs = Math.max(10, Math.min(120, JsonUtils.getIntProperty(object, "flushIntervalMs", flushIntervalMs)));
            flushPacketThreshold = Math.max(1, Math.min(12, JsonUtils.getIntProperty(object, "flushPacketThreshold", flushPacketThreshold)));
            warpProxyEnabled = JsonUtils.getBooleanProperty(object, "warpProxyEnabled", warpProxyEnabled);
        } catch (IOException exception) {
            ShindoLogger.error("Failed to load connection tweaker configuration", exception);
        }
    }

    private void saveConfig() {
        try {
            if (!configFile.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                configFile.getParentFile().mkdirs();
            }
            JsonObject object = new JsonObject();
            object.addProperty("optimizerEnabled", optimizerEnabled);
            object.addProperty("networkMedium", networkMedium.name());
            object.addProperty("linkCapacityMbps", linkCapacityMbps);
            object.addProperty("aggressiveProfile", aggressiveProfile);
            object.addProperty("adaptiveBuffering", adaptiveBuffering);
            object.addProperty("tcpNoDelayEnabled", tcpNoDelayEnabled);
            object.addProperty("autoFlushEnabled", autoFlushEnabled);
            object.addProperty("preferNativeTransport", preferNativeTransport);
            object.addProperty("writeBufferKb", writeBufferKb);
            object.addProperty("burstFlushSmoothing", burstFlushSmoothing);
            object.addProperty("flushIntervalMs", flushIntervalMs);
            object.addProperty("flushPacketThreshold", flushPacketThreshold);
            object.addProperty("warpProxyEnabled", warpProxyEnabled);

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(object, writer);
            }
            configDirty = false;
            lastConfigSave = System.currentTimeMillis();
        } catch (IOException exception) {
            ShindoLogger.error("Failed to save connection tweaker configuration", exception);
        }
    }

    public enum LinkMedium implements PropertyEnum {
        WIRED(TranslateText.NETWORK_MEDIUM_WIRED),
        WIRELESS(TranslateText.NETWORK_MEDIUM_WIRELESS),
        MOBILE(TranslateText.NETWORK_MEDIUM_MOBILE);

        private final TranslateText translate;

        LinkMedium(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }

        @Override
        public String getDisplayName() {
            return translate.getText();
        }

        public static LinkMedium fromKey(String key) {
            if (key == null) {
                return WIRED;
            }
            for (LinkMedium medium : values()) {
                if (medium.name().equalsIgnoreCase(key) || medium.getTranslate().getKey().equalsIgnoreCase(key)) {
                    return medium;
                }
            }
            return WIRED;
        }
    }

    private static class SavedState {
        private LinkMedium networkMedium;
        private int linkCapacity;
        private boolean aggressiveProfile;
        private boolean adaptiveBuffering;
        private boolean tcpNoDelay;
        private boolean autoFlush;
        private boolean preferNative;
        private int writeBuffer;
        private boolean burstSmoothing;
        private int flushInterval;
        private int flushThreshold;
    }

    @Getter
    public static class ProfileSnapshot {
        private boolean optimizerEnabled;
        private LinkMedium networkMedium;
        private int linkCapacityMbps;
        private boolean aggressiveProfile;
        private boolean adaptiveBuffering;
        private boolean tcpNoDelay;
        private boolean autoFlush;
        private boolean preferNative;
        private int writeBufferKb;
        private boolean burstSmoothing;
        private int flushIntervalMs;
        private int flushThreshold;
        private float latencyFocus;
        private float stabilityFocus;
        private float throughputFocus;
        private int recommendedBufferKb;
        private boolean warpProxyEnabled;
        private WarpProxyManager.WarpStatus warpStatus;
        private String warpResolver;
        private long warpLookupMs;
        private long warpLastUpdatedAt;
        private boolean warpCacheHit;
        private String warpError;

    }
}
