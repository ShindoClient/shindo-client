package com.shindoclient.shindo.management.mods.impl.appleskin

class FoodValues(
    val hunger: Int,
    val saturationModifier: Float,
) {
    val saturationIncrement: Float
        get() = hunger * saturationModifier * 2f

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is FoodValues) {
            return false
        }

        return hunger == other.hunger && other.saturationModifier.compareTo(saturationModifier) == 0
    }

    override fun hashCode(): Int {
        var result = hunger
        result =
            31 * result + (if (saturationModifier != 0.0f) java.lang.Float.floatToIntBits(saturationModifier) else 0)
        return result
    }
}
