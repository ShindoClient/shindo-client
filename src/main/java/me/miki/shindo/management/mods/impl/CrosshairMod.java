package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventRenderCrosshair;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.impl.crosshair.LayoutManager;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class CrosshairMod extends Mod {

    public static final LayoutManager layoutManager = new LayoutManager();

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR, category = "Display")
    private Color crosshairColor = Color.RED;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HIDE_THIRD_PERSON_VIEW, category = "Display")
    private boolean hideInThirdPerson;

    @Property(type = PropertyType.CELL_GRID, translate = TranslateText.DESIGN, category = "Design")
    private boolean[][] crosshairLayout = layoutManager.getLayout(0);

    public CrosshairMod() {
        super(TranslateText.CROSSHAIR, TranslateText.CROSSHAIR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRender(EventRender2D event) {

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        if (hideInThirdPerson && mc.gameSettings.thirdPersonView != 0) {
            event.setCancelled(true);
        }

        if (!hideInThirdPerson || mc.gameSettings.thirdPersonView == 0) {
            boolean[][] grid = crosshairLayout;
            if (grid == null) {
                return;
            }

            boolean toggled = isToggled();
            int rows = Math.min(grid.length, 11);
            for (int row = 0; row < rows; row++) {
                boolean[] cells = grid[row];
                if (cells == null) {
                    continue;
                }
                int cols = Math.min(cells.length, 11);
                for (int col = 0; col < cols; col++) {
                    if (cells[col] && toggled) {
                        RenderUtils.drawRect(sr.getScaledWidth() / 2F - 5 + col,
                                sr.getScaledHeight() / 2F - 5 + row,
                                1, 1, crosshairColor);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRenderCrosshair event) {
        event.setCancelled(true);
    }
}
