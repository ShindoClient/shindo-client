package me.miki.shindo.api.compat

import me.miki.shindo.management.settings.config.ConfigOwner

/**
 * ConfigOwner dummy para comps da API (Slider, Keybind, ColorPicker, Dropdown).
 * Evita registrar settings no addonManager. Usado como parent ao criar settings internos.
 */
internal object ApiCompConfigOwner : ConfigOwner {
    override fun getConfigId(): String = "api-comp-dummy"
    override fun getDisplayName(): String = "api-comp"
}
