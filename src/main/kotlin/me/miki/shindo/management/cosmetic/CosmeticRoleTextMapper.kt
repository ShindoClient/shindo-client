package me.miki.shindo.management.cosmetic

import me.miki.shindo.api.roles.Role
import me.miki.shindo.management.language.TranslateText

object CosmeticRoleTextMapper {
    @JvmStatic
    fun getTranslateError(role: Role): TranslateText =
        when (role) {
            Role.STAFF -> TranslateText.STAFF_ONLY
            Role.NETHERITE -> TranslateText.NETHERITE_ONLY
            Role.EMERALD -> TranslateText.EMERALD_ONLY
            Role.DIAMOND -> TranslateText.DIAMOND_ONLY
            Role.GOLD -> TranslateText.GOLD_ONLY
            Role.MEMBER -> TranslateText.MEMBER
            else -> TranslateText.NONE
        }

    @JvmStatic
    fun getTranslateText(role: Role): TranslateText =
        when (role) {
            Role.STAFF -> TranslateText.STAFF
            Role.NETHERITE -> TranslateText.NETHERITE
            Role.EMERALD -> TranslateText.EMERALD
            Role.DIAMOND -> TranslateText.DIAMOND
            Role.GOLD -> TranslateText.GOLD
            Role.MEMBER -> TranslateText.MEMBER
            else -> TranslateText.NONE
        }
}
