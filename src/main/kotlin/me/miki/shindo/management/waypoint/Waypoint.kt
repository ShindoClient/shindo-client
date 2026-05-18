package me.miki.shindo.management.waypoint

import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import java.awt.Color

class Waypoint(
    private val world: String,
    private val name: String,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val color: Color,
) {
    private val trashAnimation = SimpleAnimation()

    fun getWorld(): String = world

    fun getName(): String = name

    fun getX(): Double = x

    fun getY(): Double = y

    fun getZ(): Double = z

    fun getColor(): Color = color

    fun getTrashAnimation(): SimpleAnimation = trashAnimation
}
