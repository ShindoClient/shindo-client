@file:JvmName("Vector2fAnimationExtensions")

package me.miki.extensions.animation

import me.miki.shindo.ui.animation.value.Vector2fAnimation

/**
 * Extension helpers for [Vector2fAnimation].
 */
fun Vector2fAnimation.isDoneX(): Boolean = this.isDoneX()

fun Vector2fAnimation.isDoneY(): Boolean = this.isDoneY()

fun Vector2fAnimation.snapTo(x: Float, y: Float) {
    this.snapTo(x, y)
}
