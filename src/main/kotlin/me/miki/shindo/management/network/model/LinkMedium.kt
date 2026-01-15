package me.miki.shindo.management.network.model

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.config.PropertyEnum
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Tipo de meio de conexão de rede.
 */
enum class LinkMedium(
    private val translate: TranslateText
) : PropertyEnum {
    WIRED(TranslateText.NETWORK_MEDIUM_WIRED),
    WIRELESS(TranslateText.NETWORK_MEDIUM_WIRELESS),
    MOBILE(TranslateText.NETWORK_MEDIUM_MOBILE);

    @NotNull
    override fun getTranslate(): TranslateText = translate

    @Nullable
    override fun getNameKey(): String? = PropertyEnum.super.getNameKey()

    override fun getDisplayName(): String = translate.text

    companion object {
        fun fromKey(key: String?): LinkMedium {
            if (key == null) return WIRED
            return values().firstOrNull { 
                it.name.equals(key, ignoreCase = true) || 
                it.translate.key.equals(key, ignoreCase = true)
            } ?: WIRED
        }
    }
}
