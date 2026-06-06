package com.shindoclient.shindo.management.mods.impl

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.shindoclient.shindo.Shindo.Companion.getInstance
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRender2D
import com.shindoclient.shindo.management.event.impl.EventRenderScoreboard
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.HUDMod
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.ColorUtils.getColorByInt
import com.shindoclient.shindo.utils.GlUtils.startScale
import com.shindoclient.shindo.utils.GlUtils.stopScale
import com.shindoclient.shindo.utils.render.RenderUtils
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.EnumChatFormatting
import java.awt.Color
import kotlin.math.max

class ScoreboardMod : HUDMod(TranslateText.SCOREBOARD, TranslateText.SCOREBOARD_DESCRIPTION, Shinconic.MOD_SCOREBOARD) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    private val showBackground = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NUMBER)
    private val showNumbers = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHADOW)
    private val drawShadow = false

    private var objective: ScoreObjective? = null
    private var isFirstLoad = false

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        if (isFirstLoad) {
            isFirstLoad = false
        }

        if (mc.isSingleplayer) {
            objective = null
        }

        if (objective != null) {
            val scoreboard = objective!!.scoreboard
            var scores = scoreboard.getSortedScores(objective)
            val filteredScores =
                scores
                    .filter { score ->
                        val name = score.playerName
                        name != null && !name.startsWith("#")
                    }.toMutableList()
            filteredScores.reverse()

            nvg.setupAndDraw(
                Runnable {
                    if (drawShadow) {
                        this.drawShadow(
                            0f,
                            0f,
                            this.getWidth() / this.getScale(),
                            this.getHeight() / this.getScale(),
                            0f,
                        )
                    }
                },
            )

            scores =
                if (filteredScores.size > 15) {
                    Lists.newArrayList<Score?>(Iterables.skip<Score?>(filteredScores, scores.size - 15))
                } else {
                    filteredScores
                }

            var maxWidth = fr.getStringWidth(objective!!.displayName)

            for (score in scores) {
                val scoreplayerteam = scoreboard.getPlayersTeam(score.playerName)
                var s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.playerName)

                if (showNumbers) {
                    s += ": " + EnumChatFormatting.RED + score.scorePoints
                }

                maxWidth = max(maxWidth, mc.fontRendererObj.getStringWidth(s))
            }

            var index = 0

            startScale(this.getX().toFloat(), this.getY().toFloat(), this.getScale())

            for (score in scores) {
                index++

                val scoreplayerteam = scoreboard.getPlayersTeam(score.playerName)
                val playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.playerName)
                val scorePoints = EnumChatFormatting.RED.toString() + "" + score.scorePoints

                RenderUtils.drawRect(
                    this.getX().toFloat(),
                    (this.getY() + (index * fr.FONT_HEIGHT) + 1).toFloat(),
                    (maxWidth + 4).toFloat(),
                    fr.FONT_HEIGHT.toFloat(),
                    if (showBackground) getColorByInt(1342177280) else Color(0, 0, 0, 0),
                )

                fr.drawString(playerName, this.getX() + 2, this.getY() + (index * fr.FONT_HEIGHT) + 1, 553648127)

                if (showNumbers) {
                    fr.drawString(
                        scorePoints,
                        (this.getX() + 2 + maxWidth + 2) - fr.getStringWidth(scorePoints),
                        this.getY() + (index * fr.FONT_HEIGHT) + 1,
                        553648127,
                    )
                }

                if (index == scores.size) {
                    val displayName = objective!!.displayName

                    RenderUtils.drawRect(
                        this.getX().toFloat(),
                        this.getY().toFloat(),
                        (2 + maxWidth + 2).toFloat(),
                        fr.FONT_HEIGHT.toFloat(),
                        if (showBackground) getColorByInt(1610612736) else Color(0, 0, 0, 0),
                    )
                    RenderUtils.drawRect(
                        this.getX().toFloat(),
                        (this.getY() + fr.FONT_HEIGHT).toFloat(),
                        (2 + maxWidth + 2).toFloat(),
                        1f,
                        if (showBackground) getColorByInt(1610612736) else Color(0, 0, 0, 0),
                    )

                    fr.drawString(
                        displayName,
                        this.getX() + 2 + maxWidth / 2 - fr.getStringWidth(displayName) / 2,
                        this.getY() + 1,
                        553648127,
                    )
                }
            }

            stopScale()

            val lastMaxWidth = maxWidth + 4
            val lastMaxHeight = (index * fr.FONT_HEIGHT) + 10

            this.setWidth(lastMaxWidth)
            this.setHeight(lastMaxHeight)
        }
    }

    @EventTarget
    fun onRenderScoreboard(event: EventRenderScoreboard) {
        event.setCancelled(true)
        objective = event.getObjective()
    }
}
