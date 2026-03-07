package me.miki.shindo.addon.api.hypixel

/**
 * Representação genérica de um item do EnderChest no contexto do Pit.
 *
 * O objetivo é permitir renderização de HUD/GUI no addon sem expor classes
 * internas de item do Minecraft.
 */
data class PitItem(
    /** Posição lógica do slot dentro do EnderChest. */
    val slot: Int,
    /** Nome exibido (já com formatação básica aplicada). */
    val displayName: String,
    /** Quantidade do item. */
    val amount: Int,
    /** Identificador lógico/material (ex.: "DIAMOND_SWORD"). */
    val materialId: String,
    /** Lore já limpa/pronta para exibição em texto. */
    val lore: List<String> = emptyList()
)

