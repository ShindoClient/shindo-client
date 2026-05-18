package me.miki.shindo.management.cosmetic.bandana

import me.miki.shindo.api.roles.Role
import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.management.cosmetic.CosmeticRoleTextMapper
import me.miki.shindo.management.cosmetic.bandana.impl.Bandana
import me.miki.shindo.management.cosmetic.bandana.impl.NormalBandana
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import java.util.*

class BandanaManager {
    private val bandanas = ArrayList<Bandana>()
    private var currentBandana: Bandana? = null

    init {
        bandanas.add(NormalBandana("None", null, null, BandanaCategory.ALL, Role.MEMBER))
        bandanas.add(NormalBandana("Street", null, null, BandanaCategory.BASIC, Role.MEMBER))
        bandanas.add(NormalBandana("Striker", null, null, BandanaCategory.SPORTS, Role.GOLD))
        bandanas.add(NormalBandana("Crimson Ops", null, null, BandanaCategory.ELITE, Role.DIAMOND))

        val savedName = InternalSettingsMod.instance.bandanaConfigName ?: "None"
        currentBandana = getBandanaByName(savedName)
    }

    fun getBandanas(): ArrayList<Bandana> = bandanas

    fun getCurrentBandana(): Bandana? = currentBandana

    fun setCurrentBandana(bandana: Bandana?) {
        currentBandana = bandana
        bandana?.let { InternalSettingsMod.instance.bandanaConfigName = it.getName() }
    }

    fun getBandanaByName(name: String): Bandana = bandanas.firstOrNull { it.getName() == name } ?: bandanas.first()

    fun canUseBandana(
        uuid: UUID,
        bandana: Bandana,
    ): Boolean = RoleManager.hasAtLeast(uuid, bandana.getRequiredRole())

    fun getTranslateError(role: Role): TranslateText = CosmeticRoleTextMapper.getTranslateError(role)

    fun getTranslateText(role: Role): TranslateText = CosmeticRoleTextMapper.getTranslateText(role)
}
