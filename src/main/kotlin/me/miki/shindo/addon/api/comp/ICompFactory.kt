package me.miki.shindo.addon.api.comp

import me.miki.shindo.addon.api.render.AddonColor

/**
 * Factory para criar componentes. O client fornece implementação.
 */
interface ICompFactory {

    fun createPanel(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f): IComp

    fun createLabel(text: String, x: Float = 0f, y: Float = 0f): IComp

    fun createButton(text: String, x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f, onClick: () -> Unit = {}): IComp

    fun createTextBox(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f, defaultText: String? = null): IComp

    fun createSeparator(x: Float = 0f, y: Float = 0f, length: Float = 100f, horizontal: Boolean = true): IComp

    fun createCard(x: Float = 0f, y: Float = 0f, width: Float = 200f, height: Float = 150f): IComp

    /**
     * Cria botão toggle sem depender de Setting.
     * @param initial valor inicial
     * @param onChange chamado quando o valor muda
     */
    fun createToggleButton(x: Float = 0f, y: Float = 0f, scale: Float = 1f, initial: Boolean = false, onChange: (Boolean) -> Unit = {}): IComp

    /**
     * Cria área scrollável onde os filhos adicionados via addChild são desenhados com scroll.
     */
    fun createScrollable(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f): IComp

    /**
     * Cria barra de progresso.
     * @param maxProgress valor máximo (progress em 0..maxProgress)
     */
    fun createProgressBar(x: Float = 0f, y: Float = 0f, width: Float = 100f, height: Float = 8f, maxProgress: Float = 100f): IComp

    /**
     * Cria tooltip overlay (show/hide com animação).
     */
    fun createTooltip(text: String = "", x: Float = 0f, y: Float = 0f): ICompTooltip

    /**
     * Cria badge (texto com fundo arredondado).
     */
    fun createBadge(text: String = "", x: Float = 0f, y: Float = 0f): ICompBadge

    /**
     * Cria slider para addons (min, max, value, step, onChange).
     */
    fun createSlider(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 90f,
        min: Double = 0.0,
        max: Double = 100.0,
        initialValue: Double = 50.0,
        step: Double = 0.0,
        integer: Boolean = false,
        onChange: (Double) -> Unit = {}
    ): ICompSlider

    /**
     * Cria keybind para addons.
     */
    fun createKeybind(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 80f,
        initialKeyCode: Int = 0,
        onChange: (Int) -> Unit = {}
    ): ICompKeybind

    /**
     * Cria color picker para addons.
     */
    fun createColorPicker(
        x: Float = 0f,
        y: Float = 0f,
        initialColor: AddonColor = AddonColor.WHITE,
        showAlpha: Boolean = true,
        onChange: (AddonColor) -> Unit = {}
    ): ICompColorPicker

    /**
     * Cria dropdown para addons.
     */
    fun createDropdown(
        x: Float = 0f,
        y: Float = 0f,
        width: Float = 120f,
        options: List<String>,
        initialSelectedIndex: Int = 0,
        onChange: (Int, String) -> Unit = { _, _ -> }
    ): ICompDropdown
}
