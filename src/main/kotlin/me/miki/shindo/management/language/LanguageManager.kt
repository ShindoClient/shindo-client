package me.miki.shindo.management.language

import me.miki.shindo.logger.ShindoLogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class LanguageManager {
    private val translateMap = HashMap<String, String>()
    private var currentLanguage: Language = Language.ENGLISH

    init {
        setCurrentLanguage(Language.ENGLISH)
    }

    private fun loadMap(
        map: HashMap<String, String>,
        language: String,
    ) {
        val path = "assets/minecraft/shindo/language/$language.properties"
        val stream =
            LanguageManager::class.java.classLoader.getResourceAsStream(path)
                ?: return
        try {
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val s = line!!
                    if (s.isNotEmpty() && !s.startsWith("#")) {
                        val args = s.split("=", limit = 2)
                        if (args.size >= 2) {
                            map[args[0]] = args[1]
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load translate", e)
        }
    }

    fun getCurrentLanguage(): Language = currentLanguage

    /**
     * Retorna o texto traduzido para a chave, ou a chave se não houver tradução.
     */
    fun getText(key: String): String = translateMap[key] ?: key

    /**
     * Traduz com parâmetros (substitui {0}, {1}, etc).
     */
    fun getText(
        key: String,
        vararg args: Any,
    ): String {
        var template = getText(key)
        args.forEachIndexed { i, arg -> template = template.replace("{$i}", arg.toString()) }
        return template
    }

    fun setCurrentLanguage(lang: Language) {
        currentLanguage = lang
        loadMap(translateMap, lang.getId())
        for (text in TranslateText.values()) {
            translateMap[text.getKey()]?.let { text.setText(it) }
        }
    }
}
