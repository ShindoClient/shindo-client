package me.miki.shindo.addon.api

import me.miki.shindo.addon.api.animation.IAnimationFactory
import me.miki.shindo.addon.api.clipboard.IClipboardProvider
import me.miki.shindo.addon.api.comp.ICompFactory
import me.miki.shindo.addon.api.config.IAddonConfigStorage
import me.miki.shindo.addon.api.file.IFileProvider
import me.miki.shindo.addon.api.color.IColorProvider
import me.miki.shindo.addon.api.language.ITranslateProvider
import me.miki.shindo.addon.api.notification.INotificationProvider
import me.miki.shindo.addon.api.render.IRenderContext
import me.miki.shindo.addon.api.sound.ISoundProvider
import me.miki.shindo.addon.api.util.IMouseUtils
import me.miki.shindo.addon.api.hud.AddonHudElement

/**
 * Contexto fornecido aos addons no [ShindoAddon.onLoad].
 * Dá acesso ao sistema de eventos, render, animações, componentes, cores, arquivos, sons e config via interfaces.
 * Addons usam apenas a addon-api - sem depender do JAR do client.
 */
interface ShindoAddonContext {
    /**
     * ID do addon atual.
     */
    val addonId: String


    /**
     * Contexto de render 2D (NanoVG). Use para desenhar textos, retângulos, etc.
     * Pode ser null antes do client estar pronto.
     */
    val renderContext: IRenderContext?

    /**
     * Factory para criar animações.
     */
    val animationFactory: IAnimationFactory

    /**
     * Factory para criar componentes de UI.
     */
    val compFactory: ICompFactory

    /**
     * Provedor de textos traduzidos (TranslateText / LanguageManager).
     */
    val translateProvider: ITranslateProvider

    /**
     * Utilitários de mouse (ex: isInside).
     */
    val mouseUtils: IMouseUtils

    /**
     * Sistema de cores (accent, tema).
     */
    val colorProvider: IColorProvider

    /**
     * Acesso a arquivos (config do addon).
     */
    val fileProvider: IFileProvider

    /**
     * Reprodutor de sons.
     */
    val soundProvider: ISoundProvider

    /**
     * Envio de notificações/toasts.
     */
    val notificationProvider: INotificationProvider

    /**
     * Acesso ao clipboard do sistema.
     */
    val clipboardProvider: IClipboardProvider

    /**
     * Registro de serviços expostos pelo client.
     *
     * Use este registro para obter serviços compartilhados como HypixelApiProvider,
     * ScoreboardService, etc, sem acoplar o addon às implementações internas.
     */
    val serviceRegistry: ServiceRegistry

    /**
     * Armazenamento de config do addon persistido no perfil.
     * Use o id do addon ([AddonMetadata.id]). Fallback: configs de addons removidos são ignorados ao carregar.
     */
    fun getAddonConfigStorage(addonId: String): IAddonConfigStorage

    /**
     * Registra o listener no EventManager do client.
     * O addon deve ter métodos com @EventTarget que recebem IEventTick, IEventRender2D, IEventKey, etc.
     */
    fun registerEvents(listener: Any)

    /**
     * Remove o listener do EventManager.
     */
    fun unregisterEvents(listener: Any)

    /**
     * Registra HUD do addon usando owner automático.
     */
    fun registerHud(hud: AddonHudElement)

    /**
     * Remove HUD previamente registrado.
     */
    fun unregisterHud(hud: AddonHudElement)

    /**
     * Remove todos os HUDs do addon atual.
     */
    fun unregisterAllHuds()
}
