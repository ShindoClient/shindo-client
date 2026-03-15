package me.miki.shindo.utils.vector

class PoseStack {
    private val poseStack: MutableList<Pose> = mutableListOf(identityPose())

    fun pushPose() {
        val last = last()
        poseStack.add(
            Pose(
                Matrix4f(last.pose().copy()),
                Matrix3f(last.normal().copy())
            )
        )
    }

    fun popPose() {
        if (poseStack.size > 1) {
            poseStack.removeAt(poseStack.lastIndex)
        }
    }

    fun last(): Pose = poseStack.last()

    fun translate(x: Double, y: Double, z: Double) {
        last().pose().translate(x, y, z)
    }

    fun mulPose(quaternion: Quaternion) {
        last().pose().mulPose(quaternion)
        last().normal().mul(quaternion)
    }

    private fun identityPose(): Pose {
        val pose = Matrix4f()
        pose.setIdentity()
        val normal = Matrix3f()
        normal.setIdentity()
        return Pose(pose, normal)
    }
}
