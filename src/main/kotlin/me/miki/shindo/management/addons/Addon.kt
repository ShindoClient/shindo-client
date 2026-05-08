package me.miki.shindo.management.addons

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import java.util.*

open class Addon(
    val name: String,
    private val descriptionText: String,
    private val descriptionTranslate: TranslateText? = null,
    val icon: String,
    val type: AddonType
) : ConfigOwner {

    /** true para addons built-in (ex: RPO), false para addons externos (JAR). */
    open val isBuiltIn: Boolean = true

    /** Se true, mostra o botão toggle na UI. Addons externos podem definir addon.showToggle=false no addon.properties. */
    open val showToggle: Boolean = true

    val animation = SimpleAnimation()
    val hoverAnimation = SimpleAnimation()
    val settingsHoverAnimation = SimpleAnimation()

    private var toggled = false
    private var hide = false

    init {
        setup()
    }

    fun getDescription(): String {
        return descriptionTranslate?.getText() ?: descriptionText
    }

    open fun setup() {
    }

    open fun onEnable() {
        Shindo.getInstance().getEventManager().register(this)
        ShindoLogger.info("[ADDON] $name was enabled")
    }

    open fun onDisable() {
        Shindo.getInstance().getEventManager().unregister(this)
        ShindoLogger.info("[ADDON] $name was disabled")
    }

    fun toggle() {
        setToggled(!toggled, true)
    }

    fun setToggled(toggled: Boolean, sound: Boolean) {
        // Não permite habilitar addons hidden
        if (toggled && hide) {
            this.toggled = false
            return
        }

        this.toggled = toggled
        if (toggled) {
            onEnable()
            if (sound) Shindo.getInstance().getAddonManager().playToggleSound(true)
        } else {
            onDisable()
            if (sound) Shindo.getInstance().getAddonManager().playToggleSound(false)
        }
    }

    fun isToggled(): Boolean {
        return toggled
    }

    fun isHide(): Boolean {
        return hide
    }

    fun setHide(hide: Boolean) {
        this.hide = hide
        // Se o addon estiver habilitado e for marcado como hidden, desabilita
        if (hide && toggled) {
            setToggled(false, false)
        }
    }

    override fun getConfigId(): String {
        return name.toLowerCase(Locale.ROOT).replace(' ', '_')
    }

    override fun getDisplayName(): String {
        return name
    }
}


