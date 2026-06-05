package me.miki.shindo.management.addons

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import java.util.Locale

open class Addon(
    val name: String,
    private val descriptionText: String,
    private val descriptionTranslate: TranslateText? = null,
    val icon: String,
    val type: AddonType,
) : ConfigOwner {
    open val isBuiltIn: Boolean = true
    open val showToggle: Boolean = true

    val animation = SimpleAnimation()
    val hoverAnimation = SimpleAnimation()
    val settingsHoverAnimation = SimpleAnimation()

    private var toggled = false
    private var hide = false

    init {
        setup()
    }

    fun getDescription(): String = descriptionTranslate?.getText() ?: descriptionText

    open fun setup() {
    }

    open fun onEnable() {
        Shindo.getInstance().getEventManager().register(this)
        ShindoLogger.info("$name was enabled")
    }

    open fun onDisable() {
        Shindo.getInstance().getEventManager().unregister(this)
        ShindoLogger.info("$name was disabled")
    }

    fun toggle() {
        setToggled(!toggled, true)
    }

    fun setToggled(
        toggled: Boolean,
        sound: Boolean,
    ) {
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

    fun isToggled(): Boolean = toggled

    fun isHide(): Boolean = hide

    fun setHide(hide: Boolean) {
        this.hide = hide
        if (hide && toggled) {
            setToggled(false, false)
        }
    }

    override fun getConfigId(): String = name.lowercase(Locale.ROOT).replace(' ', '_')

    override fun getDisplayName(): String = name
}
