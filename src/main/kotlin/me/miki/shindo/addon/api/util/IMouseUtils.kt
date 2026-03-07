package me.miki.shindo.addon.api.util

/**
 * Utilitários de mouse. O client fornece implementação.
 */
interface IMouseUtils {

    fun isInside(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float): Boolean
}
