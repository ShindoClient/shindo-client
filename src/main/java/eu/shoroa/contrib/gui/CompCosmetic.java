package eu.shoroa.contrib.gui;

import eu.shoroa.contrib.cosmetic.Cosmetic;
import eu.shoroa.contrib.fake.ExampleGui;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.Minecraft;

public class CompCosmetic extends Comp {
    private final Cosmetic cosmetic;

    private final float width = 90f;
    private final float height = 140f;
    private final Animation animation;

    public CompCosmetic(Cosmetic cosmetic) {
        super(0f, 0f);
        this.cosmetic = cosmetic;
        animation = new SmoothStepAnimation(300, 1.0);
        animation.setValue(cosmetic.isEnabled() ? 1f : 0f);
    }

    public void translate(float x, float y) {
        setX(x);
        setY(y);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        animation.setDirection(cosmetic.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setEndPoint(1.0);

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        nvg.drawRoundedRect(getX(), getY(), width, height, 9f, ColorUtils.interpolateColor(palette.getBackgroundColor(ColorType.DARK), accentColor.getInterpolateColor(), animation.getValue()));
        nvg.drawRoundedRect(getX() + 2f, getY() + 2f, width - 4f, height - 4f, 7f, palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawRoundedRect(getX() + 6f, getY() + 6f, width - 12f, height - 24f, 5f, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawRoundedImage(cosmetic.getPreviewImage(), getX() + 6f, getY() + 6f, (width - 12f), height - 24f, 5f);
        nvg.drawCenteredText(cosmetic.getName(), getX() + getWidth() / 2f, getY() + getHeight() - 15f, palette.getFontColor(ColorType.NORMAL), 10f, Fonts.REGULAR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight()))
            return;

        if (mouseButton == 0) {
            cosmetic.toggle();
        }

        if (mouseButton == 1) {
            Minecraft.getMinecraft().displayGuiScreen(new ExampleGui());
        }
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
