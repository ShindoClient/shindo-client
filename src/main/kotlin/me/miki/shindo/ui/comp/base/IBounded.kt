package me.miki.shindo.ui.comp.base

/**
 * Interface para componentes que possuem posição e dimensões.
 */
interface IBounded {
    /**
     * Posição X do componente.
     */
    fun getX(): Float
    fun setX(x: Float)

    /**
     * Posição Y do componente.
     */
    fun getY(): Float
    fun setY(y: Float)

    /**
     * Largura do componente.
     */
    fun getWidth(): Float
    fun setWidth(width: Float)

    /**
     * Altura do componente.
     */
    fun getHeight(): Float
    fun setHeight(height: Float)

    /**
     * Define posição e dimensões de uma vez.
     */
    fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        setX(x)
        setY(y)
        setWidth(width)
        setHeight(height)
    }
}
