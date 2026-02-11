package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import net.minecraft.entity.player.EntityPlayer

interface ICheck {

    fun getCheatName(): String

    fun getCheatDescription(): String

    fun getFlagType(): String = ""

    fun canSendReport(): Boolean

    fun performCheck(player: EntityPlayer, data: PlayerDataSamples)

    fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean
}
