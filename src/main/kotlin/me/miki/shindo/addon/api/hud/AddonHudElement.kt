package me.miki.shindo.addon.api.hud

import me.miki.shindo.addon.api.render.IRenderContext

/**
 * Elemento de HUD registrado por um addon e controlado pelo HUD Editor do client.
 *
 * A posição/tamanho/escala são manipulados pelo client; a implementação do addon
 * deve persistir esses valores (ex.: via IAddonConfigStorage).
 */
interface AddonHudElement {

    /**
     * ID estável e único dentro do addon (ex.: "pit_streak_hud").
     */
    val id: String

    var x: Float
    var y: Float
    var width: Float
    var height: Float
    var scale: Float

    /**
     * ID do addon dono deste HUD.
     *
     * Usado para limpeza automática quando addon é desabilitado/recarregado.
     */
    fun ownerAddonId(): String = ""

    fun isDraggable(): Boolean = true

    fun minScale(): Float = 0.2f

    fun maxScale(): Float = 5.0f

    fun getScaledWidth(): Float = width * scale

    fun getScaledHeight(): Float = height * scale

    /**
     * Se o HUD deve ser considerado para renderização/edição.
     */
    fun isVisible(): Boolean = true

    /**
     * Callback para persistir x/y/scale após mudanças no HUD Editor.
     */
    fun onLayoutChanged() {}

    /**
     * Renderização do HUD na tela. As coordenadas fornecidas (x/y/width/height/scale)
     * já levam em conta o posicionamento feito pelo HUD Editor.
     */
    fun render(render: IRenderContext)
}
