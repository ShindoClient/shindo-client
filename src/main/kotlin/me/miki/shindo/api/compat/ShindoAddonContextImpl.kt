package me.miki.shindo.api.compat

import me.miki.shindo.Shindo
import me.miki.client_api.ServiceRegistry
import me.miki.client_api.ShindoAddonContext
import me.miki.client_api.animation.IAnimationFactory
import me.miki.client_api.clipboard.IClipboardProvider
import me.miki.client_api.comp.ICompFactory
import me.miki.client_api.config.IAddonConfigStorage
import me.miki.client_api.file.IFileProvider
import me.miki.client_api.color.IColorProvider
import me.miki.client_api.language.ITranslateProvider
import me.miki.client_api.notification.INotificationProvider
import me.miki.client_api.render.IRenderContext
import me.miki.client_api.sound.ISoundProvider
import me.miki.client_api.util.IMouseUtils
import me.miki.shindo.management.addons.config.AddonConfigRegistry

/**
 * Implementação do ShindoAddonContext que fornece acesso via interfaces.
 */
class ShindoAddonContextImpl(
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
}
