package me.miki.shindo.addon.api

/**
 * Metadados de um addon: identificação, versão, nome, descrição, ícone e tipo.
 */
data class AddonMetadata(
    /** ID único do addon (ex: "meu-addon", "rpo") */
    val id: String,
    /** Versão no formato semântico (ex: "1.0.0") */
    val version: String,
    /** Nome de exibição */
    val name: String,
    /** Descrição curta do addon */
    val description: String,
    /** Ícone (código LegacyIcon ou caminho relativo) */
    val icon: String = "",
    /** Tipo/categoria do addon */
    val type: AddonType = AddonType.OTHER,
    /** Autor (opcional) */
    val author: String = "",
    /** Se true, mostra o botão toggle na UI. Use false para addons que não precisam ou quando toggle pode quebrar o sistema. */
    val showToggle: Boolean = true
)
