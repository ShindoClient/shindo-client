package me.miki.shindo.management.addons

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.utils.animation.simple.SimpleAnimation

open class Addon(
    val name: String,
    val description: String,
    val icon: String,
    val type: AddonType
) : ConfigOwner {

    val animation = SimpleAnimation()

    private var toggled = false

    init {
        setup()
    }

    open fun setup() {
    }

    open fun onEnable() {
        Shindo.getInstance().eventManager.register(this)
        ShindoLogger.info("[ADDON] $name was enabled")
    }

    open fun onDisable() {
        Shindo.getInstance().eventManager.unregister(this)
        ShindoLogger.info("[ADDON] $name was disabled")
    }

    fun toggle() {
        setToggled(!toggled, true)
    }

    fun setToggled(toggled: Boolean, sound: Boolean) {
        this.toggled = toggled
        if (toggled) {
            onEnable()
            if (sound) Shindo.getInstance().addonManager.playToggleSound(true)
        } else {
            onDisable()
            if (sound) Shindo.getInstance().addonManager.playToggleSound(false)
        }
    }

    fun isToggled(): Boolean {
        return toggled
    }

    override fun getConfigId(): String {
        return name.toLowerCase().replace(' ', '_')
    }

    override fun getDisplayName(): String {
        return name
    }
}
