package me.miki.shindo.utils.vector

/**
 * Represents a transformation state with position (pose) and normal matrices.
 */
class Pose(
    val poseMatrix: Matrix4f,
    val normalMatrix: Matrix3f
) {
    fun pose(): Matrix4f = poseMatrix
    fun normal(): Matrix3f = normalMatrix
}
