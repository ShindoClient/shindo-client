@file:JvmName("ModuleExtensions")

package me.miki.extensions.modules

import me.miki.shindo.Shindo
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.settings.Setting


fun Mod.getModByTranslateKey(key: String): Mod? {
    return Shindo.getInstance().modManager.getModByTranslateKey(key)
}

fun Mod.getSettingsByMod (mod: Mod): ArrayList<Setting>? {
    return Shindo.getInstance().modManager.getSettingsByMod(mod)
}

