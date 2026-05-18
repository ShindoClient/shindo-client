package me.miki.shindo.management.addons

/**
 * Entrada de addon que falhou ao carregar. Exibida na UI com indicador vermelho.
 */
data class FailedAddonEntry(
    val jarFileName: String,
    val errorMessage: String,
)
