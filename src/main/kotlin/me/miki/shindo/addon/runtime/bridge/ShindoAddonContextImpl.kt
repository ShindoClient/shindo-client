package me.miki.shindo.addon.runtime.bridge

import me.miki.shindo.Shindo
import me.miki.shindo.addon.api.ServiceRegistry
import me.miki.shindo.addon.api.ShindoAddonContext
import me.miki.shindo.addon.api.animation.IAnimationFactory
import me.miki.shindo.addon.api.clipboard.IClipboardProvider
import me.miki.shindo.addon.api.comp.ICompFactory
import me.miki.shindo.addon.api.config.IAddonConfigStorage
import me.miki.shindo.addon.api.file.IFileProvider
import me.miki.shindo.addon.api.color.IColorProvider
import me.miki.shindo.addon.api.hud.AddonHudElement
import me.miki.shindo.addon.api.hud.HudLayoutService
import me.miki.shindo.addon.api.language.ITranslateProvider
import me.miki.shindo.addon.api.notification.INotificationProvider
import me.miki.shindo.addon.api.render.IRenderContext
import me.miki.shindo.addon.api.sound.ISoundProvider
import me.miki.shindo.addon.api.util.IMouseUtils
import me.miki.shindo.management.addons.config.AddonConfigRegistry
import java.util.IdentityHashMap

/**
 * Implementação do ShindoAddonContext que fornece acesso via interfaces.
 */
class ShindoAddonContextImpl(
    override val addonId: String,
    private val serviceRegistryImpl: ServiceRegistry
) : ShindoAddonContext {

    private val _renderAdapter by lazy { NanoVGRenderContextAdapter() }
    private val _animFactory by lazy { AnimationFactoryImpl() }
    private val _compFactory by lazy { CompFactoryImpl() }
    private val _translateProvider by lazy { TranslateProviderImpl() }
    private val _mouseUtils by lazy { MouseUtilsAdapter() }
    private val _colorProvider by lazy { ColorProviderAdapter() }
    private val _fileProvider by lazy { FileProviderAdapter() }
    private val _soundProvider by lazy { SoundProviderAdapter() }
    private val _notificationProvider by lazy { NotificationProviderAdapter() }
    private val _clipboardProvider by lazy { ClipboardProviderAdapter() }
    private val ownedHudMap = IdentityHashMap<AddonHudElement, AddonHudElement>()

    override val renderContext: IRenderContext?
        get() = if (Shindo.getInstance().nanoVGManager != null) _renderAdapter else null

    override val animationFactory: IAnimationFactory
        get() = _animFactory

    override val compFactory: ICompFactory
        get() = _compFactory

    override val translateProvider: ITranslateProvider
        get() = _translateProvider

    override val mouseUtils: IMouseUtils
        get() = _mouseUtils

    override val colorProvider: IColorProvider
        get() = _colorProvider

    override val fileProvider: IFileProvider
        get() = _fileProvider

    override val soundProvider: ISoundProvider
        get() = _soundProvider

    override val notificationProvider: INotificationProvider
        get() = _notificationProvider

    override val clipboardProvider: IClipboardProvider
        get() = _clipboardProvider

    override val serviceRegistry: ServiceRegistry
        get() = serviceRegistryImpl

    override fun getAddonConfigStorage(addonId: String): IAddonConfigStorage =
        AddonConfigRegistry.getOrCreate(addonId)

    override fun registerEvents(listener: Any) {
        Shindo.getInstance().eventManager.register(listener)
    }

    override fun unregisterEvents(listener: Any) {
        Shindo.getInstance().eventManager.unregister(listener)
    }

    override fun registerHud(hud: AddonHudElement) {
        val hudLayout = serviceRegistry.get(HudLayoutService::class) ?: return
        val owned = OwnedAddonHudElement(addonId, hud)
        synchronized(ownedHudMap) {
            ownedHudMap[hud] = owned
        }
        hudLayout.register(owned)
    }

    override fun unregisterHud(hud: AddonHudElement) {
        val hudLayout = serviceRegistry.get(HudLayoutService::class) ?: return
        val owned = synchronized(ownedHudMap) { ownedHudMap.remove(hud) }
        if (owned != null) {
            hudLayout.unregister(owned)
        } else {
            hudLayout.unregister(hud)
        }
    }

    override fun unregisterAllHuds() {
        val hudLayout = serviceRegistry.get(HudLayoutService::class) ?: return
        synchronized(ownedHudMap) {
            val iterator = ownedHudMap.values.iterator()
            while (iterator.hasNext()) {
                hudLayout.unregister(iterator.next())
                iterator.remove()
            }
        }
        hudLayout.unregisterByOwner(addonId)
    }

    private class OwnedAddonHudElement(
        private val ownerId: String,
        private val delegate: AddonHudElement
    ) : AddonHudElement by delegate {
        override fun ownerAddonId(): String = ownerId
    }
}
