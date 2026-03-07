package me.miki.shindo.addon.api.render

/**
 * Interface de render 2D. O client fornece implementação que delega ao NanoVGManager.
 * Addons usam esta interface sem depender do JAR do client.
 */
interface IRenderContext {

    fun drawText(text: String, x: Float, y: Float, color: AddonColor, size: Float, font: AddonFont)

    fun drawCenteredText(text: String, x: Float, y: Float, color: AddonColor, size: Float, font: AddonFont)

    fun drawTextBox(text: String, x: Float, y: Float, maxWidth: Float, color: AddonColor, size: Float, font: AddonFont)

    fun drawTextGlowing(text: String, x: Float, y: Float, color: AddonColor, blurRadius: Float, size: Float, font: AddonFont)

    fun drawCenteredIcon(icon: String, x: Float, y: Float, size: Float, color: AddonColor)

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: AddonColor)

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: AddonColor)

    fun drawVerticalGradientRect(x: Float, y: Float, width: Float, height: Float, color1: AddonColor, color2: AddonColor)

    fun drawHorizontalGradientRect(x: Float, y: Float, width: Float, height: Float, color1: AddonColor, color2: AddonColor)

    fun drawGradientRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color1: AddonColor, color2: AddonColor)

    fun drawOutlineRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, strokeWidth: Float, color: AddonColor)

    fun drawShadow(x: Float, y: Float, width: Float, height: Float, radius: Float, strength: Int = 7)

    fun drawCircle(x: Float, y: Float, radius: Float, color: AddonColor)

    fun drawGradientCircle(x: Float, y: Float, radius: Float, color1: AddonColor, color2: AddonColor)

    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, strokeWidth: Float, color: AddonColor)

    fun drawArc(cx: Float, cy: Float, r: Float, a0: Float, a1: Float, strokeWidth: Float, color: AddonColor)

    fun getTextWidth(text: String, fontSize: Float, font: AddonFont): Float

    fun getTextHeight(text: String, fontSize: Float, font: AddonFont): Float

    fun getLimitText(text: String, fontSize: Float, font: AddonFont, maxWidth: Float): String

    fun save()

    fun restore()

    fun scissor(x: Float, y: Float, width: Float, height: Float)

    /**
     * Aplica transformação de translação ao contexto atual.
     * Use com save/restore para escopo limitado.
     */
    fun translate(x: Float, y: Float)

    /**
     * Desenha imagem. Path: "namespace:path" (recurso) ou path absoluto (arquivo).
     * Ex: "shindo:icons/addon.png", "minecraft:textures/gui/..."
     */
    fun drawImage(path: String, x: Float, y: Float, width: Float, height: Float)

    /**
     * Desenha imagem com alpha (0-255).
     */
    fun drawImage(path: String, x: Float, y: Float, width: Float, height: Float, alpha: Int)

    /**
     * Desenha imagem com cantos arredondados.
     */
    fun drawRoundedImage(path: String, x: Float, y: Float, width: Float, height: Float, radius: Float)

    /**
     * Desenha imagem com cantos arredondados e alpha (0-255).
     */
    fun drawRoundedImage(path: String, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Int)

    /**
     * Desenha cabeça do jogador pelo nome.
     * O recurso é carregado assincronamente; se ainda não estiver em cache, nada é desenhado.
     * Chame repetidamente (ex: no render) até o head aparecer.
     * @param playerName nome do jogador Minecraft
     * @param alpha 0.0 a 1.0
     */
    fun drawPlayerHead(playerName: String, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float = 1f)
}
