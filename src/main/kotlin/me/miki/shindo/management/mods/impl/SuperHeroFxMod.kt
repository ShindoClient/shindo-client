package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventAttackEntity
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.proj.Projection.w2s
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import java.awt.Color
import java.util.Random

class SuperHeroFxMod : Mod(TranslateText.SUPERHEROFX_NAME, TranslateText.SUPERHEROFX_DESCRIPTION, ModCategory.RENDER) {
    private val particles: MutableList<HeroTextParticle> = ArrayList()
    private var attackCounter = 0

    @EventTarget
    private fun onEntityHurt(event: EventAttackEntity) {
        if (event.getEntity() === mc.thePlayer || event.getEntity() !is EntityLivingBase) {
            return
        }
        attackCounter++
        if (attackCounter % 2 == 0) spawnHeroFX(event.getEntity() as EntityLivingBase)
    }

    @EventTarget
    private fun onUpdate(event: EventUpdate) {
        val rand = Random()
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.prevPos = p.pos
            p.pos = p.pos.add(p.velocity)
            val gravity = -0.008 - rand.nextDouble() * 0.006
            val drag = 0.98 - rand.nextDouble() * 0.02
            p.velocity = p.velocity.addVector(0.0, gravity, 0.0)
            p.velocity =
                Vec3(
                    p.velocity.xCoord * drag,
                    p.velocity.yCoord,
                    p.velocity.zCoord * drag,
                )
            p.age++
            if (p.age > p.maxAge) {
                it.remove()
            }
        }
    }

    @EventTarget
    fun onRenderWorldLast(event: EventRender3D) {
        val player: EntityPlayerSP = mc.thePlayer
        val px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks()
        val py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks()
        val pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks()
        val nvg: NanoVGManager = Shindo.getInstance().nanoVGManager!!
        val sr = ScaledResolution(mc)
        val scaleFactor = sr.scaleFactor.toFloat()
        nvg.setupAndDraw(
            Runnable {
                for (p in particles) {
                    val interpX =
                        p.prevPos.xCoord + (p.pos.xCoord - p.prevPos.xCoord) * event.getPartialTicks()
                    val interpY =
                        p.prevPos.yCoord + (p.pos.yCoord - p.prevPos.yCoord) * event.getPartialTicks()
                    val interpZ =
                        p.prevPos.zCoord + (p.pos.zCoord - p.prevPos.zCoord) * event.getPartialTicks()
                    val wx = (interpX - px).toFloat()
                    val wy = (interpY - py).toFloat()
                    val wz = (interpZ - pz).toFloat()
                    val screenPos = w2s(wx, wy, wz)
                    if (screenPos!!.zCoord > 1.0) {
                        continue
                    }
                    val distance = MathHelper.sqrt_float(wx * wx + wy * wy + wz * wz)
                    val distanceScale = MathHelper.clamp_float(1.0f / (distance * 0.15f + 0.3f), 0.3f, 2.5f)
                    var alpha = 1f - p.age / p.maxAge.toFloat()
                    alpha = MathHelper.clamp_float(alpha, 0.1f, 1f)
                    val a = (alpha * 255f).toInt()
                    nvg.drawText(
                        p.text,
                        screenPos.xCoord.toFloat() / scaleFactor,
                        screenPos.yCoord.toFloat() / scaleFactor,
                        Color(p.color shr 16 and 0xFF, p.color shr 8 and 0xFF, p.color and 0xFF, a),
                        p.scale * 12f * distanceScale,
                        Fonts.BANGERS,
                    )
                }
            },
        )
    }

    private fun spawnHeroFX(entity: EntityLivingBase) {
        val rand = Random()
        val word = WORDS[rand.nextInt(WORDS.size)]
        val pos =
            Vec3(
                entity.posX,
                entity.posY + entity.height * 0.8,
                entity.posZ,
            )
        val vel =
            Vec3(
                (rand.nextDouble() - 0.5) * (0.15 + rand.nextDouble() * 0.15),
                0.05 + rand.nextDouble() * 0.08,
                (rand.nextDouble() - 0.5) * (0.15 + rand.nextDouble() * 0.15),
            )
        val p =
            HeroTextParticle(
                pos,
                vel,
                word,
                15 + rand.nextInt(15),
                COLORS[rand.nextInt(COLORS.size)].rgb,
            )
        p.scale = 0.8f + rand.nextFloat() * 0.6f
        particles.add(p)
    }

    class HeroTextParticle(
        var pos: Vec3,
        velocity: Vec3,
        text: String,
        maxAge: Int,
        color: Int,
    ) {
        var prevPos: Vec3 = pos
        var velocity: Vec3
        var text: String
        var age: Int
        var maxAge: Int
        var scale: Float
        var color: Int

        init {
            this.velocity = velocity
            this.text = text
            age = 0
            this.maxAge = maxAge
            scale = 1.0f
            this.color = color
        }
    }

    companion object {
        private val WORDS =
            arrayOf(
                "POW!",
                "BAM!",
                "OUCH!",
                "ZAP!",
                "WHAM!",
                "CRIT!",
                "SMASH!",
                "BOOM!",
                "KAPOW!",
                "BANG!",
                "SLAM!",
                "WHACK!",
                "THWACK!",
                "ZING!",
                "BOP!",
                "CLANG!",
                "CLASH!",
                "KABLAM!",
                "SPLAT!",
                "THUD!",
            )
        private val COLORS =
            arrayOf(
                Color.RED,
                Color.YELLOW,
                Color.ORANGE,
                Color.CYAN,
                Color.MAGENTA,
                Color.GREEN,
            )
    }
}
