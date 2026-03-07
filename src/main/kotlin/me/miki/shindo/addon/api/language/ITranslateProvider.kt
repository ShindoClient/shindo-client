package me.miki.shindo.addon.api.language

/**
 * Provedor de textos traduzidos. O client implementa usando LanguageManager / I18n.
 * Addons usam chaves (ex: "text.addons") para obter o texto na língua atual.
 */
interface ITranslateProvider {

    /**
     * Retorna o texto traduzido para a chave, ou a própria chave se não houver tradução.
     * @param key chave de tradução (ex: "text.addons")
     */
    fun getText(key: String): String

    /**
     * Traduz com parâmetros de formatação.
     * @param key chave de tradução
     * @param args argumentos para substituição (ex: {0}, {1} no texto)
     */
    fun getText(key: String, vararg args: Any): String
}
