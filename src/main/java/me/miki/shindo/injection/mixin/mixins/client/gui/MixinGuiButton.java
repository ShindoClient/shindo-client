package me.miki.shindo.injection.mixin.mixins.client.gui;

import me.miki.shindo.ui.theme.VanillaButtonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton {

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

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    private void shindo$drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if ((Object) this instanceof GuiOptionSlider) {
            return;
        }

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        if (VanillaButtonRenderer.drawButton((GuiButton) (Object) this,
                this.xPosition, this.yPosition, this.width, this.height,
                this.enabled, this.visible, this.hovered,
                this.packedFGColor, mc, mouseX, mouseY, null)) {
            ci.cancel();
        }
    }
}
