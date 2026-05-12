package me.miki.shindo.injection.mixin.minecraft.client.gui;


import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {


    @Shadow public boolean visible;
    @Shadow protected boolean hovered;
    @Shadow public int xPosition;
    @Shadow public int yPosition;
    @Shadow protected int width;
    @Shadow protected int height;
    @Shadow protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);
    @Shadow public boolean enabled;
    @Shadow public String displayString;

    @Shadow
    protected abstract int getHoverState(boolean mouseOver);

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.visible) {
            Shindo instance = Shindo.getInstance();
            NanoVGManager nvg = instance.getNanoVGManager();
            ColorManager colorManager = instance.getColorManager();
            ColorPalette palette = colorManager.getPalette();

            nvg.setupAndDraw(() -> drawNanoVG(nvg, palette, mouseX, mouseY));
            mouseDragged(mc, mouseX, mouseY);
        }
        ci.cancel();
    }

    @Unique
    private void drawNanoVG(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY) {

        this.hovered = MouseUtils.isInside(mouseX, mouseY, xPosition, yPosition, width, height);

        nvg.drawShadow(this.xPosition, this.yPosition, this.width, this.height, 2f, 7);
        nvg.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, 2f, ColorUtils.transitionColor(ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 245), ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 235), hovered));
        nvg.drawOutlineRoundedRect(this.xPosition, this.yPosition, width, height, 2f, 1, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 230));
        nvg.drawCenteredText(this.displayString, this.xPosition + this.width / 2f, this.yPosition + (this.height - nvg.getTextHeight(this.displayString, 12f, Fonts.MOJANGLES)) / 2, palette.getFontColor(ColorType.NORMAL), 12f, Fonts.MOJANGLES);
    }

}
