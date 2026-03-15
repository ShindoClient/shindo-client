package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventCameraRotation(
    @JvmField var yaw: Float,
    @JvmField var pitch: Float,
    @JvmField var roll: Float,
    @JvmField var thirdPersonDistance: Float
) : Event() {
    fun getYaw(): Float = yaw
    fun setYaw(yaw: Float) {
        this.yaw = yaw
    }

    fun getPitch(): Float = pitch
    fun setPitch(pitch: Float) {
        this.pitch = pitch
    }

    fun getRoll(): Float = roll
    fun setRoll(roll: Float) {
        this.roll = roll
    }

    fun getThirdPersonDistance(): Float = thirdPersonDistance
    fun setThirdPersonDistance(thirdPersonDistance: Float) {
        this.thirdPersonDistance = thirdPersonDistance
    }
}

