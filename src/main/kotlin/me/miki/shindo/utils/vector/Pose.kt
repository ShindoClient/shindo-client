package me.miki.shindo.utils.vector

class Pose(
    val poseMatrix: Matrix4f,
    val normalMatrix: Matrix3f
) {
    fun pose(): Matrix4f = poseMatrix
    fun normal(): Matrix3f = normalMatrix
}
