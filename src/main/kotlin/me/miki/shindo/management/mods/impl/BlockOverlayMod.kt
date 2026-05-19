package me.miki.shindo.management.mods.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.Shindo
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventBlockHighlightRender
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils.setColor
import me.miki.shindo.utils.Render3DUtils.drawFillBox
import me.miki.shindo.utils.TimerUtils
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.world.WorldSettings
import org.lwjgl.opengl.GL11
import java.awt.Color

open class BlockOverlayMod :
    Mod(
        TranslateText.BLOCK_OVERLAY,
        TranslateText.BLOCK_OVERLAY_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_BLOCK_OVERLAY,
        "blockoutline",
    ) {
    private val simpleAnimation =
        arrayOf<SimpleAnimation?>(
            SimpleAnimation(0.0f),
            SimpleAnimation(0.0f),
            SimpleAnimation(0.0f),
            SimpleAnimation(0.0f),
            SimpleAnimation(0.0f),
            SimpleAnimation(0.0f),
        )

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANIMATION)
    private val animationSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.FILL)
    private val fillSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.OUTLINE)
    private val outlineSetting = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.FILL_ALPHA, min = 0.0, max = 1.0, current = 0.15)
    private val fillAlphaSetting = 0.15

    @Property(type = PropertyType.NUMBER, translate = TranslateText.OUTLINE_ALPHA, min = 0.0, max = 1.0, current = 0.15)
    private val outlineAlphaSetting = 0.15

    @Property(type = PropertyType.NUMBER, translate = TranslateText.OUTLINE_WIDTH, min = 1.0, max = 10.0, current = 4.0)
    private val outlineWidthSetting = 4.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.DEPTH)
    private val depthSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    private val customColorSetting = false

    @Property(type = PropertyType.COLOR, translate = TranslateText.FILL_COLOR)
    private val fillColorSetting: Color = Color.RED

    @Property(type = PropertyType.COLOR, translate = TranslateText.OUTLINE_COLOR)
    private val outlineColorSetting: Color = Color.RED

    private var currentBB: AxisAlignedBB? = null
    private var slideBB: AxisAlignedBB? = null
    protected var timer: TimerUtils = TimerUtils()

    @EventTarget
    fun onBlockHighlightRender(event: EventBlockHighlightRender) {
        val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()

        event.setCancelled(true)

        if (!canRender(event.objectMouseOver)) {
            return
        }

        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)

        if (depthSetting) {
            GlStateManager.disableDepth()
        }

        GlStateManager.disableTexture2D()

        GlStateManager.depthMask(false)
        val blockpos = event.objectMouseOver.blockPos
        val block = mc.theWorld.getBlockState(blockpos).block

        if (block.material !== Material.air && mc.theWorld.worldBorder.contains(blockpos)) {
            block.setBlockBoundsBasedOnState(mc.theWorld, blockpos)

            val x = (
                mc.renderViewEntity.lastTickPosX +
                    (mc.renderViewEntity.posX - mc.renderViewEntity.lastTickPosX) *
                    event
                        .getPartialTicks()
                        .toDouble()
            )
            val y = (
                mc.renderViewEntity.lastTickPosY +
                    (mc.renderViewEntity.posY - mc.renderViewEntity.lastTickPosY) *
                    event
                        .getPartialTicks()
                        .toDouble()
            )
            val z = (
                mc.renderViewEntity.lastTickPosZ +
                    (mc.renderViewEntity.posZ - mc.renderViewEntity.lastTickPosZ) *
                    event
                        .getPartialTicks()
                        .toDouble()
            )

            var selectedBox = block.getSelectedBoundingBox(mc.theWorld, blockpos)

            if (animationSetting) {
                if (selectedBox != currentBB) {
                    slideBB = currentBB
                    currentBB = selectedBox
                }

                val slide: AxisAlignedBB?

                if ((slideBB.also { slide = it }) != null) {
                    simpleAnimation[0]!!.setAnimation((slide!!.minX + (selectedBox.minX - slide.minX)).toFloat(), 24)
                    simpleAnimation[1]!!.setAnimation((slide.minY + (selectedBox.minY - slide.minY)).toFloat(), 24)
                    simpleAnimation[2]!!.setAnimation((slide.minZ + (selectedBox.minZ - slide.minZ)).toFloat(), 24)
                    simpleAnimation[3]!!.setAnimation((slide.maxX + (selectedBox.maxX - slide.maxX)).toFloat(), 24)
                    simpleAnimation[4]!!.setAnimation((slide.maxY + (selectedBox.maxY - slide.maxY)).toFloat(), 24)
                    simpleAnimation[5]!!.setAnimation((slide.maxZ + (selectedBox.maxZ - slide.maxZ)).toFloat(), 24)

                    val renderBB =
                        AxisAlignedBB(
                            simpleAnimation[0]!!.getValue() - 0.01,
                            simpleAnimation[1]!!.getValue() - 0.01,
                            simpleAnimation[2]!!.getValue() - 0.01,
                            simpleAnimation[3]!!.getValue() + 0.01,
                            simpleAnimation[4]!!.getValue() + 0.01,
                            simpleAnimation[5]!!.getValue() + 0.01,
                        )

                    if (fillSetting) {
                        setColor(
                            if (customColorSetting) {
                                fillColorSetting.rgb
                            } else {
                                currentColor
                                    .getInterpolateColor()
                                    .rgb
                            },
                            fillAlphaSetting.toFloat(),
                        )
                        drawFillBox(interpolateAxis(renderBB))
                    }

                    if (outlineSetting) {
                        setColor(
                            if (customColorSetting) {
                                outlineColorSetting.rgb
                            } else {
                                currentColor
                                    .getInterpolateColor()
                                    .rgb
                            },
                            outlineAlphaSetting.toFloat(),
                        )
                        GL11.glLineWidth(outlineWidthSetting.toFloat())
                        RenderGlobal.drawSelectionBoundingBox(interpolateAxis(renderBB))
                    }
                }
            } else {
                selectedBox =
                    selectedBox
                        .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                        .offset(-x, -y, -z)

                if (fillSetting) {
                    setColor(
                        if (customColorSetting) {
                            fillColorSetting.rgb
                        } else {
                            currentColor
                                .getInterpolateColor()
                                .rgb
                        },
                        fillAlphaSetting.toFloat(),
                    )
                    drawFillBox(selectedBox)
                }

                if (outlineSetting) {
                    setColor(
                        if (customColorSetting) {
                            outlineColorSetting.rgb
                        } else {
                            currentColor
                                .getInterpolateColor()
                                .rgb
                        },
                        outlineAlphaSetting.toFloat(),
                    )
                    GL11.glLineWidth(outlineWidthSetting.toFloat())
                    RenderGlobal.drawSelectionBoundingBox(selectedBox)
                }
            }
        }

        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()

        GlStateManager.disableBlend()

        if (depthSetting) {
            GlStateManager.enableDepth()
        }

        GL11.glLineWidth(2f)
    }

    private fun canRender(movingObjectPositionIn: MovingObjectPosition): Boolean {
        val entity = mc.renderViewEntity
        var result = entity is EntityPlayer && !mc.gameSettings.hideGUI

        if (result && !(entity as EntityPlayer).capabilities.allowEdit) {
            val itemstack = entity.currentEquippedItem

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                val selectedBlock = mc.objectMouseOver.blockPos
                val block = mc.theWorld.getBlockState(selectedBlock).block

                result =
                    if (mc.playerController.currentGameType == WorldSettings.GameType.SPECTATOR) {
                        block.hasTileEntity() && mc.theWorld.getTileEntity(selectedBlock) is IInventory
                    } else {
                        itemstack != null && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block))
                    }
            }
        }

        result = result && movingObjectPositionIn.typeOfHit == MovingObjectType.BLOCK

        return result
    }

    private fun interpolateAxis(bb: AxisAlignedBB): AxisAlignedBB =
        AxisAlignedBB(
            bb.minX - mc.renderManager.viewerPosX,
            bb.minY - mc.renderManager.viewerPosY,
            bb.minZ - mc.renderManager.viewerPosZ,
            bb.maxX - mc.renderManager.viewerPosX,
            bb.maxY - mc.renderManager.viewerPosY,
            bb.maxZ - mc.renderManager.viewerPosZ,
        )
}
