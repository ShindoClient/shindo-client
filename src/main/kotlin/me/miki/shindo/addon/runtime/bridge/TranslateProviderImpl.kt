package me.miki.shindo.addon.runtime.bridge

import me.miki.shindo.Shindo
import me.miki.shindo.addon.api.language.ITranslateProvider

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
