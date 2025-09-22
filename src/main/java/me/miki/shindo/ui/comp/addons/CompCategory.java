package me.miki.shindo.ui.comp.addons;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.settings.impl.CategorySetting;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.ui.comp.Comp;

@Getter
public class CompCategory extends Comp {

    private final CategorySetting setting;

    private final float width;

    public CompCategory(float x, float y, float width, CategorySetting setting) {
        super(x, y);

        this.width = width;
        this.setting = setting;
    }

    public CompCategory(float width, CategorySetting setting) {
        super(0, 0);
        this.width = width;
        this.setting = setting;
    }


    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();

        nvg.drawGradientRoundedRect(getX(), getY(), width, 3, 4, accentColor.getColor1(), accentColor.getColor2());

    }
}
