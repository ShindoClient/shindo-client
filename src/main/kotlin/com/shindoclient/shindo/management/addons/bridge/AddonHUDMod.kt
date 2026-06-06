package com.shindoclient.shindo.management.addons.bridge

import com.shindoclient.addon.api.hud.AddonHUD
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.HUDMod

/**
 * Bridge wrapper that makes an [AddonHUD] from the AddonAPI appear
 * as a [HUDMod] in the client's HUD system.
 *
 * This allows the HUD to be visible in the HUD editor (GuiEditHUD),
 * repositioned by drag, enabled/disabled, and persisted in profiles.
 *
 * Rendering is NOT handled here — it stays in [ExternalAddon.renderHuds]
 * which checks [isToggled] before each frame and calls [syncToAddonHUD]
 * so the addon sees the position set by the editor.
 */
class AddonHUDMod(
    val addonHUD: AddonHUD,
) : HUDMod(
    nameTranslate = TranslateText.NONE,
    descriptionText = TranslateText.NONE,
    icon = "",
) {
    init {
        setX(addonHUD.x)
        setY(addonHUD.y)
        setWidth(addonHUD.width)
        setHeight(addonHUD.height)
    }

    override fun getName(): String = addonHUD.name
    override fun getDescription(): String = ""
    override fun getIcon(): String? = null

    fun syncToAddonHUD() {
        addonHUD.x = getX()
        addonHUD.y = getY()
        addonHUD.width = getWidth()
        addonHUD.height = getHeight()
    }

    override fun setX(x: Int) {
        super.setX(x)
        addonHUD.x = x
    }

    override fun setY(y: Int) {
        super.setY(y)
        addonHUD.y = y
    }

    override fun setWidth(width: Int) {
        super.setWidth(width)
        addonHUD.width = width
    }

    override fun setHeight(height: Int) {
        super.setHeight(height)
        addonHUD.height = height
    }

    // Rendering is managed by ExternalAddon — do NOT register with EventManager.
    override fun onEnable() {}
    override fun onDisable() {}
}
