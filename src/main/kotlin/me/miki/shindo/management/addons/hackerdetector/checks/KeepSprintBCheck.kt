package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.data.SampleListD
import me.miki.shindo.management.addons.hackerdetector.utils.Vector2D
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import kotlin.math.sqrt

class KeepSprintBCheck : Check() {

    override fun getCheatName(): String = "KeepSprint"

    override fun getCheatDescription(): String = "A velocidade do jogador não diminui após atacar outros jogadores"

    override fun getFlagType(): String = "B"

    override fun canSendReport(): Boolean = true

    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.keepSprintBVL)
    }

    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (player.isRiding) return false
        if (data.isNotMovingXZ()) return false
        if (!data.serverPosXList.hasCollected()) return false

        if (checkAttack(data) && (data.isOnFlatGround() || accel(data.serverPosYList) < -25.0)) {
            val speedXZ = speedXZ(data, 0)
            val prevSpeedXZ = speedXZ(data, 1)
            if (prevSpeedXZ > 4.0) {
                if (speedXZ > 0.9 * prevSpeedXZ) {
                    data.keepSprintBVL.add(10)
                    if (HackerDetectorAddon.instance.debugLoggingSetting) {
                        log(
                            player, data, data.keepSprintBVL,
                            " | attacked ${data.hasAttacked()}" +
                                    " | prevSpeedXZ ${String.format("%.4f", prevSpeedXZ)}" +
                                    " | speedXZ ${String.format("%.4f", speedXZ)}" +
                                    " | onGround ${player.onGround}" +
                                    " | moveDiff ${String.format("%.2f", getMoveAngleDiff(data))}" +
                                    " | moveLookDiff ${String.format("%.2f", data.getMoveLookAngleDiff())}" +
                                    " | posY ${data.serverPosYList}"
                        )
                        fail(player)
                    }
                } else {
                    data.keepSprintBVL.subtract(if (data.hasAttacked()) 2 else 5)
                }
            }
        }
        return false
    }

    private fun getMoveAngleDiff(data: PlayerDataSamples): Double {
        return MathHelper.wrapAngleTo180_double(
            Vector2D(
                data.serverPosZList.get(0) - data.serverPosZList.get(1),
                -(data.serverPosXList.get(0) - data.serverPosXList.get(1))
            ).getOrientedAngle() -
                    Vector2D(
                        data.serverPosZList.get(1) - data.serverPosZList.get(2),
                        -(data.serverPosXList.get(1) - data.serverPosXList.get(2))
                    ).getOrientedAngle()
        )
    }

    private fun checkAttack(data: PlayerDataSamples): Boolean {
        return data.serverUpdatesList.get(0) == 1 &&
                (data.attackList.get(0) || (!data.attackList.get(0) && data.attackList.get(1)))
    }

    private fun speedXZ(data: PlayerDataSamples, i: Int): Double {
        val vx = 10.0 * (data.serverPosXList.get(i) - data.serverPosXList.get(1 + i))
        val vz = 10.0 * (data.serverPosZList.get(i) - data.serverPosZList.get(1 + i))
        return sqrt(vx * vx + vz * vz)
    }

    private fun accel(list: SampleListD): Double {
        return 10.0 * 10.0 * (list.get(2) - 2.0 * list.get(1) + list.get(0))
    }

    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(30)
    }
}
