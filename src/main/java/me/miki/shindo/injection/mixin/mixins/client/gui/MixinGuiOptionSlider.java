package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.ui.theme.VanillaButtonRenderer;
import me.miki.shindo.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(GuiOptionSlider.class)
public abstract class MixinGuiOptionSlider {

    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int xPosition;
    @Shadow
    public int yPosition;
    @Shadow
    public String displayString;
    @Shadow
    public boolean visible;
    @Shadow
    public boolean enabled;
    @Shadow
    protected boolean hovered;
    @Shadow
    protected int packedFGColor;
    @Shadow
    public float sliderValue;
    @Shadow
    public boolean dragging;

    @Shadow
    protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    private void shindo$drawSlider(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if (!visible) {
            return;
        }

        // Update slider logic (value + display string) while suppressing vanilla knob rendering.
        this.mouseDragged(mc, mouseX, mouseY);
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        boolean drawn = VanillaButtonRenderer.drawButton((GuiButton) (Object) this,
                this.xPosition, this.yPosition, this.width, this.height,
                this.enabled, this.visible, this.hovered,
                this.packedFGColor, mc, mouseX, mouseY, context -> {
            float trackX = this.xPosition + 6F;
            float trackWidth = this.width - 12F;
            float trackHeight = 3F;
            float trackY = this.yPosition + (this.height / 2F) - (trackHeight / 2F);

            context.getNvg().drawRoundedRect(trackX, trackY, trackWidth, trackHeight, 1.5F,
                    ColorUtils.applyAlpha(context.getPalette().getBackgroundColor(ColorType.DARK), 155));

            float clampedValue = Math.max(0F, Math.min(1F, this.sliderValue));
            float fillWidth = trackWidth * clampedValue;
            if (fillWidth > 0.5F) {
                context.getNvg().drawGradientRoundedRect(trackX, trackY, fillWidth, trackHeight, 1.5F,
                        context.getAccent().getColor1(), context.getAccent().getColor2());
            }

            float knobRadius = Math.min(7.5F, Math.max(6F, this.height / 2F - 3F));
            float knobCenterX = trackX + fillWidth;
            float knobCenterY = this.yPosition + (this.height / 2F);

            context.getNvg().drawGradientCircle(knobCenterX, knobCenterY, knobRadius,
                    context.getAccent().getColor1(), context.getAccent().getColor2());
            context.getNvg().drawCircle(knobCenterX, knobCenterY, knobRadius - 3F,
                    ColorUtils.applyAlpha(Color.WHITE, 210));

            if (this.dragging || context.isHovered()) {
                context.getNvg().drawCircle(knobCenterX, knobCenterY, knobRadius + 1.5F,
                        ColorUtils.applyAlpha(context.getAccent().getColor2(), 90));
            }
        });

        if (drawn) {
            ci.cancel();
        }
    }

    @Redirect(method = "mouseDragged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawTexturedModalRect(IIIIII)V"))
    private void shindo$skipVanillaKnob(GuiButton button, int x, int y, int u, int v, int width, int height) {
        // Suppress vanilla textured slider knob rendering; themed renderer draws it instead.
    }
}
