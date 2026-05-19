package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.utils.GlUtils.startScale
import me.miki.shindo.utils.GlUtils.stopScale
import me.miki.shindo.utils.ServerUtils.isJoinServer
import me.miki.shindo.utils.render.RenderUtils.drawTexturedModalRect
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.boss.BossStatus

class BossHealthMod : HUDMod(TranslateText.BOSS_HEALTH, TranslateText.BOSS_HEALTH_DESCRIPTION, Shinconic.MOD_BOSS_HEALTH) {
    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val bossHealthWidth = 182
        val scale = (BossStatus.healthScale * (bossHealthWidth + 1).toFloat()).toInt()

        if ((isJoinServer() && BossStatus.bossName != null && BossStatus.statusBarTime > 0) || this.isEditing()) {
            val title = if (this.isEditing()) "Boss Health" else BossStatus.bossName

            startScale(this.getX().toFloat(), this.getY().toFloat(), this.getScale())

            mc.textureManager.bindTexture(Gui.icons)
            BossStatus.statusBarTime--
            mc.textureManager.bindTexture(Gui.icons)

            drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 74, bossHealthWidth, 5)
            drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 74, bossHealthWidth, 5)

            if (scale > 0) {
                drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 79, scale, 5)
            }

            fr.drawStringWithShadow(
                title,
                (((182 / 2) - (fr.getStringWidth(title) / 2)) + this.getX()).toFloat(),
                ((this.getY() - 10) + 13).toFloat(),
                16777215,
            )
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            mc.textureManager.bindTexture(Gui.icons)

            stopScale()
        }

        this.setWidth(182)
        this.setHeight(20)
    }
}
