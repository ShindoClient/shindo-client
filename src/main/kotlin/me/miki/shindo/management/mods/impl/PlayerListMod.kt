package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class PlayerListMod : HUDMod(TranslateText.PLAYER_LIST, TranslateText.PLAYER_LIST_DESCRIPTION, Shinconic.MOD_PLAYER_LIST) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MAX,
        min = 1.0,
        max = 100.0,
        current = 16.0,
        step = 1.0,
    )
    private val maxSetting = 16

    private var index = 0
    private var maxName = 0f

    @EventTarget
    fun onRender2D(event: EventNVG) {
        var prevIndex = 0
        var offsetY = 23

        this.drawBackground(maxName, (index * 15) + 24.5f)
        this.drawText("Player List", 5.5f, 6f, 10.5f, getHudFont(1))
        this.drawRect(0f, 18f, maxName, 1f)

        for (playerInfo in mc.netHandler.playerInfoMap) {
            if (playerInfo != null && playerInfo.gameProfile != null) {
                val name = playerInfo.gameProfile.name

                if (this.getTextWidth(name, 9f, getHudFont(2))!! + 26 > maxName) {
                    maxName = this.getTextWidth(name, 9f, getHudFont(2))!! + 26
                }

                this.drawPlayerHead(playerInfo.locationSkin, 5.5f, offsetY.toFloat(), 12f, 12f, 2.5f)
                this.drawText(name, 20f, offsetY + 2.5f, 9f, getHudFont(1))

                if (prevIndex > maxSetting) {
                    prevIndex++
                    index = prevIndex
                    break
                }

                prevIndex++
                offsetY += 15
            }
        }

        index = prevIndex

        this.setWidth(maxName.toInt())
        this.setHeight((index * 15) + 26)
    }
}
