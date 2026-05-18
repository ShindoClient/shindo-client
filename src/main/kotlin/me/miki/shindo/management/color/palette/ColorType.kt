package me.miki.shindo.management.color.palette

enum class ColorType(
    private val index: Int,
) {
    DARK(0),
    MID(1),
    NORMAL(2),
    ;

    fun getIndex(): Int = index
}
