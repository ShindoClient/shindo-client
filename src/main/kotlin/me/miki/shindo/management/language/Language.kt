package me.miki.shindo.management.language

import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import net.minecraft.util.ResourceLocation

enum class Language(
    private val id: String,
    private val nameTranslate: String,
    private val flag: ResourceLocation,
) {
    ENGLISH("en-us", "English (United States)", ResourceLocation("shindo/flag/us.png")),
    ITALIAN("it-it", "Italiano", ResourceLocation("shindo/flag/it.png")),
    SPANISH("es-es", "Español", ResourceLocation("shindo/flag/es.png")),
    GERMAN("de-de", "Deutsch", ResourceLocation("shindo/flag/de.png")),
    FRENCH("fr-fr", "Français", ResourceLocation("shindo/flag/fr.png")),
    DANISH("da-da", "Dansk", ResourceLocation("shindo/flag/da.png")),
    JAPANESE("jp-jp", "日本語", ResourceLocation("shindo/flag/jp.png")),
    PORTUGUESE("pt-br", "Português (Brasileiro)", ResourceLocation("shindo/flag/br.png")),
    PORTUGUESE_PORTUGAL("pt-pt", "Português (Portugal)", ResourceLocation("shindo/flag/pt.png")),
    ;

    private val animation = SimpleAnimation()

    fun getId(): String = id

    fun getName(): String = nameTranslate

    fun getFlag(): ResourceLocation = flag

    fun getAnimation(): SimpleAnimation = animation

    fun getNameTranslate(): String = nameTranslate

    companion object {
        fun getLanguageById(id: String): Language = entries.find { it.id == id } ?: ENGLISH
    }
}
