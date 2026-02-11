package me.miki.shindo.management.addons.hackerdetector.data

import me.miki.shindo.management.addons.hackerdetector.AttackDetector
import net.minecraft.entity.player.EntityPlayer

class AttackInfo(
    target: EntityPlayer?,
    val attackType: AttackDetector.AttackType
) {
    var target: EntityPlayer? = target
        set(newTarget) {
            field = newTarget
            targetName = newTarget?.name
        }

    var targetName: String? = target?.name

    var multiTarget: Boolean = false
}
