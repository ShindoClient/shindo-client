package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.impl.minimap.ChunkAtlas
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.GlUtils.startTranslate
import me.miki.shindo.utils.GlUtils.stopTranslate
import me.miki.shindo.ui.animation.screen.ScreenStencil
import me.miki.shindo.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

class MinimapMod : HUDMod(TranslateText.MINIMAP, TranslateText.MINIMAP_DESCRIPTION, LegacyIcon.MOD_MINIMAP) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.WIDTH,
        min = 10.0,
        max = 180.0,
        current = 150.0,
        step = 1.0
    )
    private val widthSetting = 150

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.HEIGHT,
        min = 10.0,
        max = 180.0,
        current = 70.0,
        step = 1.0
    )
    private val heightSetting = 70

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0, max = 1.0, current = 1.0)
    private val alphaSetting = 1.0

    private val stencil = ScreenStencil()
    private var chunkAtlas: ChunkAtlas? = null

    override fun setup() {
        chunkAtlas = ChunkAtlas(10)
    }

    @EventTarget
    fun onRender2D(event: EventRender2D) {
        val nvg = getInstance().nanoVGManager
        val width = widthSetting
        val height = heightSetting

        nvg!!.setupAndDraw(Runnable {
            nvg.drawShadow(
                this.getX().toFloat(),
                this.getY().toFloat(),
                width * this.getScale(),
                height * this.getScale(),
                6 * this.getScale()
            )
        })

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()

        stencil.wrap(
            Runnable { drawMap(event.getPartialTicks()) },
            this.getX().toFloat(),
            this.getY().toFloat(),
            width * this.getScale(),
            height * this.getScale(),
            6 * this.getScale(),
            alphaSetting.toFloat()
        )

        this.setWidth(width)
        this.setHeight(height)
    }

    private fun drawMap(partialTicks: Float) {
        val width = widthSetting
        val height = heightSetting
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        val p: EntityPlayer = mc.thePlayer

        val x = lerp(p.prevPosX, p.posX, partialTicks)
        val z = lerp(p.prevPosZ, p.posZ, partialTicks)
        val yaw = lerp(p.prevRotationYaw.toDouble(), p.rotationYaw.toDouble(), partialTicks)

        chunkAtlas!!.loadChunks(x.toInt() shr 4, z.toInt() shr 4)

        RenderUtils.drawRect(
            this.getX().toFloat(),
            this.getY().toFloat(),
            this.getWidth().toFloat(),
            this.getHeight().toFloat(),
            Color(138, 176, 254)
        )

        startTranslate(this.getX() + (width / 2) * this.getScale(), this.getY() + (height / 2) * this.getScale())

        GL11.glRotated(180 - yaw, 0.0, 0.0, 1.0)

        GlStateManager.color(1f, 1f, 1f)
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.bindTexture(chunkAtlas!!.textureHandle)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

        val chunkWidth = chunkAtlas!!.spriteWidth
        val chunkHeight = chunkAtlas!!.spriteHeight

        for (sprite in chunkAtlas!!) {
            val minX = chunkAtlas!!.getSpriteX(sprite.offset)
            val minY = chunkAtlas!!.getSpriteY(sprite.offset)

            val maxX = minX + chunkWidth
            val maxY = minY + chunkHeight

            val renderX = (sprite.chunkX shl 4) - x
            val renderY = (sprite.chunkZ shl 4) - z

            worldRenderer.pos(renderX, renderY, 0.0).tex(minX, minY).endVertex()
            worldRenderer.pos(renderX, renderY + 16.0, 0.0).tex(minX, maxY).endVertex()
            worldRenderer.pos(renderX + 16.0, renderY + 16.0, 0.0).tex(maxX, maxY).endVertex()
            worldRenderer.pos(renderX + 16.0, renderY + 0.0, 0.0).tex(maxX, minY).endVertex()
        }

        tessellator.draw()

        stopTranslate()
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld?) {
        chunkAtlas!!.clear()
    }

    private fun lerp(prev: Double, current: Double, partialTicks: Float): Double {
        return prev + (current - prev) * partialTicks
    }

    override fun onEnable() {
        super.onEnable()
        chunkAtlas!!.clear()
    }
}




