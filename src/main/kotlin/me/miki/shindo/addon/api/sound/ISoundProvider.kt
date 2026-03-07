package me.miki.shindo.addon.api.sound

/**
 * Reprodutor de sons. Permite addons tocarem sons built-in ou de arquivos.
 */
interface ISoundProvider {

    /**
     * Toca um som built-in do client por path de recurso.
     * Ex: "shindo/sounds/ui_positive.wav"
     *
     * @param resourcePath path relativo a assets/minecraft/
     * @param volume 0.0 a 1.0
     * @param pitch 0.5 a 2.0 (1.0 = normal)
     */
    fun playSound(resourcePath: String, volume: Float = 1f, pitch: Float = 1f)

    /**
     * Toca um som a partir de um arquivo no disco.
     * O path deve estar dentro do diretório do addon (shindo/addons/configs/{addonId}/)
     * ou em cache custom-sound.
     *
     * Formatos suportados: WAV, MP3, OGG (depende do client).
     *
     * @param filePath path absoluto ou relativo ao addon config
     * @param volume 0.0 a 1.0
     * @param pitch 0.5 a 2.0
     */
    fun playSoundFromFile(filePath: String, volume: Float = 1f, pitch: Float = 1f)
}
