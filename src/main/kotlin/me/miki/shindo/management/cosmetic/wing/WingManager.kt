package me.miki.shindo.management.cosmetic.wing

import me.miki.shindo.api.roles.Role
import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.management.cosmetic.CosmeticRoleTextMapper
import me.miki.shindo.management.cosmetic.wing.impl.NormalWing
import me.miki.shindo.management.cosmetic.wing.impl.Wing
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import java.util.UUID

class WingManager {

    private val wings = ArrayList<Wing>()
    private var currentWing: Wing? = null

    init {
        wings.add(NormalWing("None", null, null, WingCategory.ALL, Role.MEMBER))
        wings.add(NormalWing("Aether", null, null, WingCategory.CLASSIC, Role.MEMBER))
        wings.add(NormalWing("Valkyrie", null, null, WingCategory.FANTASY, Role.GOLD))
        wings.add(NormalWing("Neon Drift", null, null, WingCategory.TECH, Role.DIAMOND))

        val savedName = InternalSettingsMod.instance.wingConfigName ?: "None"
        currentWing = getWingByName(savedName)
    }

    fun getWings(): ArrayList<Wing> = wings

    fun getCurrentWing(): Wing? = currentWing

    fun setCurrentWing(wing: Wing?) {
        currentWing = wing
        wing?.let { InternalSettingsMod.instance.wingConfigName = it.getName() }
    }

    fun getWingByName(name: String): Wing {
        return wings.firstOrNull { it.getName() == name } ?: wings.first()
    }

    fun canUseWing(uuid: UUID, wing: Wing): Boolean {
        return RoleManager.hasAtLeast(uuid, wing.getRequiredRole())
    }

    fun getTranslateError(role: Role): TranslateText = CosmeticRoleTextMapper.getTranslateError(role)

    fun getTranslateText(role: Role): TranslateText = CosmeticRoleTextMapper.getTranslateText(role)
}
