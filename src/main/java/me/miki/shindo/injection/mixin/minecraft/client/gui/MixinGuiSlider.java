package me.miki.shindo.injection.mixin.minecraft.client.gui;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlider.class)
public abstract class MixinGuiSlider extends GuiButton{

    @Shadow @Final private GuiPageButtonList.GuiResponder responder;
    @Shadow protected abstract String getDisplayString();
    @Shadow public abstract float func_175220_c();
    @Shadow private float sliderPosition;
    @Shadow public boolean isMouseDown;

    protected MixinGuiSlider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, String name, float min, float max, float defaultValue, GuiSlider.FormatHelper formatter) {
        super(idIn, x, y, 150, 20, "");
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.visible)
        {
            if (this.isMouseDown)
            {
                this.sliderPosition = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);

                if (this.sliderPosition < 0.0F)
                {
                    this.sliderPosition = 0.0F;
                }

                if (this.sliderPosition > 1.0F)
                {
                    this.sliderPosition = 1.0F;
                }

                this.displayString = this.getDisplayString();
                this.responder.onTick(this.id, this.func_175220_c());
            }

            Shindo  instance = Shindo.getInstance();
            NanoVGManager nvg = instance.getNanoVGManager();
            ColorPalette palette = instance.getColorManager().getPalette();
            nvg.setupAndDraw(() -> drawNanoVG(nvg, palette, mouseX, mouseY));
        }
        ci.cancel();
    }

    @Unique
    private void drawNanoVG(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY) {
        nvg.drawRoundedRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)), this.yPosition, 8f, 20f, 2f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 245));
        nvg.drawOutlineRoundedRect(this.xPosition + (int)(this.sliderPosition * (float)(this.width - 8)), this.yPosition, 8f, 20f, 2f, 1, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 230));
    }
}
