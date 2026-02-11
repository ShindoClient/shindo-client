package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.BrokenBlock
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper

open class FastbreakCheck(private val brokenBlocksList: MutableList<BrokenBlock>) : Check() {

    private var sendReport = false

    override fun getCheatName(): String = "Fastbreak"

    override fun getCheatDescription(): String = "O jogador pode quebrar blocos mais rápido que o normal"

    override fun canSendReport(): Boolean = sendReport

    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        check(player, data)
    }

    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        checkPlayerBreakingBlocks(player, data)
        return false
    }

    fun checkPlayerSP(player: EntityPlayer) {
        checkPlayerBreakingBlocks(player, null)
    }

    private fun checkPlayerBreakingBlocks(player: EntityPlayer, data: PlayerDataSamples?) {
        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled() || !addon.enableFastbreakCheck || !player.isSwingInProgress || brokenBlocksList.isEmpty()) return

        val stack: ItemStack = player.heldItem ?: return

        for (brokenBlock in brokenBlocksList) {
            if (isAppropriateTool(stack, brokenBlock) && isPlayerLookingAtBlock(
                    player,
                    data ?: return,
                    brokenBlock.blockPos
                )
            ) {
                brokenBlock.addPlayer(player)
                return
            }
        }
    }

    private fun isAppropriateTool(stack: ItemStack, brokenBlock: BrokenBlock): Boolean {
        return when (brokenBlock.tool) {
            "pickaxe" -> {
                stack.isItemEnchanted && stack.item == Items.diamond_pickaxe
            }
            "axe" -> {
                stack.item is ItemAxe
            }
            else -> false
        }
    }

    fun onTickEnd() {
        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled() || mc.theWorld == null || mc.thePlayer == null || !mc.theWorld.isRemote) {
            brokenBlocksList.clear()
            return
        }

        for (brokenBlock in brokenBlocksList) {
            if (brokenBlock.playerList == null) continue

            var oldestTime = System.currentTimeMillis()
            var playerBreaking: EntityPlayer? = null

            val playerList = brokenBlock.playerList ?: continue
            for (player in playerList) {
                if (player is IMixinEntityPlayer) {
                    val data = player.getPlayerDataSamples()
                    if (data.lastBreakBlockTime - oldestTime < 0) {
                        oldestTime = data.lastBreakBlockTime
                        playerBreaking = player
                    }
                }
            }

            if (playerBreaking == null || playerBreaking !is IMixinEntityPlayer) continue

            val data = playerBreaking.getPlayerDataSamples()
            if (data.serverUpdatesList.sum() * 20 / data.serverUpdatesList.capacity() > 14) continue

            val recordedBreakTime = brokenBlock.breakTime - data.lastBreakBlockTime
            data.lastBreakBlockTime = brokenBlock.breakTime

            if (playerBreaking == mc.thePlayer || "pickaxe" != brokenBlock.tool) continue

            val expectedBreakTime =
                50F * getTimeToHarvestBlock(getBlockStrength(playerBreaking, brokenBlock.blockPos, brokenBlock.block))
            val breakTimeRatio = recordedBreakTime / expectedBreakTime
            data.breakTimeRatio.add(breakTimeRatio.coerceAtMost(1.1F))

            if (breakTimeRatio < 0.95F) {
                data.fastbreakVL.add(MathHelper.clamp_int(MathHelper.floor_float((1F - breakTimeRatio) * 20F), 1, 4))
                if (addon.debugLoggingSetting && data.fastbreakVL.getViolationLevel() > 6) {
                    logFastbreak(
                        playerBreaking, data, data.fastbreakVL,
                        " | avgBreaktimeRatio ${String.format("%.2f", data.breakTimeRatio.average())}" +
                                " | breakTimeRatio ${String.format("%.2f", breakTimeRatio)}" +
                                " | breakTime $recordedBreakTime/${expectedBreakTime.toInt()}" +
                                " | block ${brokenBlock.block.unlocalizedName}"
                    )
                }
                sendReport = data.breakTimeRatio.average() < 0.8F
                checkViolationLevel(playerBreaking, true, data.fastbreakVL)
            } else {
                data.fastbreakVL.subtract(2)
                checkViolationLevel(playerBreaking, false, data.fastbreakVL)
            }
        }

        brokenBlocksList.removeIf { System.currentTimeMillis() - it.breakTime > 1000 }
    }

    private fun logFastbreak(
        player: EntityPlayer,
        data: PlayerDataSamples,
        vl: ViolationLevelTracker,
        extramsg: String?
    ) {
        var msg = extramsg ?: ""
        if (player.isPotionActive(Potion.digSpeed)) {
            val effect = player.getActivePotionEffect(Potion.digSpeed)
            msg += " | haste level ${(effect?.amplifier ?: -1) + 1}"
        }
        if (player.isPotionActive(Potion.digSlowdown)) {
            val effect = player.getActivePotionEffect(Potion.digSlowdown)
            msg += " | mining fatigue level ${(effect?.amplifier ?: -1) + 1}"
        }
        log(player, data, vl, msg)
    }

    private fun getBlockStrength(player: EntityPlayer, pos: BlockPos, block: Block): Float {
        val hardness = block.getBlockHardness(null, pos)
        if (hardness < 0.0F) return 0.0F

        return if (canHarvestBlock(block)) {
            getBreakSpeed(player, block) / hardness / 30F
        } else {
            getBreakSpeed(player, block) / hardness / 100F
        }
    }

    private fun canHarvestBlock(block: Block): Boolean {
        if (block.material.isToolNotRequired) return true
        val hardness = block.getBlockHardness(null, BlockPos.ORIGIN)
        return hardness >= 0.0F
    }

    private fun getBreakSpeed(player: EntityPlayer, block: Block): Float {
        var f = Items.diamond_pickaxe.getStrVsBlock(null, block)
        if (f > 1.0F) {
            val efficiencyModifier = 3
            f += (efficiencyModifier * efficiencyModifier + 1).toFloat()
        }
        f *= if (player.isPotionActive(Potion.digSpeed)) {
            val effect = player.getActivePotionEffect(Potion.digSpeed)
            1.0F + ((effect?.amplifier ?: -1) + 1) * 0.2F
        } else {
            1.0F + 2 * 0.2F
        }
        if (player.isPotionActive(Potion.digSlowdown)) {
            val effect = player.getActivePotionEffect(Potion.digSlowdown)
            val f1 = when (effect?.amplifier ?: -1) {
                0 -> 0.3F
                1 -> 0.09F
                2 -> 0.0027F
                else -> 8.1E-4F
            }
            f *= f1
        }
        return if (f < 0) 0F else f
    }

    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(40)
    }
}
