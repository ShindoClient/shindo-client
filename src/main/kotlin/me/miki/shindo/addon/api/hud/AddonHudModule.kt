package me.miki.shindo.addon.api.hud

/**
 * Base opcional para HUDs de addon.
 *
 * Facilita manter posição/dimensão/escala com utilitários comuns, no mesmo
 * padrão esperado pelo HUD Editor do client.
 */
abstract class AddonHudModule(
    final override val id: String,
    initialX: Float = 0f,
    initialY: Float = 0f,
    initialWidth: Float = 80f,
    initialHeight: Float = 20f,
    initialScale: Float = 1f
) : AddonHudElement {

    final override var x: Float = initialX
    final override var y: Float = initialY
    final override var width: Float = initialWidth
    final override var height: Float = initialHeight
    final override var scale: Float = initialScale.coerceAtLeast(0.01f)

    fun moveTo(newX: Float, newY: Float) {
        x = newX
        y = newY
        onLayoutChanged()
    }

    fun resize(newWidth: Float, newHeight: Float) {
        width = newWidth.coerceAtLeast(1f)
        height = newHeight.coerceAtLeast(1f)
        onLayoutChanged()
    }

    fun setScaleClamped(newScale: Float) {
        scale = newScale.coerceIn(minScale(), maxScale())
        onLayoutChanged()
    }
}
