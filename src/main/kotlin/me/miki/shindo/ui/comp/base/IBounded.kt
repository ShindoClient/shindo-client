package me.miki.shindo.ui.comp.base

interface IBounded {
    fun getX(): Float
    fun setX(x: Float)
    fun getY(): Float
    fun setY(y: Float)
    fun getWidth(): Float
    fun setWidth(width: Float)
    fun getHeight(): Float
    fun setHeight(height: Float)
    fun setBounds(x: Float, y: Float, width: Float, height: Float) {
        setX(x)
        setY(y)
        setWidth(width)
        setHeight(height)
    }
}
