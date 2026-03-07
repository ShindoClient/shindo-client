package me.miki.shindo.addon.api.file

import java.nio.charset.Charset

/**
 * Acesso a arquivos para addons. Paths são relativos ao diretório de config do addon
 * (shindo/addons/configs/{addonId}/).
 */
interface IFileProvider {

    /**
     * Retorna o path absoluto do diretório de config do addon.
     * Ex: C:/.../shindo/addons/configs/meu-addon/
     */
    fun getAddonConfigDir(addonId: String): String

    /**
     * Lê arquivo do diretório do addon.
     * @param addonId ID do addon
     * @param relativePath path relativo (ex: "config.json", "data/settings.txt")
     */
    fun readAddonFile(addonId: String, relativePath: String, charset: Charset = Charsets.UTF_8): String?

    /**
     * Escreve arquivo no diretório do addon. Cria diretórios se necessário.
     */
    fun writeAddonFile(addonId: String, relativePath: String, content: String, charset: Charset = Charsets.UTF_8): Boolean

    /**
     * Verifica se arquivo existe no dir do addon.
     */
    fun addonFileExists(addonId: String, relativePath: String): Boolean

    /**
     * Path absoluto do arquivo no addon. Para uso com outras APIs.
     */
    fun resolvePath(addonId: String, relativePath: String): String
}
