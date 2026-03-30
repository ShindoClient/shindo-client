@file:JvmName("Vector2fAnimationExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.value.Vector2fAnimation

/**
 * Extension helpers for [Vector2fAnimation].
 */
/** Exposes X completion in extension-friendly style. */
fun Vector2fAnimation.isDoneX(): Boolean = this.isDoneX()

/** Exposes Y completion in extension-friendly style. */
fun Vector2fAnimation.isDoneY(): Boolean = this.isDoneY()

/** Snaps both components to the provided coordinates. */
fun Vector2fAnimation.snapTo(x: Float, y: Float) {
    this.snapTo(x, y)
}
