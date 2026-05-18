package me.miki.shindo.ui.particle

import me.miki.shindo.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import java.awt.Color
import kotlin.random.Random

class ParticleEngine {
    private val particles: MutableList<Particle> = mutableListOf()
    private val mc: Minecraft = Minecraft.getMinecraft()
    private var amount: Int = 0

    private var prevWidth: Int = 0
    private var prevHeight: Int = 0

    fun draw(
        mouseX: Int,
        mouseY: Int,
    ) {
        if (particles.isEmpty() || prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
            particles.clear()
            amount = (mc.displayWidth + mc.displayHeight) / 8
            create()
        }

        prevWidth = mc.displayWidth
        prevHeight = mc.displayHeight

        for (particle in particles) {
            if (particle.timer.delay((1000 / 60).toLong())) {
                particle.fall()
                particle.interpolation()
                particle.timer.reset()
            }

            val range = 50
            val mouseOver =
                mouseX >= particle.x - range &&
                    mouseY >= particle.y - range &&
                    mouseX <= particle.x + range &&
                    mouseY <= particle.y + range

            if (mouseOver) {
                particles
                    .asSequence()
                    .filter { part ->
                        (part.x > particle.x && part.x - particle.x < range && particle.x - part.x < range) &&
                            (
                                (part.y > particle.y && part.y - particle.y < range) ||
                                    (particle.y > part.y && particle.y - part.y < range)
                            )
                    }.forEach { connectable -> particle.connect(connectable.x, connectable.y) }
            }

            RenderUtils.drawRect(particle.x, particle.y, particle.size, particle.size, Color.WHITE)
        }
    }

    private fun create() {
        repeat(amount) {
            particles.add(
                Particle(Random.nextInt(mc.displayWidth), Random.nextInt(mc.displayHeight)),
            )
        }
    }
}
