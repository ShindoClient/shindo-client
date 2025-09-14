package me.miki.shindo.ui.comp.mods;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.mods.impl.CrosshairMod;
import me.miki.shindo.management.mods.settings.impl.CellGridSetting;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;

public class CompCellGrid extends Comp {

    private final CellGridSetting setting;
    @Setter
    @Getter
    private float width, height;

    public CompCellGrid(float x, float y, int width, int height, CellGridSetting setting) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.setting = setting;
    }

    public CompCellGrid(float width, float height, CellGridSetting setting) {
        super(0, 0);
        this.width = width;
        this.height = height;
        this.setting = setting;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();


        //nvg.drawRoundedRect(this.getX(), this.getY(), width, 160, 3, palette.getBackgroundColor(ColorType.NORMAL));

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                float x = this.getX() + width - 115 + col * 10;
                float y = this.getY() + 5 + row * 10;
                nvg.drawRect(x, y, 10, 10,
                        setting.getCells()[row][col] ?
                                MouseUtils.isInside(mouseX, mouseY, x, y, 10, 10) ? new Color(255, 255, 255, 50) : new Color(255, 255, 255, 255) :
                                MouseUtils.isInside(mouseX, mouseY, x, y, 10, 10) ? new Color(255, 255, 255, 20) : palette.getBackgroundColor(ColorType.NORMAL)

                );
            }
        }

        int index = 0;
        int counter = 0;
        for (boolean[][] layout : CrosshairMod.layoutManager.getLayoutList()) {
            float x = this.getX() + width - 165 - counter * 40;
            float y = this.getY() + 5 + index * 40;
            nvg.drawRoundedRect(x, y, 35, 35, 2,
                    MouseUtils.isInside(mouseX, mouseY, x, y, 37, 37) ? palette.getBackgroundColor(ColorType.DARK) : palette.getBackgroundColor(ColorType.NORMAL));

            for (int row = 0; row < 11; row++) {
                for (int col = 0; col < 11; col++) {
                    nvg.drawRect(
                            this.getX() + width - 158 - counter * 40 + col * 2,
                            this.getY() + 13 + index * 40 + row * 2,
                            2, 2, layout[row][col] ? new Color(255, 255, 255, 255) : new Color(255, 255, 255, 0)
                    );
                }
            }

            index++;
            if (index % 3 == 0) {
                counter++;
                index = 0;
            }
        }

        String text = "Erase all";
        float twidth = nvg.getTextWidth(text, 10, Fonts.REGULAR);
        float x = this.getX() + width - 50;
        float y = this.getY() + 125;


        nvg.drawRoundedRect(x - 9, y, twidth + 10, 20, 2,
                MouseUtils.isInside(mouseX, mouseY, x - 9, y, twidth + 20, 20) ? palette.getBackgroundColor(ColorType.DARK) : palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawText(text, x - 3, y + 6, palette.getFontColor(ColorType.DARK), 10, Fonts.MEDIUM);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                if (MouseUtils.isInside(mouseX, mouseY,
                        this.getX() + width - 115 + col * 10,
                        this.getY() + 5 + row * 10,
                        10, 10
                )) {
                    setting.getCells()[row][col] = !setting.getCells()[row][col];
                }
            }
        }

        int index = 0;
        int counter = 0;
        for (boolean[][] layout : CrosshairMod.layoutManager.getLayoutList()) {
            float x = this.getX() + width - 165 - counter * 40;
            float y = this.getY() + 5 + index * 40;
            if (MouseUtils.isInside(mouseX, mouseY, x, y, 35, 35)) {
                setting.setCells(layout);
            }

            index++;
            if (index % 3 == 0) {
                counter++;
                index = 0;
            }
        }

        String text = "Erase all";
        float twidth = nvg.getTextWidth(text, 9.5F, Fonts.REGULAR);
        float x = this.getX() + width - 50;
        float y = this.getY() + 125;

        if (MouseUtils.isInside(mouseX, mouseY, x - 9, y, twidth + 20, 20)) {
            setting.setCells(new boolean[11][11]);
        }
    }

}
