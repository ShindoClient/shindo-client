package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRender2D
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.HUDMod
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.utils.GlUtils.startScale
import com.shindoclient.shindo.utils.GlUtils.stopScale
import com.shindoclient.shindo.utils.ServerUtils.isJoinServer
import com.shindoclient.shindo.utils.render.RenderUtils.drawTexturedModalRect
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
