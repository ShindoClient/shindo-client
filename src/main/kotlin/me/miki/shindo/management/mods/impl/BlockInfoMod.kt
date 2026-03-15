package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.easing.EaseBackIn
import me.miki.shindo.ui.animation.screen.ScreenAnimation
import me.miki.shindo.utils.GlUtils.startScale
import me.miki.shindo.utils.GlUtils.stopScale
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition.MovingObjectType

class BlockInfoMod :
    HUDMod(TranslateText.BLOCK_INFO, TranslateText.BLOCK_INFO_DESCRIPTION, LegacyIcon.MOD_BLOCK_INFO, "waila") {
    private val screenAnimation = ScreenAnimation()
    private var lastSelection: Long = 0
    private var introAnimation: Animation? = null
    private var pos: BlockPos? = null
    private var state: IBlockState? = null
    private var block: Block? = null

    override fun setup() {
        introAnimation = EaseBackIn(320, 1.0, 2.0f)
        introAnimation!!.setDirection(Direction.BACKWARDS)
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        screenAnimation.wrap(
            Runnable { drawBlock() },
            Runnable { drawNanoVG() },
            this.getX().toFloat(),
            this.getY().toFloat(),
            this.getWidth().toFloat(),
            this.getHeight().toFloat(),
            2 - introAnimation!!.getValueFloat(),
            introAnimation!!.getValueFloat()
        )

        this.setWidth(80)
        this.setHeight(80)
    }

    private fun drawBlock() {
        if (block != null && (block != Blocks.portal) && (block != Blocks.end_portal)) {
            startScale(
                this.getX().toFloat(),
                this.getY().toFloat(),
                this.getWidth().toFloat(),
                this.getHeight().toFloat(),
                2f * this.getScale()
            )
            RenderHelper.enableGUIStandardItemLighting()
            GlStateManager.enableColorMaterial()
            GlStateManager.colorMask(true, true, true, false)
            mc.renderItem.renderItemAndEffectIntoGUI(
                ItemStack(block),
                this.getX() + (this.getWidth() / 2) - 8,
                this.getY() + (this.getHeight() / 2) - 8
            )
            RenderHelper.disableStandardItemLighting()
            GlStateManager.colorMask(true, true, true, true)
            stopScale()
        }
    }

    private fun drawNanoVG() {
        if ((mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) || this.isEditing()) {
            if (this.isEditing()) {
                block = Blocks.grass
            } else {
                pos = mc.objectMouseOver.blockPos
                state = mc.theWorld.getBlockState(pos)
                block = state!!.block
            }

            introAnimation!!.setDirection(Direction.FORWARDS)
            lastSelection = System.currentTimeMillis()
        } else {
            if (System.currentTimeMillis() - lastSelection > 1000) {
                introAnimation!!.setDirection(Direction.BACKWARDS)
            }
        }

        if (block != null && (block != Blocks.portal) && (block != Blocks.end_portal)) {
            this.drawBackground(80f, 80f)

            this.drawCenteredText(block!!.localizedName, 40f, 6f, 9f, getHudFont(1))
        }
    }
}




