package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRender2D
import com.shindoclient.shindo.management.event.impl.EventRenderCrosshair
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.mods.impl.crosshair.LayoutManager
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.CellGridSetting
import com.shindoclient.shindo.management.settings.impl.CellGridSettingConsumer
import com.shindoclient.shindo.utils.render.RenderUtils.drawRect
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.min

class CrosshairMod :
    Mod(TranslateText.CROSSHAIR, TranslateText.CROSSHAIR_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_CROSSHAIR),
    CellGridSettingConsumer {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HIDE_THIRD_PERSON_VIEW)
    private val hideInThirdPerson = false

    @Property(type = PropertyType.CELL_GRID, translate = TranslateText.DESIGN)
    private val crosshairLayout: Array<BooleanArray?> = layoutManager.defaultLayout

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private val crosshairColor: Color = Color.WHITE

    private var cellGridSetting: CellGridSetting? = null

    @EventTarget
    fun onRender(event: EventRender2D) {
        val sr = ScaledResolution(Minecraft.getMinecraft())

        if (hideInThirdPerson && mc.gameSettings.thirdPersonView != 0) {
            event.setCancelled(true)
        }

        if (!hideInThirdPerson || mc.gameSettings.thirdPersonView == 0) {
            val grid: Array<out BooleanArray?> =
                (if (cellGridSetting != null) cellGridSetting!!.getCells() else crosshairLayout) ?: return

            val toggled = isToggled()
            val rows = min(grid.size, 11)
            for (row in 0 until rows) {
                val cells = grid[row] ?: continue
                val cols = min(cells.size, 11)
                for (col in 0 until cols) {
                    if (cells[col] && toggled) {
                        drawRect(sr.scaledWidth / 2f - 5 + col, sr.scaledHeight / 2f - 5 + row, 1f, 1f, crosshairColor)
                    }
                }
            }
        }
    }

    @EventTarget
    fun onRender2D(event: EventRenderCrosshair) {
        event.setCancelled(true)
    }

    override fun onCellGridAvailable(setting: CellGridSetting) {
        this.cellGridSetting = setting
    }

    companion object {
        val layoutManager: LayoutManager = LayoutManager()
    }
}
