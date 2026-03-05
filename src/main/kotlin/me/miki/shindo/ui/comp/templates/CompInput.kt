package me.miki.shindo.ui.comp.templates

abstract class CompInput<T>(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 0f
) : CompInteractive(x, y) {

    private var focused: Boolean = false
    private var value: T? = null
    private var validator: ((T?) -> Boolean)? = null
    private var onValueChanged: ((T?) -> Unit)? = null

    init {
        setWidth(width)
        setHeight(height)
    }

    fun isFocused(): Boolean = focused
    fun setFocused(focused: Boolean) {
        this.focused = focused
    }

    fun getValue(): T? = value
    fun setValue(value: T?) {
        if (this.value != value) {
            this.value = value
            onValueChanged?.invoke(value)
        }
    }

    fun setValidator(validator: ((T?) -> Boolean)?): CompInput<T> {
        this.validator = validator
        return this
    }

    fun setOnValueChanged(callback: ((T?) -> Unit)?): CompInput<T> {
        this.onValueChanged = callback
        return this
    }

    fun isValid(): Boolean = validator?.invoke(value) ?: true

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            setFocused(isHovered(mouseX, mouseY))
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun drawInteractive(mouseX: Int, mouseY: Int, partialTicks: Float, hovered: Boolean) {
        drawInput(mouseX, mouseY, partialTicks, focused, hovered)
    }

    protected abstract fun drawInput(mouseX: Int, mouseY: Int, partialTicks: Float, focused: Boolean, hovered: Boolean)
}
