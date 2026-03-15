@file:JvmName("Vector2fAnimationExtensions")

package me.miki.extensions.animation

import me.miki.shindo.ui.animation.value.Vector2fAnimation
import java.lang.reflect.Field

private object Vector2fReflectionCache {
    val animX: Field by lazy {
        Vector2fAnimation::class.java.getDeclaredField("animX").apply { isAccessible = true }
    }
    val animY: Field by lazy {
        Vector2fAnimation::class.java.getDeclaredField("animY").apply { isAccessible = true }
    }
}

/**
 * Extension helpers for [Vector2fAnimation].
 */
fun Vector2fAnimation.isDoneX(): Boolean =
    (Vector2fReflectionCache.animX.get(this) as? me.miki.shindo.ui.animation.value.FloatAnimation)?.isDone() ?: isDone()

fun Vector2fAnimation.isDoneY(): Boolean =
    (Vector2fReflectionCache.animY.get(this) as? me.miki.shindo.ui.animation.value.FloatAnimation)?.isDone() ?: isDone()

fun Vector2fAnimation.snapTo(x: Float, y: Float) {
    (Vector2fReflectionCache.animX.get(this) as? me.miki.shindo.ui.animation.value.FloatAnimation)?.setValue(if (x >= getX()) 1.0 else 0.0)
    (Vector2fReflectionCache.animY.get(this) as? me.miki.shindo.ui.animation.value.FloatAnimation)?.setValue(if (y >= getY()) 1.0 else 0.0)
}
