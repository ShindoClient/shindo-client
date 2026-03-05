package me.miki.shindo.api.compat

import me.miki.shindo.Shindo
import me.miki.client_api.language.ITranslateProvider

/**
 * Implementação de ITranslateProvider que delega ao LanguageManager do client.
 */
class TranslateProviderImpl : ITranslateProvider {

    override fun getText(key: String): String {
        return Shindo.getInstance().languageManager.getText(key)
    }

    override fun getText(key: String, vararg args: Any): String {
        return Shindo.getInstance().languageManager.getText(key, *args)
    }
}
